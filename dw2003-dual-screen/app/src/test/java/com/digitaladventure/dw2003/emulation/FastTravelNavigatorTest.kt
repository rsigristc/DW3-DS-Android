package com.digitaladventure.dw2003.emulation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FastTravelNavigatorTest {
    @Test
    fun opensStatusMenuThenMovesLeftToMap() {
        val buttons = FastTravelNavigator.openMap(menuAlreadyOpen = false).map { it.button }
        assertEquals(
            listOf(RetroPadButton.START, RetroPadButton.L1, RetroPadButton.L1),
            buttons
        )
    }

    @Test
    fun closesExistingMenuBeforeOpeningMap() {
        val buttons = FastTravelNavigator.openMap(menuAlreadyOpen = true).map { it.button }
        assertEquals(
            listOf(
                RetroPadButton.TRIANGLE,
                RetroPadButton.START,
                RetroPadButton.L1,
                RetroPadButton.L1
            ),
            buttons
        )
    }

    @Test
    fun confirmsWithCrossAndExitsWithTriangle() {
        val buttons = FastTravelNavigator.commitSelectionAndExit().map { it.button }
        assertEquals(listOf(RetroPadButton.CROSS, RetroPadButton.TRIANGLE), buttons)
    }

    @Test
    fun detectsMenuOverlay() {
        assertTrue(FastTravelNavigator.isMenuOverlay(0x1000, 0x0200))
        assertTrue(FastTravelNavigator.isMenuOverlay(0x0200, 0x1000))
        assertFalse(FastTravelNavigator.isMenuOverlay(0x0200, 0x0200))
    }
}
