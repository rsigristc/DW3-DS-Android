package com.digitaladventure.dw2003.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FastTravelCatalogTest {
    @Test
    fun unlocksOnlyVisitedFlaweIcons() {
        val asuka = FastTravelCatalog.groups(99, setOf(0x0200, 0x0206), 0x0206)
        assertTrue(asuka.any { group -> group.destinations.any { it.areaId == 0x0200 } })
        assertFalse(asuka.any { group -> group.destinations.any { it.areaId == 0x0206 } })
        assertFalse(asuka.any { group -> group.destinations.any { it.areaId == 0x0202 } })
        assertFalse(asuka.any { group -> group.destinations.any { it.areaId == 0x021D } })
        assertFalse(asuka.any { group -> group.destinations.any { it.areaId == 0x0780 } })
    }

    @Test
    fun bridgeVisitsCountAsAsukaCityNotASeparateIcon() {
        assertEquals(0x0200, FastTravelCatalog.iconId(0x0202))
        assertEquals(0x0200, FastTravelCatalog.iconId(0x0203, 0x0203))
        val groups = FastTravelCatalog.groups(99, setOf(0x0202, 0x021D), 0x0202)
        assertTrue(groups.any { group -> group.destinations.any { it.areaId == 0x0200 } })
        assertTrue(groups.any { group -> group.destinations.any { it.areaId == 0x021D } })
        assertFalse(groups.any { group -> group.destinations.any { it.areaId == 0x0202 } })
    }

    @Test
    fun unlocksVisitedParkIcon() {
        val groups = FastTravelCatalog.groups(99, setOf(0x0200, 0x021D), 0x0200)
        assertTrue(groups.any { group -> group.destinations.any { it.areaId == 0x021D } })
        assertTrue(groups.any { group -> group.destinations.any { it.areaId == 0x0200 } })
    }

    @Test
    fun includesCurrentHubEvenIfVisitWasNotPersisted() {
        val groups = FastTravelCatalog.groups(1, emptySet(), 0x0200)
        assertTrue(groups.any { group -> group.destinations.any { it.areaId == 0x0200 } })
        assertFalse(groups.any { group -> group.destinations.any { it.areaId == 0x0206 } })
    }

    @Test
    fun keepsUnknownServersLocked() {
        val destination = FastTravelDestination(
            areaId = 0x1000,
            name = "Menú",
            server = ServerRegion.UNKNOWN,
            sector = SectorRegion.UNKNOWN
        )
        assertFalse(FastTravelCatalog.isUnlocked(destination, 99, emptySet(), 0x0200))
        assertFalse(FastTravelCatalog.isUnlocked(destination, 99, setOf(0x1000), 0x1000))
    }
}
