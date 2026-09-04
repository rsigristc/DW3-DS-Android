package com.digitaladventure.dw2003.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PalLanguageTest {
    @Test
    fun mapsEuropeanEnglishAndSpanishAndIgnoresOtherCodes() {
        assertEquals(CompanionLanguage.ENGLISH, PalLanguage.companionLanguage(PalLanguage.ENGLISH))
        assertEquals(CompanionLanguage.ENGLISH, PalLanguage.companionLanguage(PalLanguage.US_ENGLISH))
        assertEquals(CompanionLanguage.SPANISH, PalLanguage.companionLanguage(PalLanguage.SPANISH))
        assertNull(PalLanguage.companionLanguage(PalLanguage.JAPANESE))
        assertNull(PalLanguage.companionLanguage(PalLanguage.FRENCH))
        assertNull(PalLanguage.companionLanguage(PalLanguage.ITALIAN))
        assertNull(PalLanguage.companionLanguage(PalLanguage.GERMAN))
        assertEquals(0x5CCA8, PalLanguage.ADDRESS)
        assertEquals(0x8005CCA8L, 0x80000000L or PalLanguage.ADDRESS.toLong())
        assertEquals(PalLanguage.ADDRESS, GameStateReader.PAL_LANGUAGE)
    }
}
