package com.digitaladventure.dw2003.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FastTravelCatalogTest {
    @Test
    fun unlocksVisitedMapsAndEarlyAsukaHub() {
        val asuka = FastTravelCatalog.groups(4, setOf(0x0206), 0x0206)
        assertTrue(asuka.any { group -> group.destinations.any { it.areaId == 0x0206 } })
        assertTrue(asuka.any { group -> group.destinations.any { it.areaId == 0x0200 } })
        assertFalse(asuka.any { group -> group.destinations.any { it.areaId == 0x0780 } })
    }

    @Test
    fun keepsUnknownServersLocked() {
        val destination = FastTravelDestination(
            areaId = 0x1000,
            name = "Menú",
            server = ServerRegion.UNKNOWN,
            sector = SectorRegion.UNKNOWN,
            minStory = 1
        )
        assertFalse(FastTravelCatalog.isUnlocked(destination, 99, emptySet(), 0x0200))
    }
}
