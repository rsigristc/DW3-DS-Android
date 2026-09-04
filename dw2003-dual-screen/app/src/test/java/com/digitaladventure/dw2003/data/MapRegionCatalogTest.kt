package com.digitaladventure.dw2003.data

import org.junit.Assert.assertEquals
import org.junit.Test

class MapRegionCatalogTest {
    @Test fun followsWikimonAsukaSectorLists() {
        assertEquals(MapRegion(ServerRegion.ASUKA, SectorRegion.CENTRAL), MapRegionCatalog.resolve(0x0200))
        assertEquals(MapRegion(ServerRegion.ASUKA, SectorRegion.CENTRAL), MapRegionCatalog.resolve(0x021D))
        assertEquals(MapRegion(ServerRegion.ASUKA, SectorRegion.CENTRAL), MapRegionCatalog.resolve(0x021E))
        assertEquals(MapRegion(ServerRegion.ASUKA, SectorRegion.CENTRAL), MapRegionCatalog.resolve(0x021F))
        assertEquals(MapRegion(ServerRegion.ASUKA, SectorRegion.CENTRAL), MapRegionCatalog.resolve(0x0220))
        assertEquals(MapRegion(ServerRegion.ASUKA, SectorRegion.EAST), MapRegionCatalog.resolve(0x0221))
        assertEquals(MapRegion(ServerRegion.ASUKA, SectorRegion.EAST), MapRegionCatalog.resolve(0x0227))
        assertEquals(MapRegion(ServerRegion.ASUKA, SectorRegion.EAST), MapRegionCatalog.resolve(0x0228))
        assertEquals(MapRegion(ServerRegion.ASUKA, SectorRegion.EAST), MapRegionCatalog.resolve(0x0229))
        assertEquals(MapRegion(ServerRegion.ASUKA, SectorRegion.EAST), MapRegionCatalog.resolve(0x022E))
        assertEquals(MapRegion(ServerRegion.ASUKA, SectorRegion.SOUTH), MapRegionCatalog.resolve(0x0232))
        assertEquals(MapRegion(ServerRegion.ASUKA, SectorRegion.WEST), MapRegionCatalog.resolve(0x025D))
        assertEquals(MapRegion(ServerRegion.ASUKA, SectorRegion.NORTH), MapRegionCatalog.resolve(0x0261))
        assertEquals(MapRegion(ServerRegion.AMATERASU, SectorRegion.WEST), MapRegionCatalog.resolve(0x084A))
    }

    @Test fun leavesMenusOutsideTheWorldMap() {
        assertEquals(MapRegion(ServerRegion.UNKNOWN, SectorRegion.UNKNOWN), MapRegionCatalog.resolve(0x1000))
    }
}
