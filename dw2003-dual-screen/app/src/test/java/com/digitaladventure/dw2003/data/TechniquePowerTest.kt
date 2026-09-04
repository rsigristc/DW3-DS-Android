package com.digitaladventure.dw2003.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TechniquePowerTest {
    @Test
    fun matchesRookieAliasesAndIgnoresUnknownSupportPower() {
        assertEquals(80, TechniquePower.powerOf("Hot Head"))
        assertEquals(100, TechniquePower.powerOf("Pyro Sphere"))
        assertEquals(100, TechniquePower.powerOf("Pyrosphere"))
        assertEquals(120, TechniquePower.powerOf("Vee Headbutt"))
        assertEquals(120, TechniquePower.powerOf("Vee Head Butt"))
        assertNull(TechniquePower.powerOf("Double Power"))
        assertEquals(60, TechniquePower.powerOf("Picking Claw"))
        assertNull(TechniquePower.powerOf("Unknown Technique"))
    }

    @Test
    fun fillsMissingMpAndPowerWithoutOverwritingCatalogMp() {
        val filled = TechniquePower.enrich(TechniqueInfo("Swing Swing", null, 1))
        assertEquals(20, filled.mp)
        assertEquals(80, filled.power)
        val kept = TechniquePower.enrich(TechniqueInfo("Nova Blast", 99, 60, null))
        assertEquals(99, kept.mp)
        assertEquals(300, kept.power)
    }
}
