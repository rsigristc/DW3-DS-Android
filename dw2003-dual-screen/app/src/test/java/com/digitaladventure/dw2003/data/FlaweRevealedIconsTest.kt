package com.digitaladventure.dw2003.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FlaweRevealedIconsTest {
    private fun main(): ByteArray = ByteArray(GameStateReader.MAIN_LENGTH)

    @Test
    fun emptySaveYieldsNoIcons() {
        assertTrue(FlaweRevealedIcons.fromMain(main(), 4).isEmpty())
    }

    @Test
    fun readsAsukaBytesAtMapIdOffset() {
        val ram = main()
        listOf(0x0200, 0x021D, 0x0227, 0x0229, 0x022E).forEach { mapId ->
            ram[0x4B000 - GameStateReader.MAIN_BASE + mapId] = 1
        }
        ram[0x4B000 - GameStateReader.MAIN_BASE + 0x0264] = 1
        val icons = FlaweRevealedIcons.fromMain(ram, 4)
        assertTrue(icons.containsAll(setOf(0x0200, 0x021D, 0x0227, 0x0229, 0x022E)))
        assertFalse(icons.contains(0x0264))
        assertFalse(icons.contains(0x0780))
    }

    @Test
    fun readsAskmapIconCodeBits() {
        val ram = main()
        fun setBit(index: Int) {
            val offset = FlaweRevealedIcons.ICON_BITS - GameStateReader.MAIN_BASE + (index ushr 3)
            ram[offset] = (ram[offset].toInt() or (1 shl (index and 7))).toByte()
        }
        setBit(0x14)
        setBit(0x1E)
        setBit(0x0F)
        val icons = FlaweRevealedIcons.fromMain(ram, 4)
        assertEquals(setOf(0x0200, 0x021D, 0x022E), icons)
    }

    @Test
    fun rejectsAFullAtlasAsOverlayGarbage() {
        val ram = main()
        FlaweFastTravelTable.asukaMapIds.forEach { mapId ->
            ram[0x4B000 - GameStateReader.MAIN_BASE + mapId] = 1
        }
        assertTrue(FlaweRevealedIcons.fromMain(ram, 4).isEmpty())
    }
}
