package com.digitaladventure.dw2003.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class VirtualPadMathTest {
    @Test
    fun supportsDeadZoneCardinalsAndDiagonals() {
        assertEquals(emptySet<PadDirection>(), VirtualPadMath.dpadDirections(2f, -2f, 5f))
        assertEquals(setOf(PadDirection.RIGHT), VirtualPadMath.dpadDirections(20f, 1f, 5f))
        assertEquals(
            setOf(PadDirection.LEFT, PadDirection.UP),
            VirtualPadMath.dpadDirections(-20f, -18f, 5f)
        )
    }

    @Test
    fun capturedDpadKeepsWalkingOutsideItsDrawnCircle() {
        assertEquals(
            null,
            VirtualPadMath.dpadDirectionsForPointer(120f, 0f, 5f, 50f, captured = false)
        )
        assertEquals(
            setOf(PadDirection.RIGHT),
            VirtualPadMath.dpadDirectionsForPointer(120f, 0f, 5f, 50f, captured = true)
        )
    }
}
