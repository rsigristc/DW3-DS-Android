package com.digitaladventure.dw2003.emulation

/**
 * Flawe's Fast Travel lives on the in-game Map tab, not on the first START screen.
 * The field status menu (overlay `0x1000`) starts on STATUS; L1 twice reaches MAP
 * (STATUS → TECHNIQUES → MAP). Selecting a destination and fully leaving the menu
 * is what triggers the loading screen.
 */
enum class RetroPadButton { START, L1, CROSS, TRIANGLE }

data class PadStep(val button: RetroPadButton, val afterMs: Long)

object FastTravelNavigator {
    const val MENU_OVERLAY = 0x1000

    fun openMap(menuAlreadyOpen: Boolean): List<PadStep> {
        val steps = mutableListOf<PadStep>()
        if (menuAlreadyOpen) {
            steps += PadStep(RetroPadButton.TRIANGLE, 450)
        }
        steps += PadStep(RetroPadButton.START, 560)
        steps += PadStep(RetroPadButton.L1, 260)
        steps += PadStep(RetroPadButton.L1, 300)
        return steps
    }

    fun commitSelectionAndExit(): List<PadStep> = listOf(
        PadStep(RetroPadButton.CROSS, 220),
        PadStep(RetroPadButton.TRIANGLE, 380)
    )

    fun isMenuOverlay(areaId: Int, mapId: Int): Boolean =
        areaId == MENU_OVERLAY || mapId == MENU_OVERLAY
}
