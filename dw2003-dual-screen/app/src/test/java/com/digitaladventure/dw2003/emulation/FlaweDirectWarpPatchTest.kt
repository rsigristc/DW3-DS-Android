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
        assertTrue(FlaweDirectWarpPatch.matchesPreferred(dispatcherWindow()))
        assertTrue(!FlaweDirectWarpPatch.matchesPreferred(ByteArray(FlaweDirectWarpPatch.WINDOW_SIZE)))
    }

    @Test
    fun acceptsUniqueDispatcherWithoutJumpThunk() {
        val ram = ByteArray(0x8000)
        val offset = 0x2000
        dispatcherWindow().copyInto(ram, offset)

        assertEquals(listOf(offset), FlaweDirectWarpPatch.findActiveDispatcherOffsets(ram))
        val site = FlaweDirectWarpPatch.selectActiveSite(ram)!!
        val window = ram.copyOfRange(site.ramOffset, site.ramOffset + site.windowSize)
        assertEquals(
            0x3403000FL,
            GameStateReader.u32(
                FlaweDirectWarpPatch.prepare(window, 0x022E, site.copy(ramOffset = 0))!!,
                site.iconLoadOffset
            )
        )
    }

    @Test
    fun refusesRendererLoadsWithoutAKnownWarpFunction() {
        val ram = ByteArray(0x4000)
        val branchAt = 0x1100
        val loadAt = 0x1130
        GameMemoryController.writeU32(ram, branchAt, 0x14680010L)
        GameMemoryController.writeU32(ram, loadAt, 0x8E230184L)

        assertNull(FlaweDirectWarpPatch.selectActiveSite(ram))
        assertTrue(!FlaweDirectWarpPatch.matchesNearby(ram.copyOfRange(0x1100, 0x1140)))
    }

    @Test
    fun supportsActualFlawe2FunctionCapturedFromDevice() {
        val original = v2Window()
        assertTrue(FlaweDirectWarpPatch.matchesV2(original))
        val patched = FlaweDirectWarpPatch.prepare(original, 0x229, FlaweDirectWarpPatch.v2Site(0))!!
        assertEquals(0L, GameStateReader.u32(patched, 8))
        assertEquals(0x34070018L, GameStateReader.u32(patched, 0x38))
        assertTrue(original.contentEquals(v2Window()))
        // The server-availability check must remain intact.
        assertEquals(GameStateReader.u32(original, 0x24), GameStateReader.u32(patched, 0x24))
        original[0x14] = 0
        assertNull(FlaweDirectWarpPatch.prepare(original, 0x229, FlaweDirectWarpPatch.v2Site(0)))
    }

    @Test
    fun selectsReferencedV2CopyAndFindsShortWindowAtEndOfRam() {
        val ram = ByteArray(0x4000 + FlaweDirectWarpPatch.V2_WINDOW_SIZE)
        v2Window().copyInto(ram, 0x1000)
        v2Window().copyInto(ram, 0x4000)
        assertNull(FlaweDirectWarpPatch.selectActiveSite(ram))
        GameMemoryController.writeU32(ram, 0, 0x0C001000L) // jal 0x80004000
        assertEquals(0x4000, FlaweDirectWarpPatch.selectActiveSite(ram)!!.ramOffset)
    }

    private fun v2Window(): ByteArray =
        ("8001828c000000004c004010000000000180023c14b2428cd0ffbd272800b0af" +
            "2c00bfaf3c00401025808000020042380100422c0a80033c8401078e")
            .chunked(2).map { it.toInt(16).toByte() }.toByteArray()

    @Test
    fun refusesAmbiguousLooseCopies() {
        val ram = ByteArray(0x4000)
        GameMemoryController.writeU32(ram, 0x1000, 0x14680010L)
        GameMemoryController.writeU32(ram, 0x1030, 0x8E230184L)
        GameMemoryController.writeU32(ram, 0x2000, 0x14680010L)
        GameMemoryController.writeU32(ram, 0x2030, 0x8E230184L)

        assertTrue(FlaweDirectWarpPatch.findActiveDispatcherOffsets(ram).isEmpty())
        assertNull(FlaweDirectWarpPatch.selectActiveSite(ram))
    }

    private fun dispatcherWindow(
        branch: Long = 0x14680271L
    ) = ByteArray(FlaweDirectWarpPatch.WINDOW_SIZE).also {
        GameMemoryController.writeU32(it, 0x00, 0x8E230180L)
        GameMemoryController.writeU32(it, 0x0C, branch)
        GameMemoryController.writeU32(it, 0x4C, 0x8E230184L)
    }
}
