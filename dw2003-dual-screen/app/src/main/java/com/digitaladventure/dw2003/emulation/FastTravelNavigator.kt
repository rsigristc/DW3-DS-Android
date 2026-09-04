package com.digitaladventure.dw2003.emulation

import com.digitaladventure.dw2003.data.GameStateReader

/**
 * START opens a vertical list (Items, Sort, Map, Techniques, Status)
 * moved with the D-pad, not L1/R1. The STSTATUS pad handler stores the
 * 0–4 tab index at widget+0x178 (MAP = 2). That widget base is not a
 * stable global, so this helper never writes overlay RAM: it walks with
 * UP/DOWN. The list does not wrap; Status+Down clamps at 4.
 *
 * Cross on Map opens Flawe's world map, where the D-pad selects icons
 * and Cross + Triangle confirm the warp.
 */
enum class RetroPadButton { START, L1, R1, CROSS, TRIANGLE, SQUARE, DPAD_UP, DPAD_DOWN }

data class PadStep(val button: RetroPadButton, val afterMs: Long, val holdMs: Long = 70)

object FastTravelNavigator {
    const val MENU_OVERLAY = 0x1000
    const val MENU_SETTLE_MS = 100L
    const val TAB_ITEMS = 0
    const val TAB_SORT = 1
    const val TAB_MAP = 2
    const val TAB_TECHNIQUES = 3
    const val TAB_STATUS = 4

    fun dismissMenu(): List<PadStep> = listOf(PadStep(RetroPadButton.TRIANGLE, 220))

    fun pressStart(): List<PadStep> = listOf(PadStep(RetroPadButton.START, 50, 100))

    fun stepsToMapTab(fromTab: Int = TAB_ITEMS): List<PadStep> {
        val from = fromTab.coerceIn(TAB_ITEMS, TAB_STATUS)
        val button = when {
            from < TAB_MAP -> RetroPadButton.DPAD_DOWN
            from > TAB_MAP -> RetroPadButton.DPAD_UP
            else -> null
        }
        val moves = if (button == null) {
            emptyList()
        } else {
            List(kotlin.math.abs(TAB_MAP - from)) { PadStep(button, 50) }
        }
        return moves + PadStep(RetroPadButton.CROSS, 220, 90)
    }

    fun selectMapFromItems(): List<PadStep> = stepsToMapFromUnknown()

    /**
     * START remembers the last tab and the list does not wrap. Four ups
     * clamp on Items, then two downs always land on Map before Cross.
     */
    fun stepsToMapFromUnknown(): List<PadStep> {
        val reset = List(TAB_STATUS - TAB_ITEMS) { PadStep(RetroPadButton.DPAD_UP, 60, 80) }
        val down = List(TAB_MAP - TAB_ITEMS) { PadStep(RetroPadButton.DPAD_DOWN, 60, 80) }
        return reset + down + PadStep(RetroPadButton.CROSS, 280, 100)
    }

    fun switchServer(): List<PadStep> = listOf(PadStep(RetroPadButton.SQUARE, 380, 110))

    fun selectMapDestination(): List<PadStep> =
        listOf(PadStep(RetroPadButton.CROSS, 400, 120))

    fun exitMapMenu(): List<PadStep> = listOf(
        PadStep(RetroPadButton.TRIANGLE, 420, 120),
        PadStep(RetroPadButton.TRIANGLE, 500, 120)
    )

    fun confirmMapDestination(): List<PadStep> =
        selectMapDestination() + exitMapMenu()

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
        return if (end > start) {
            List(end - start) { PadStep(RetroPadButton.DPAD_DOWN, 420, 120) }
        } else {
            List(start - end) { PadStep(RetroPadButton.DPAD_UP, 420, 120) }
        }
    }
}
