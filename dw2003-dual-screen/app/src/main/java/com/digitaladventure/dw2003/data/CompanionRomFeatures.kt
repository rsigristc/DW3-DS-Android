package com.digitaladventure.dw2003.data

/**
 * Companion features that depend on the disc, not on the current map.
 * Fast travel and the in-game walkthrough are Flawe / PAL 2003 modules.
 *
 * The USA save block sits `0x84C` bytes below PAL: GameShark bits are
 * `80048554` and Kotemon HP is `80048C70`, versus PAL `80048DA0` / `800494BC`.
 */
data class CompanionRomFeatures(
    val supportsWalkthrough: Boolean = true,
    val supportsFastTravel: Boolean = true,
    val detectPalLanguage: Boolean = true,
    val ramBase: Int = PAL_RAM_BASE
) {
    companion object {
        const val PAL_RAM_BASE = 0x48D00
        const val USA_RAM_SHIFT = -0x84C
        const val USA_RAM_BASE = PAL_RAM_BASE + USA_RAM_SHIFT

        val PAL = CompanionRomFeatures()
        val USA = CompanionRomFeatures(
            supportsWalkthrough = false,
            supportsFastTravel = false,
            detectPalLanguage = false,
            ramBase = USA_RAM_BASE
        )
    }
}
