package com.digitaladventure.dw2003.emulation

import com.digitaladventure.dw2003.data.GameStateReader

/**
 * START opens the last used status-menu tab (Items, Sort, Map, Techniques,
 * Status, Card Folder). The status overlay is `STSTATUS` at `0x80080000`;
 * it does **not** write `0x1000` into AREA/MAP_ID, so those words cannot
 * detect the menu. Tab index is discovered by probing one R1.
 */
enum class RetroPadButton { START, L1, R1, CROSS, TRIANGLE }

data class PadStep(val button: RetroPadButton, val afterMs: Long, val holdMs: Long = 120)

data class MenuTabCursor(val offset: Int, val index: Int, val r1Increases: Boolean)

object FastTravelNavigator {
    const val MENU_OVERLAY = 0x1000
    const val MENU_SETTLE_MS = 400L
    const val TAB_COUNT = 6
    const val MAP_TAB_INDEX = 2

    fun dismissMenu(): List<PadStep> = listOf(PadStep(RetroPadButton.TRIANGLE, 500))

    fun pressStart(): List<PadStep> = listOf(PadStep(RetroPadButton.START, 80))

    fun probeNextTab(): List<PadStep> = listOf(PadStep(RetroPadButton.R1, 420))

    fun fallbackAfterProbe(): List<PadStep> = listOf(PadStep(RetroPadButton.R1, 480))

    fun confirmMapDestination(): List<PadStep> = listOf(
        PadStep(RetroPadButton.CROSS, 380),
        PadStep(RetroPadButton.TRIANGLE, 480)
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

    fun findTabCursor(before: ByteArray, afterR1: ByteArray): MenuTabCursor? {
        if (before.size != afterR1.size || before.size < 2) return null
        val hits = mutableListOf<MenuTabCursor>()
        for (offset in 0 until before.size - 1) {
            if ((before[offset + 1].toInt() and 0xFF) != 0) continue
            if ((afterR1[offset + 1].toInt() and 0xFF) != 0) continue
            val previous = GameStateReader.u16(before, offset)
            val next = GameStateReader.u16(afterR1, offset)
            val delta = tabDelta(previous, next) ?: continue
            hits += MenuTabCursor(offset, next, r1Increases = delta == 1)
        }
        return hits.singleOrNull()
    }

    fun stepsTowardMap(currentIndex: Int, r1Increases: Boolean): List<PadStep> {
        val index = ((currentIndex % TAB_COUNT) + TAB_COUNT) % TAB_COUNT
        if (index == MAP_TAB_INDEX) return emptyList()
        val forward = (MAP_TAB_INDEX - index + TAB_COUNT) % TAB_COUNT
        val backward = (index - MAP_TAB_INDEX + TAB_COUNT) % TAB_COUNT
        val useForward = forward <= backward
        val button = when {
            useForward && r1Increases -> RetroPadButton.R1
            useForward && !r1Increases -> RetroPadButton.L1
            !useForward && r1Increases -> RetroPadButton.L1
            else -> RetroPadButton.R1
        }
        val count = if (useForward) forward else backward
        return List(count) { PadStep(button, 420) }
    }
}
