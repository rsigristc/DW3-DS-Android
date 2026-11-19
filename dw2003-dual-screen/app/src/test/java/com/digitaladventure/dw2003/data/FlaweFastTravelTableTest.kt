package com.digitaladventure.dw2003.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FlaweFastTravelTableTest {
    @Test
    fun recoversTheFortySixAsukaAskmapIconsFromThePatcherIps() {
        assertEquals(46, FlaweFastTravelTable.asukaIcons.size)
        assertEquals(46, FlaweFastTravelTable.asukaMapIds.size)
        assertEquals(0x14, FlaweFastTravelTable.iconCode(0x0200))
        assertEquals(0x1E, FlaweFastTravelTable.iconCode(0x021D))
        assertEquals(0x16, FlaweFastTravelTable.iconCode(0x021E))
        assertEquals(0x0A, FlaweFastTravelTable.iconCode(0x0261))
        assertEquals(0x01, FlaweFastTravelTable.iconCode(0x0267))
        assertEquals(0x06, FlaweFastTravelTable.iconCode(0x026F))
        assertNull(FlaweFastTravelTable.iconCode(0x0202))
        assertTrue(FlaweFastTravelTable.asukaIcons.map { it.iconCode }.toSet() == (1..46).toSet())
    }

    @Test
    fun nativeAmaterasuIdsShareTheirAsukaIconCodes() {
        assertEquals(45, FlaweFastTravelTable.amaterasuIcons.size)
        assertEquals(0x14, FlaweFastTravelTable.iconCode(0x0270))
        assertEquals(0x1E, FlaweFastTravelTable.iconCode(0x028C))
        assertEquals(0x0F, FlaweFastTravelTable.iconCode(0x029C))
        assertEquals(0x2A, FlaweFastTravelTable.iconCode(0x02AB))
        assertEquals(0x22, FlaweFastTravelTable.iconCode(0x02C6))
        assertEquals(0x06, FlaweFastTravelTable.iconCode(0x02D6))
        assertEquals(0x27, FlaweFastTravelTable.iconCode(0x025A))
        assertNull(FlaweFastTravelTable.iconCode(0x0780))
        FlaweFastTravelTable.amaterasuIcons.forEach {
            assertEquals(it.iconCode, FlaweFastTravelTable.iconCode(FlaweFastTravelTable.asukaCounterpart.getValue(it.mapId)))
        }
    }
}
