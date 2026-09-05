package com.digitaladventure.dw2003.data

import org.junit.Assert.assertEquals
import org.junit.Test

class OverlayLocationFinderTest {
    @Test
    fun keepsCanonicalRoomWhenAliasesShareTheSameName() {
        assertEquals(0x0203, OverlayLocationFinder.stageId("Main Lobby\u0000".toByteArray()))
    }

    @Test
    fun readsCompactLabelsWithoutScanningInsideMachineCode() {
        val ram = ByteArray(0x40000) { 0x55 }
        ram[0x1FF] = 0
        val label = DwTextDecoder.encode("Seiryu Tower")
        label.copyInto(ram, 0x200)
        ram[0x200 + label.size] = 0
        assertEquals(0x0230, OverlayLocationFinder.stageId(ram))
    }

    @Test
    fun readsAsukaBridgeBanner() {
        val ram = ByteArray(0x80)
        "Central Sector".toByteArray(Charsets.US_ASCII).copyInto(ram, 0x08)
        "Asuka Bridge".toByteArray(Charsets.US_ASCII).copyInto(ram, 0x20)
        assertEquals(0x0202, OverlayLocationFinder.stageId(ram))
    }

    @Test
    fun prefersInnOverCityHubWhenBothLabelsAreVisible() {
        val ram = ByteArray(0x80)
        "Asuka City".toByteArray(Charsets.US_ASCII).copyInto(ram, 0x08)
        "Asuka Inn 1F".toByteArray(Charsets.US_ASCII).copyInto(ram, 0x20)
        assertEquals(0x020A, OverlayLocationFinder.stageId(ram))
    }
}
