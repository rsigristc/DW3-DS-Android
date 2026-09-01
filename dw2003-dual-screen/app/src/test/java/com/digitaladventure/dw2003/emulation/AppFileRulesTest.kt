package com.digitaladventure.dw2003.emulation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppFileRulesTest {
    @Test
    fun acceptsExpectedPlayStationFileFormats() {
        assertTrue(AppFileRules.isValidBiosSize(512L * 1024L))
        assertFalse(AppFileRules.isValidBiosSize(256L * 1024L))
        assertTrue(AppFileRules.isValidMemoryCardSize(128L * 1024L))
        assertFalse(AppFileRules.isValidMemoryCardSize(128L * 1024L + 64L))
        assertTrue(AppFileRules.memoryCardPayloadOffset(128L * 1024L) == 0)
        assertTrue(AppFileRules.memoryCardPayloadOffset(128L * 1024L + 64L) == 64)
        assertTrue(AppFileRules.memoryCardPayloadOffset(128L * 1024L + 3904L) == 3904)
        assertTrue(AppFileRules.memoryCardPayloadOffset(123L) == null)
        assertTrue(AppFileRules.hasMemoryCardSignature(MemoryCardFactory.createFormatted()))
        assertFalse(AppFileRules.hasMemoryCardSignature(ByteArray(128 * 1024)))
    }

    @Test
    fun createsFormattedBlankMemoryCard() {
        val card = MemoryCardFactory.createFormatted()
        assertTrue(card.size == 128 * 1024)
        assertTrue(card[0] == 'M'.code.toByte())
        assertTrue(card[1] == 'C'.code.toByte())
        assertTrue(card[0x7F] == 0x0E.toByte())
        assertTrue(card[0x80] == 0xA0.toByte())
        assertTrue(card[0xFF] == 0xA0.toByte())
    }

    @Test
    fun recognizesEuropeanPlayStationBiosMarker() {
        val bios = ByteArray(AppFileRules.PLAYSTATION_BIOS_SIZE.toInt())
        bios[1] = 0x00
        bios[2] = 0x08
        bios[3] = 0x3C
        bios[4] = 0x3F
        bios[0x7FF51] = ' '.code.toByte()
        bios[0x7FF52] = 'E'.code.toByte()

        assertTrue(AppFileRules.isEuropeanPlayStationBios(bios))
        bios[0x7FF52] = 'J'.code.toByte()
        assertFalse(AppFileRules.isEuropeanPlayStationBios(bios))
    }
}
