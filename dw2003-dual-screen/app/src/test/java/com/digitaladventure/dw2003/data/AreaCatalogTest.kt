package com.digitaladventure.dw2003.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AreaCatalogTest {
    @Test
    fun resolvesRealNonConsecutiveMapIds() {
        assertEquals("Ciudad Asuka", AreaCatalog.name(0x0200))
        assertEquals("Laboratorio Digimon", AreaCatalog.name(0x0206))
        assertEquals("Centro Online", AreaCatalog.name(0x02D8))
        assertEquals("Ciudad Amaterasu", AreaCatalog.name(0x0780))
        assertEquals("Ciudad Bai Hu", AreaCatalog.name(0x0845))
        assertEquals("Área 0x7777", AreaCatalog.name(0x7777))
        assertTrue(AreaCatalog.isOverlay(0x0C01))
        assertFalse(AreaCatalog.isField(0x0C01))
        assertTrue(AreaCatalog.isField(0x0206))
    }

    @Test
    fun identifiesKnownFishingSpots() {
        assertTrue(AreaCatalog.supportsFishing(0x021F))
        assertFalse(AreaCatalog.supportsFishing(0x0206))
    }
}
