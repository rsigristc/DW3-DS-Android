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
    fun forcesOfficialAskmapIconsFromThePatcherTable() {
        val beach = FlaweDirectWarpPatch.prepare(dispatcherWindow(), 0x021F)!!
        val gym = FlaweDirectWarpPatch.prepare(dispatcherWindow(), 0x0267)!!

        assertEquals(0x34030021L, GameStateReader.u32(beach, 0x4C))
        assertEquals(0x34030001L, GameStateReader.u32(gym, 0x4C))
    }

    @Test
    fun findsRelocatedDispatcherReferencedByMapOverlay() {
        val ram = ByteArray(0x10000)
        val sourceOffset = 0x3000
        val runtimeOffset = 0x6000
        dispatcherWindow().copyInto(ram, sourceOffset)
        dispatcherWindow(branch = 0x14680042L).copyInto(ram, runtimeOffset)
        val jumpToRuntime = 0x08000000L or
            (((runtimeOffset or 0x80000000.toInt()) ushr 2) and 0x03FFFFFF).toLong()
        GameMemoryController.writeU32(ram, 0x100, jumpToRuntime)

        assertEquals(
            listOf(runtimeOffset),
            FlaweDirectWarpPatch.findActiveDispatcherOffsets(ram)
        )
        assertEquals(
            0x34030014L,
            GameStateReader.u32(
                FlaweDirectWarpPatch.prepare(
                    ram.copyOfRange(runtimeOffset, runtimeOffset + FlaweDirectWarpPatch.WINDOW_SIZE),
                    0x0200
                )!!,
                0x4C
            )
        )
    }

    @Test
    fun refusesUnknownVersionsAndDestinations() {
        assertNull(FlaweDirectWarpPatch.prepare(ByteArray(FlaweDirectWarpPatch.WINDOW_SIZE), 0x0200))
        assertNull(FlaweDirectWarpPatch.prepare(dispatcherWindow(), 0x0202))
        assertNull(FlaweDirectWarpPatch.prepare(dispatcherWindow(), 0x0780))
    }

    private fun dispatcherWindow(
        branch: Long = 0x14680271L
    ) = ByteArray(FlaweDirectWarpPatch.WINDOW_SIZE).also {
        GameMemoryController.writeU32(it, 0x00, 0x8E230180L)
        GameMemoryController.writeU32(it, 0x0C, branch)
        GameMemoryController.writeU32(it, 0x4C, 0x8E230184L)
    }
}
