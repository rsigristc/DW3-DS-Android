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
    fun doesNotInventAmaterasuAskmapCodes() {
        listOf(0x0780, 0x0810, 0x0825, 0x0845, 0x0855).forEach { mapId ->
            assertNull(FlaweFastTravelTable.iconCode(mapId))
        }
    }
}
