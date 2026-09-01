package com.digitaladventure.dw2003.emulation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FastTravelNavigatorTest {
    @Test
    fun statusMenuUsesOverlaySignatureNotAreaWord() {
        assertTrue(FastTravelNavigator.isStatusMenu(0x8008428CL, 0x0202, 0x0202))
        assertTrue(FastTravelNavigator.isStatusMenu(0L, 0x1000, 0x0200))
        assertTrue(!FastTravelNavigator.isStatusMenu(0L, 0x0202, 0x0202))
    }

    @Test
    fun findsAlignedTabIndexAfterR1() {
        val before = ByteArray(8)
        val after = ByteArray(8)
        before[2] = 0
        after[2] = 1
        val cursor = FastTravelNavigator.findTabCursor(before, after)
        assertEquals(2, cursor?.offset)
        assertEquals(1, cursor?.index)
        assertEquals(true, cursor?.r1Increases)
    }

    @Test
    fun ignoresAmbiguousTabCandidates() {
        val before = ByteArray(8)
        val after = ByteArray(8)
        before[0] = 0
        after[0] = 1
        before[4] = 2
        after[4] = 3
        assertNull(FastTravelNavigator.findTabCursor(before, after))
    }

    @Test
    fun stepsFromSortReachMapWithOneR1() {
        val steps = FastTravelNavigator.stepsTowardMap(1, r1Increases = true)
        assertEquals(listOf(RetroPadButton.R1), steps.map { it.button })
    }

    @Test
    fun stepsFromMapDoNothing() {
        assertTrue(FastTravelNavigator.stepsTowardMap(2, r1Increases = true).isEmpty())
    }

    @Test
    fun fallbackAfterProbeIsOneMoreR1() {
        assertEquals(listOf(RetroPadButton.R1), FastTravelNavigator.fallbackAfterProbe().map { it.button })
    }

    @Test
    fun confirmsDestinationWithCrossThenTriangle() {
        assertEquals(
            listOf(RetroPadButton.CROSS, RetroPadButton.TRIANGLE),
            FastTravelNavigator.confirmMapDestination().map { it.button }
        )
    }
}
