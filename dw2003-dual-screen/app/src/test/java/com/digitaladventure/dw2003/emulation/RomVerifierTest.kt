package com.digitaladventure.dw2003.emulation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream

class RomVerifierTest {
    @Test
    fun identifiesUsaByKnownHash() {
        assertEquals(RomVerifier.Variant.USA, RomVerifier.identify(RomVerifier.USA_SHA1))
        assertEquals("NTSC", RomVerifier.Variant.USA.emulatorRegion)
        assertFalse(RomVerifier.Variant.USA.features.supportsFastTravel)
        assertFalse(RomVerifier.Variant.USA.features.supportsWalkthrough)
        assertEquals(0x484B4, RomVerifier.Variant.USA.features.ramBase)
    }

    @Test
    fun identifiesUsaBySerialWhenHashDiffers() {
        assertEquals(
            RomVerifier.Variant.USA,
            RomVerifier.identify("deadbeef", "BOOT = cdrom:\\SLUS_014.36;1")
        )
        assertEquals(RomVerifier.SERIAL_USA, RomVerifier.normalizeSerial("SLUS-01436"))
    }

    @Test
    fun keepsPalVariantsOnKnownHashes() {
        assertEquals(RomVerifier.Variant.ORIGINAL, RomVerifier.identify(RomVerifier.ORIGINAL_SHA1, "SLES_039.36"))
        assertEquals(RomVerifier.Variant.FLAWES_MOD_2, RomVerifier.identify(RomVerifier.FLAWES_MOD_2_SHA1))
        assertTrue(RomVerifier.Variant.ORIGINAL.features.supportsFastTravel)
        assertEquals("PAL", RomVerifier.Variant.ORIGINAL.emulatorRegion)
    }

    @Test
    fun findsSerialInRawDumpBytes() {
        val dump = ByteArray(64) { 0x20 }
        "SLUS_014.36;1".toByteArray().copyInto(dump, 20)
        assertEquals(RomVerifier.SERIAL_USA, RomVerifier.findSerial(dump))
    }

    @Test
    fun inspectsStreamForUsaSerial() {
        val payload = ByteArray(80) { 0 }
        "cdrom:\\SLUS_014.36;1".toByteArray().copyInto(payload, 8)
        val result = RomVerifier.inspect(ByteArrayInputStream(payload))
        assertEquals(RomVerifier.Variant.USA, result.variant)
        assertEquals(RomVerifier.SERIAL_USA, result.serial)
    }

    @Test
    fun restoresStoredUsaLabelOrName() {
        assertEquals(RomVerifier.Variant.USA, RomVerifier.Variant.fromStored("USA", null))
        assertEquals(
            RomVerifier.Variant.USA,
            RomVerifier.Variant.fromStored("Digimon World 3 USA", null)
        )
        assertEquals(
            RomVerifier.Variant.USA,
            RomVerifier.Variant.fromStored(null, RomVerifier.USA_SHA1)
        )
        assertEquals(
            RomVerifier.Variant.USA,
            RomVerifier.Variant.fromStored("Imagen no verificada", RomVerifier.USA_SHA1)
        )
    }
}
