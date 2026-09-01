package com.digitaladventure.dw2003.emulation

import com.digitaladventure.dw2003.data.GameStateReader

/**
 * START opens a vertical list (Items, Sort, Map, Techniques, Status)
 * moved with the D-pad, not L1/R1. Cross on Map opens Flawe's world map,
 * where L1/R1 cycle unlocked icons and Cross + Triangle confirm the warp.
 */
enum class RetroPadButton { START, L1, R1, CROSS, TRIANGLE, DPAD_UP, DPAD_DOWN }

data class PadStep(val button: RetroPadButton, val afterMs: Long, val holdMs: Long = 70)

object FastTravelNavigator {
    const val MENU_OVERLAY = 0x1000
    const val MENU_SETTLE_MS = 100L

    fun dismissMenu(): List<PadStep> = listOf(PadStep(RetroPadButton.TRIANGLE, 220))

    fun pressStart(): List<PadStep> = listOf(PadStep(RetroPadButton.START, 50, 100))

    fun selectMapFromItems(): List<PadStep> = listOf(
        PadStep(RetroPadButton.DPAD_DOWN, 50),
        PadStep(RetroPadButton.DPAD_DOWN, 50),
        PadStep(RetroPadButton.CROSS, 220, 90)
    )

    fun confirmMapDestination(): List<PadStep> = listOf(
        PadStep(RetroPadButton.CROSS, 240, 100),
        PadStep(RetroPadButton.TRIANGLE, 300, 100)
    )

    fun closeMenu(): List<PadStep> = listOf(PadStep(RetroPadButton.TRIANGLE, 260))

    fun isStatusMenu(overlaySignature: Long, areaId: Int, mapId: Int): Boolean =
        overlaySignature == GameStateReader.STSTATUS_SIGNATURE ||
            areaId == MENU_OVERLAY ||
            mapId == MENU_OVERLAY

    fun stepsToFlaweIcon(fromIcon: Int, toIcon: Int, order: List<Int>): List<PadStep> {
        if (fromIcon == toIcon || order.size < 2) return emptyList()
        val start = order.indexOf(fromIcon).takeIf { it >= 0 } ?: 0
        val end = order.indexOf(toIcon).takeIf { it >= 0 } ?: return emptyList()
        if (start == end) return emptyList()
        val forward = (end - start + order.size) % order.size
        val backward = (start - end + order.size) % order.size
        return if (forward <= backward) {
            List(forward) { PadStep(RetroPadButton.R1, 220, 100) }
        } else {
            List(backward) { PadStep(RetroPadButton.L1, 220, 100) }
        }
    }
}
