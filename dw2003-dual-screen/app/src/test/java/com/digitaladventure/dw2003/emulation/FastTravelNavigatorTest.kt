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
    fun opensMapByResettingUnknownTabToItemsThenDownToMap() {
        val expected = List(4) { RetroPadButton.DPAD_UP } +
            List(2) { RetroPadButton.DPAD_DOWN } +
            RetroPadButton.CROSS
        assertEquals(expected, FastTravelNavigator.stepsToMapFromUnknown().map { it.button })
        assertEquals(
            FastTravelNavigator.stepsToMapFromUnknown(),
            FastTravelNavigator.selectMapFromItems()
        )
        assertEquals(
            listOf(RetroPadButton.SQUARE),
            FastTravelNavigator.switchServer().map { it.button }
        )
    }

    @Test
    fun walksToMapTabWithoutWrappingPastStatus() {
        assertEquals(
            listOf(RetroPadButton.CROSS),
            FastTravelNavigator.stepsToMapTab(FastTravelNavigator.TAB_MAP).map { it.button }
        )
        assertEquals(
            listOf(RetroPadButton.DPAD_DOWN, RetroPadButton.CROSS),
            FastTravelNavigator.stepsToMapTab(FastTravelNavigator.TAB_SORT).map { it.button }
        )
        assertEquals(
            listOf(RetroPadButton.DPAD_UP, RetroPadButton.DPAD_UP, RetroPadButton.CROSS),
            FastTravelNavigator.stepsToMapTab(FastTravelNavigator.TAB_STATUS).map { it.button }
        )
        assertEquals(
            listOf(RetroPadButton.DPAD_UP, RetroPadButton.CROSS),
            FastTravelNavigator.stepsToMapTab(FastTravelNavigator.TAB_TECHNIQUES).map { it.button }
        )
    }

    @Test
    fun movesDownFromAsukaToPark() {
        assertEquals(
            listOf(RetroPadButton.DPAD_DOWN),
            FastTravelNavigator.stepsToFlaweIcon(0x0200, 0x021D, listOf(0x0200, 0x021D)).map { it.button }
        )
    }

    @Test
    fun movesUpFromParkToAsuka() {
        assertEquals(
            listOf(RetroPadButton.DPAD_UP),
            FastTravelNavigator.stepsToFlaweIcon(0x021D, 0x0200, listOf(0x0200, 0x021D)).map { it.button }
        )
    }

    @Test
    fun confirmsDestinationAndFullyExitsMapMenu() {
        assertEquals(
            listOf(RetroPadButton.CROSS, RetroPadButton.TRIANGLE, RetroPadButton.TRIANGLE),
            FastTravelNavigator.confirmMapDestination().map { it.button }
        )
    }
}
