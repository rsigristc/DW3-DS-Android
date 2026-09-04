package com.digitaladventure.dw2003.data

/**
 * PAL language index at `0x8005CCA8`, confirmed by ddw3 (`D0x8005cca8`,
 * default `2`) and [dmw_2003_japanese](https://github.com/markisha64/dmw_2003_japanese)
 * (writes `0` to skip the language screen).
 *
 * Disc LBA prefixes are MS/US/ES/FS/IS/DS/SS, so the runtime word is:
 * 0 Japanese (also a zeroed word before EXE init), 1 US English,
 * 2 European English, 3 French, 4 Italian, 5 German, 6 Spanish.
 *
 * AUTO only maps 1/2 → English and 6 → Spanish. Zero stays unset so a
 * Spanish session is not flipped by boot RAM, and Flawe's English-only
 * walkthrough cannot override a Spanish disc language.
 */
object PalLanguage {
    /** `RETRO_MEMORY_SYSTEM_RAM` offset of virtual `0x8005CCA8`. */
    const val ADDRESS = 0x5CCA8
    const val JAPANESE = 0
    const val US_ENGLISH = 1
    const val ENGLISH = 2
    const val FRENCH = 3
    const val ITALIAN = 4
    const val GERMAN = 5
    const val SPANISH = 6

    fun companionLanguage(code: Int): CompanionLanguage? = when (code) {
        SPANISH -> CompanionLanguage.SPANISH
        US_ENGLISH, ENGLISH -> CompanionLanguage.ENGLISH
        else -> null
    }
}
