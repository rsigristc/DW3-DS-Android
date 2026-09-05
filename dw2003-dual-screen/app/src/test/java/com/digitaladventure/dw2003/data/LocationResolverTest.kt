package com.digitaladventure.dw2003.data

import com.digitaladventure.dw2003.model.GameMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LocationResolverTest {
    @Test
    fun allowsTravelFromStartMenuButNotBattlesOrSaveEvents() {
        assertTrue(LocationResolver.canFastTravel(0x230, 0x1000, GameMode.MANAGEMENT, true))
        assertFalse(LocationResolver.canFastTravel(0x230, 0x1000, GameMode.BATTLE, true))
        assertFalse(LocationResolver.canFastTravel(0x0C01, 0x1000, GameMode.MANAGEMENT, true))
    }

    @Test
    fun indoorRoomUsesCurrentMapIdNotStaleCityArea() {
        val location = LocationResolver.resolve(0x0200, 0x0206)

        assertEquals("Laboratorio Digimon", location.title)
        assertEquals("Sector Central · Laboratorio Digimon", location.radarLabel)
        assertEquals(0x0206, location.publicMapId)
        assertFalse(location.title.contains("Asuka", ignoreCase = true))
    }

    @Test
    fun innBannerUsesCurrentMapId() {
        val location = LocationResolver.resolve(0x0200, 0x020A)

        assertEquals("Posada Asuka 1P", location.title)
        assertEquals(0x020A, location.publicMapId)
    }

    @Test
    fun saveOverlayKeepsInnAsTitle() {
        val location = LocationResolver.resolve(0x020A, 0x0C01)

        assertEquals("Posada Asuka 1P", location.title)
        assertEquals("Pantalla de guardado", location.mapLabel)
    }

    @Test
    fun divermonLakeUsesEastSectorMap() {
        val location = LocationResolver.resolve(0x0227, 0x0227)
        assertEquals("Lago de Divermon", location.title)
        assertEquals("Sector Este · Lago de Divermon", location.radarLabel)
    }

    @Test
    fun enteringWireForestUsesDestinationMapId() {
        val location = LocationResolver.resolve(0x021D, 0x021E)

        assertEquals("Entrada del Bosque Alambre", location.title)
        assertEquals("Sector Central · Entrada del Bosque Alambre", location.radarLabel)
        assertEquals(0x021E, location.publicMapId)
    }

    @Test
    fun bridgeUsesMapIdWhenAreaStaysOnCityHub() {
        val location = LocationResolver.resolve(0x0200, 0x0202)

        assertEquals("Puente Asuka", location.title)
        assertEquals("Sector Central · Puente Asuka", location.radarLabel)
        assertEquals(0x0202, location.publicMapId)
        assertFalse(location.title.contains("Ciudad", ignoreCase = true))
    }

    @Test
    fun returningToCityUsesDestinationMapId() {
        val location = LocationResolver.resolve(0x0203, 0x0200)

        assertEquals("Ciudad Asuka", location.title)
        assertEquals(0x0200, location.publicMapId)
        assertFalse(location.title.contains("Salón", ignoreCase = true))
    }

    @Test
    fun returningToParkUsesDestinationMapId() {
        val location = LocationResolver.resolve(0x021E, 0x021D)

        assertEquals("Central Park", location.title)
        assertEquals(null, location.roomName)
    }

    @Test
    fun followsWhicheverStageWordChangedLast() {
        val tracker = LocationTracker()
        assertEquals(0x0202, tracker.follow(0x0202, 0x0202))
        assertEquals(0x021D, tracker.follow(0x021D, 0x0202))
        assertEquals(0x021E, tracker.follow(0x021D, 0x021E))
        assertEquals(0x020A, tracker.follow(0x020A, 0x020A))
        assertEquals(0x020A, tracker.follow(0x020A, 0x0C01))
    }

    @Test
    fun keepsCityWhenWorldMapRewindsAreaToInn() {
        val tracker = LocationTracker()
        assertEquals(0x020A, tracker.follow(0x020A, 0x020A))
        assertEquals(0x0200, tracker.follow(0x0200, 0x020A))
        assertEquals(0x0200, tracker.follow(0x020A, 0x1000))
    }

    @Test
    fun overlayBannerOverridesStaleCityId() {
        val tracker = LocationTracker()
        assertEquals(0x0200, tracker.follow(0x0200, 0x0200))
        assertEquals(0x0202, tracker.follow(0x0200, 0x0200, 0x0202))
        assertEquals(0x0202, tracker.follow(0x0200, 0x1000))
    }

    @Test
    fun overlayShopNameDoesNotOverrideDistantField() {
        val tracker = LocationTracker()
        assertEquals(0x0226, tracker.follow(0x0226, 0x0226))
        assertEquals(0x0226, tracker.follow(0x0226, 0x0226, 0x020E))
    }

    @Test
    fun blocksPartyChangesDuringBattleAndStory() {
        assertFalse(LocationResolver.canReorderParty(0x0200, 0x0200, GameMode.BATTLE, true))
        assertFalse(LocationResolver.canReorderParty(0x0E03, 0x0200, GameMode.EXPLORATION, true))
        assertTrue(LocationResolver.canReorderParty(0x0200, 0x0200, GameMode.EXPLORATION, true))
        assertFalse(LocationResolver.canFastTravel(0x0C01, 0x0C01, GameMode.EXPLORATION, true))
        assertTrue(LocationResolver.canFastTravel(0x0200, 0x0200, GameMode.EXPLORATION, true))
    }
}
