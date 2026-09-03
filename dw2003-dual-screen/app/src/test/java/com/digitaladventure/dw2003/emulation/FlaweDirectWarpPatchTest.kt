package com.digitaladventure.dw2003.emulation

import com.digitaladventure.dw2003.data.GameStateReader
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FlaweDirectWarpPatchTest {
    @Test
    fun forcesAsukaIconThroughValidatedFlaweDispatcher() {
        val original = dispatcherWindow()

        val patched = FlaweDirectWarpPatch.prepare(original, 0x0200)!!

        assertEquals(0L, GameStateReader.u32(patched, 0x0C))
        assertEquals(0x34030014L, GameStateReader.u32(patched, 0x4C))
        assertEquals(0x8E230180L, GameStateReader.u32(original, 0))
        assertTrue(original.contentEquals(dispatcherWindow()))
    }

    @Test
    fun forcesCentralParkIcon() {
        val patched = FlaweDirectWarpPatch.prepare(dispatcherWindow(), 0x021D)!!

        assertEquals(0x3403001EL, GameStateReader.u32(patched, 0x4C))
    }

    @Test
    fun refusesUnknownVersionsAndDestinations() {
        assertNull(FlaweDirectWarpPatch.prepare(ByteArray(FlaweDirectWarpPatch.WINDOW_SIZE), 0x0200))
        assertNull(FlaweDirectWarpPatch.prepare(dispatcherWindow(), 0x0202))
    }

    private fun dispatcherWindow() = ByteArray(FlaweDirectWarpPatch.WINDOW_SIZE).also {
        GameMemoryController.writeU32(it, 0x00, 0x8E230180L)
        GameMemoryController.writeU32(it, 0x0C, 0x14680271L)
        GameMemoryController.writeU32(it, 0x4C, 0x8E230184L)
    }
}
