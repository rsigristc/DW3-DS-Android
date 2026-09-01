package com.digitaladventure.dw2003.emulation

import org.junit.Assert.assertEquals
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
    fun fallbackFromItemsIsDownThenCross() {
        assertEquals(
            listOf(RetroPadButton.DPAD_DOWN, RetroPadButton.CROSS),
            FastTravelNavigator.fallbackFromItemsToMap().map { it.button }
        )
    }

    @Test
    fun stepsFromSortReachMapWithOneDownThenCross() {
        assertEquals(
            listOf(RetroPadButton.DPAD_DOWN, RetroPadButton.CROSS),
            FastTravelNavigator.stepsTowardMap(1, downIncreases = true).map { it.button }
        )
    }

    @Test
    fun stepsFromMapOnlyPressCross() {
        assertEquals(
            listOf(RetroPadButton.CROSS),
            FastTravelNavigator.stepsTowardMap(2, downIncreases = true).map { it.button }
        )
    }

    @Test
    fun cyclesAsukaToParkWithR1() {
        assertEquals(
            listOf(RetroPadButton.R1),
            FastTravelNavigator.stepsToFlaweIcon(0x0200, 0x021D, listOf(0x0200, 0x021D)).map { it.button }
        )
    }

    @Test
    fun cyclesParkToAsukaByWrappingR1() {
        assertEquals(
            listOf(RetroPadButton.R1),
            FastTravelNavigator.stepsToFlaweIcon(0x021D, 0x0200, listOf(0x0200, 0x021D)).map { it.button }
        )
    }

    @Test
    fun confirmsDestinationWithCrossThenTriangle() {
        assertEquals(
            listOf(RetroPadButton.CROSS, RetroPadButton.TRIANGLE),
            FastTravelNavigator.confirmMapDestination().map { it.button }
        )
    }
}
