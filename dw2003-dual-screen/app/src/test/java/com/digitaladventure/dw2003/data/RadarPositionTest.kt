package com.digitaladventure.dw2003.data

import org.junit.Assert.*
import org.junit.Test

class RadarPositionTest {
    @Test fun seiryuTowerMarkerFollowsEastSector() {
        val point = RadarPosition.forStage(0x230)!!
        assertTrue(point.x > 0.65f)
        assertEquals(point, RadarPosition.forStage(0x22E))
        assertNotEquals(point, RadarPosition.forStage(0x26F))
    }

    @Test fun unknownLocationsDoNotInventCoordinates() {
        assertNull(RadarPosition.forStage(0))
        assertNull(RadarPosition.forStage(0x1000))
    }
}
