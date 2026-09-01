package com.digitaladventure.dw2003.emulation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FastTravelNavigatorTest {
    @Test
    fun startThenTwoR1ReachMapTabFromItems() {
        assertEquals(listOf(RetroPadButton.START), FastTravelNavigator.pressStart().map { it.button })
        assertEquals(
            listOf(RetroPadButton.R1, RetroPadButton.R1),
            FastTravelNavigator.moveToMapTab().map { it.button }
        )
    }

    @Test
    fun closesMenuWithTriangleOnly() {
        assertEquals(
            listOf(RetroPadButton.TRIANGLE),
            FastTravelNavigator.closeMenu().map { it.button }
        )
        assertFalse(FastTravelNavigator.closeMenu().any { it.button == RetroPadButton.CROSS })
    }

    @Test
    fun detectsMenuOverlay() {
        assertTrue(FastTravelNavigator.isMenuOverlay(0x1000, 0x0200))
        assertTrue(FastTravelNavigator.isMenuOverlay(0x0200, 0x1000))
        assertFalse(FastTravelNavigator.isMenuOverlay(0x0200, 0x0200))
    }
}
