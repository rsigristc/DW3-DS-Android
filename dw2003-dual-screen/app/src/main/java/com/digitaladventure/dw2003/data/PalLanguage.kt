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
 * Harmony's seven-language build replaces only banks 0 and 1.
 * Zero stays unset on stock PAL because it is also the initial RAM value.
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

    fun companionLanguage(code: Int, harmonySevenLanguages: Boolean = false): CompanionLanguage? = when (code) {
        JAPANESE -> CompanionLanguage.CHINESE.takeIf { harmonySevenLanguages }
        US_ENGLISH -> if (harmonySevenLanguages) CompanionLanguage.PORTUGUESE else CompanionLanguage.ENGLISH
        SPANISH -> CompanionLanguage.SPANISH
        ENGLISH -> CompanionLanguage.ENGLISH
        FRENCH -> CompanionLanguage.FRENCH
        ITALIAN -> CompanionLanguage.ITALIAN
        GERMAN -> CompanionLanguage.GERMAN
        else -> null
    }

    /** The Harmony partner hook and the Chinese wide-glyph renderer hooks. */
    fun hasHarmonyLanguageBanks(read: (Int, Int) -> ByteArray): Boolean =
        GameStateReader.u32(read(0x134E8, 4), 0) == 0x0802F58CL &&
            GameStateReader.u32(read(0x1A610, 4), 0) == 0x08004288L &&
            GameStateReader.u32(read(0x1D370, 4), 0) == 0x0800427DL
}
