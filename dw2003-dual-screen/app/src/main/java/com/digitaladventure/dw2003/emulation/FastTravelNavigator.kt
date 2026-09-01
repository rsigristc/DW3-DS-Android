package com.digitaladventure.dw2003.emulation

/**
 * START opens the field menu on ITEMS (leftmost tab):
 * ITEMS, SORT, MAP, TECHNIQUES, STATUS, CARD FOLDER.
 * Two R1 steps reach MAP. Two L1 steps wrap to STATUS, which looks like
 * the main Start screen. L1/R1 must wait until overlay `0x1000` is up.
 * Flawe loads on menu exit after a destination ID is written; × on the map
 * picks whatever icon has focus and dumps the player on inaccessible tiles.
 */
enum class RetroPadButton { START, L1, R1, CROSS, TRIANGLE }

data class PadStep(val button: RetroPadButton, val afterMs: Long, val holdMs: Long = 110)

object FastTravelNavigator {
    const val MENU_OVERLAY = 0x1000
    const val MENU_SETTLE_MS = 450L

    fun dismissMenu(): List<PadStep> = listOf(PadStep(RetroPadButton.TRIANGLE, 500))

    fun pressStart(): List<PadStep> = listOf(PadStep(RetroPadButton.START, 80))

    fun moveToMapTab(): List<PadStep> = listOf(
        PadStep(RetroPadButton.R1, 480),
        PadStep(RetroPadButton.R1, 520)
    )

    fun closeMenu(): List<PadStep> = listOf(PadStep(RetroPadButton.TRIANGLE, 420))

    fun isMenuOverlay(areaId: Int, mapId: Int): Boolean =
        areaId == MENU_OVERLAY || mapId == MENU_OVERLAY
}
