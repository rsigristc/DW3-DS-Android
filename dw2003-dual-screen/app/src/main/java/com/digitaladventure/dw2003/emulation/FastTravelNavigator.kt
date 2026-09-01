package com.digitaladventure.dw2003.emulation

import com.digitaladventure.dw2003.data.GameStateReader

/**
 * START opens a vertical list (Items, Sort, Map, Techniques, Status)
 * moved with the D-pad, not L1/R1. Cross on Map opens Flawe's world map,
 * where L1/R1 cycle unlocked icons and Cross + Triangle confirm the warp.
 */
enum class RetroPadButton { START, L1, R1, CROSS, TRIANGLE, DPAD_UP, DPAD_DOWN }

data class PadStep(val button: RetroPadButton, val afterMs: Long, val holdMs: Long = 120)

data class MenuTabCursor(val offset: Int, val index: Int, val downIncreases: Boolean)

object FastTravelNavigator {
    const val MENU_OVERLAY = 0x1000
    const val MENU_SETTLE_MS = 400L
    const val TAB_COUNT = 5
    const val MAP_TAB_INDEX = 2

    fun dismissMenu(): List<PadStep> = listOf(PadStep(RetroPadButton.TRIANGLE, 500))

    fun pressStart(): List<PadStep> = listOf(PadStep(RetroPadButton.START, 80))

    fun probeNextEntry(): List<PadStep> = listOf(PadStep(RetroPadButton.DPAD_DOWN, 280))

    fun fallbackFromItemsToMap(): List<PadStep> = listOf(
        PadStep(RetroPadButton.DPAD_DOWN, 280),
        PadStep(RetroPadButton.CROSS, 520)
    )

    fun enterMapFromHighlight(): List<PadStep> = listOf(PadStep(RetroPadButton.CROSS, 520))

    fun confirmMapDestination(): List<PadStep> = listOf(
        PadStep(RetroPadButton.CROSS, 400),
        PadStep(RetroPadButton.TRIANGLE, 500)
    )

    fun closeMenu(): List<PadStep> = listOf(PadStep(RetroPadButton.TRIANGLE, 420))

    fun isStatusMenu(overlaySignature: Long, areaId: Int, mapId: Int): Boolean =
        overlaySignature == GameStateReader.STSTATUS_SIGNATURE ||
            areaId == MENU_OVERLAY ||
            mapId == MENU_OVERLAY

    fun tabDelta(before: Int, after: Int): Int? {
        if (before !in 0 until TAB_COUNT || after !in 0 until TAB_COUNT) return null
        return when ((after - before + TAB_COUNT) % TAB_COUNT) {
            1 -> 1
            TAB_COUNT - 1 -> -1
            else -> null
        }
    }

    fun findTabCursor(before: ByteArray, afterDown: ByteArray): MenuTabCursor? {
        if (before.size != afterDown.size || before.size < 2) return null
        val hits = mutableListOf<MenuTabCursor>()
        for (offset in 0 until before.size - 1) {
            if ((before[offset + 1].toInt() and 0xFF) != 0) continue
            if ((afterDown[offset + 1].toInt() and 0xFF) != 0) continue
            val previous = GameStateReader.u16(before, offset)
            val next = GameStateReader.u16(afterDown, offset)
            val delta = tabDelta(previous, next) ?: continue
            hits += MenuTabCursor(offset, next, downIncreases = delta == 1)
        }
        return hits.singleOrNull()
    }

    fun stepsTowardMap(currentIndex: Int, downIncreases: Boolean): List<PadStep> {
        val index = ((currentIndex % TAB_COUNT) + TAB_COUNT) % TAB_COUNT
        if (index == MAP_TAB_INDEX) return enterMapFromHighlight()
        val forward = (MAP_TAB_INDEX - index + TAB_COUNT) % TAB_COUNT
        val backward = (index - MAP_TAB_INDEX + TAB_COUNT) % TAB_COUNT
        val useForward = forward <= backward
        val button = when {
            useForward && downIncreases -> RetroPadButton.DPAD_DOWN
            useForward && !downIncreases -> RetroPadButton.DPAD_UP
            !useForward && downIncreases -> RetroPadButton.DPAD_UP
            else -> RetroPadButton.DPAD_DOWN
        }
        val count = if (useForward) forward else backward
        return List(count) { PadStep(button, 280) } + enterMapFromHighlight()
    }

    fun stepsToFlaweIcon(fromIcon: Int, toIcon: Int, order: List<Int>): List<PadStep> {
        if (fromIcon == toIcon || order.size < 2) return emptyList()
        val start = order.indexOf(fromIcon).takeIf { it >= 0 } ?: 0
        val end = order.indexOf(toIcon).takeIf { it >= 0 } ?: return emptyList()
        if (start == end) return emptyList()
        val forward = (end - start + order.size) % order.size
        val backward = (start - end + order.size) % order.size
        return if (forward <= backward) {
            List(forward) { PadStep(RetroPadButton.R1, 420) }
        } else {
            List(backward) { PadStep(RetroPadButton.L1, 420) }
        }
    }
}
