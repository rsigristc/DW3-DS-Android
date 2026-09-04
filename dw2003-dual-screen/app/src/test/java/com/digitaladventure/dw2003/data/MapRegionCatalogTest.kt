package com.digitaladventure.dw2003.data

import org.junit.Assert.assertEquals
import org.junit.Test

class MapRegionCatalogTest {
    @Test fun resolvesAsukaAndAmaterasuBanks() {
        assertEquals(MapRegion(ServerRegion.ASUKA, SectorRegion.EAST), MapRegionCatalog.resolve(0x022A))
        assertEquals(MapRegion(ServerRegion.ASUKA, SectorRegion.WEST), MapRegionCatalog.resolve(0x025D))
        assertEquals(MapRegion(ServerRegion.ASUKA, SectorRegion.NORTH), MapRegionCatalog.resolve(0x0261))
        assertEquals(MapRegion(ServerRegion.AMATERASU, SectorRegion.WEST), MapRegionCatalog.resolve(0x084A))
    }

    @Test fun leavesMenusOutsideTheWorldMap() {
        assertEquals(MapRegion(ServerRegion.UNKNOWN, SectorRegion.UNKNOWN), MapRegionCatalog.resolve(0x1000))
    }
}
