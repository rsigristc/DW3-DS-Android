package com.digitaladventure.dw2003.data

/**
 * Companion features that depend on the disc, not on the current map.
 * Fast travel and the in-game walkthrough are Flawe / PAL 2003 modules.
 */
data class CompanionRomFeatures(
    val supportsWalkthrough: Boolean = true,
    val supportsFastTravel: Boolean = true,
    val detectPalLanguage: Boolean = true
) {
    companion object {
        val PAL = CompanionRomFeatures()
        val USA = CompanionRomFeatures(
            supportsWalkthrough = false,
            supportsFastTravel = false,
            detectPalLanguage = false
        )
    }
}
