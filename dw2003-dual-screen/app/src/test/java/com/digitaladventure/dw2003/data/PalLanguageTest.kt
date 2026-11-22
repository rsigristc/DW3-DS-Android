package com.digitaladventure.dw2003.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PalLanguageTest {
    @Test
    fun retainsStockPalAndUsaMeaningsWithoutHarmonySignatures() {
        assertEquals(CompanionLanguage.ENGLISH, PalLanguage.companionLanguage(PalLanguage.ENGLISH))
        assertEquals(CompanionLanguage.ENGLISH, PalLanguage.companionLanguage(PalLanguage.US_ENGLISH))
        assertEquals(CompanionLanguage.SPANISH, PalLanguage.companionLanguage(PalLanguage.SPANISH))
        assertNull(PalLanguage.companionLanguage(PalLanguage.JAPANESE))
        assertEquals(CompanionLanguage.FRENCH, PalLanguage.companionLanguage(PalLanguage.FRENCH))
        assertEquals(CompanionLanguage.ITALIAN, PalLanguage.companionLanguage(PalLanguage.ITALIAN))
        assertEquals(CompanionLanguage.GERMAN, PalLanguage.companionLanguage(PalLanguage.GERMAN))
        assertEquals(0x5CCA8, PalLanguage.ADDRESS)
        assertEquals(0x8005CCA8L, 0x80000000L or PalLanguage.ADDRESS.toLong())
        assertEquals(PalLanguage.ADDRESS, GameStateReader.PAL_LANGUAGE)
    }

    @Test
    fun mapsAllSevenHarmonyBanksAfterVerifyingExecutableHooks() {
        val hooks = mutableMapOf(
            0x134E8 to byteArrayOf(0x8c.toByte(), 0xf5.toByte(), 0x02, 0x08),
            0x1A610 to byteArrayOf(0x88.toByte(), 0x42, 0x00, 0x08),
            0x1D370 to byteArrayOf(0x7d, 0x42, 0x00, 0x08)
        )
        val read = { offset: Int, length: Int -> hooks[offset] ?: ByteArray(length) }
        assertTrue(PalLanguage.hasHarmonyLanguageBanks(read))
        assertEquals(
            listOf("zh", "pt", "en", "fr", "it", "de", "es"),
            (0..6).map { PalLanguage.companionLanguage(it, true)?.guideKey }
        )
        hooks.remove(0x1D370)
        assertFalse(PalLanguage.hasHarmonyLanguageBanks(read))
        assertNull(PalLanguage.companionLanguage(7, true))
    }
}
