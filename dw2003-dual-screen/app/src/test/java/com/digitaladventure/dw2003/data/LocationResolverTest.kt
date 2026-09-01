package com.digitaladventure.dw2003.data

import com.digitaladventure.dw2003.model.GameMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LocationResolverTest {
    @Test
    fun indoorRoomUsesBannerNameNotCityHub() {
        val location = LocationResolver.resolve(0x0206, 0x0200)

        assertEquals("Laboratorio Digimon", location.title)
        assertEquals("Sector Central · Laboratorio Digimon", location.radarLabel)
        assertEquals(0x0206, location.publicMapId)
        assertFalse(location.title.contains("Asuka", ignoreCase = true))
    }

    @Test
    fun innBannerWhenAreaIsRoomAndMapIsHub() {
        val location = LocationResolver.resolve(0x020A, 0x0200)

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
    fun outdoorFieldKeepsCurrentAreaEvenIfHubMapIdLags() {
        val location = LocationResolver.resolve(0x021D, 0x0200)

        assertEquals("Central Park", location.title)
        assertEquals("Sector Central · Central Park", location.radarLabel)
        assertEquals(0x021D, location.publicMapId)
        assertFalse(location.title.contains("Asuka", ignoreCase = true))
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
    fun returningToCityIgnoresStaleIndoorMapId() {
        val location = LocationResolver.resolve(0x0200, 0x0203)

        assertEquals("Ciudad Asuka", location.title)
        assertEquals(0x0200, location.publicMapId)
        assertFalse(location.title.contains("Salón", ignoreCase = true))
    }

    @Test
    fun distinctFieldMapsPreferAreaId() {
        val location = LocationResolver.resolve(0x021D, 0x0221)

        assertEquals("Central Park", location.title)
        assertEquals(null, location.roomName)
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
