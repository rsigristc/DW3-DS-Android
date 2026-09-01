package com.digitaladventure.dw2003.data

import com.digitaladventure.dw2003.model.GameMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LocationResolverTest {
    @Test
    fun cityHubKeepsLoadScreenNameForIndoorRooms() {
        val location = LocationResolver.resolve(0x0206, 0x0200)

        assertEquals("Ciudad Asuka", location.title)
        assertEquals("Sector Central · Ciudad Asuka", location.radarLabel)
        assertEquals("Sector Central · Ciudad Asuka · 0x0200", location.detail)
        assertEquals(0x0200, location.publicMapId)
        assertFalse(location.detail.contains("Laboratorio", ignoreCase = true))
        assertFalse(location.radarLabel.contains("Laboratorio", ignoreCase = true))
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
    fun distinctFieldMapsPreferTheCurrentArea() {
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
