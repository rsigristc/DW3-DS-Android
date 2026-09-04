package com.digitaladventure.dw2003.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TechniqueCatalogTest {
    @Test
    fun mapsRookieSignatureTechniques() {
        assertEquals("Pepper Breath", TechniqueCatalog.signatureFor(3))
        assertEquals("Diamond Storm", TechniqueCatalog.signatureFor(6))
        assertNull(TechniqueCatalog.signatureFor(99))
        val swing = TechniqueCatalog.signatureInfo(2)!!
        assertEquals("Swing Swing", swing.name)
        assertEquals(20, swing.mp)
        assertEquals(80, swing.power)
        val pepper = TechniqueCatalog.signatureInfo(3)!!
        assertEquals(30, pepper.mp)
        assertEquals(100, pepper.power)
        val vee = TechniqueCatalog.signatureInfo(4)!!
        assertEquals(22, vee.mp)
        assertEquals(120, vee.power)
    }
}
