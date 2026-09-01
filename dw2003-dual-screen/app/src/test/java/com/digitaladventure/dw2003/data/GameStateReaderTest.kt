package com.digitaladventure.dw2003.data

import com.digitaladventure.dw2003.model.GameMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameStateReaderTest {
    @Test
    fun parsesAreaModeFormationAndStats() {
        val ram = ByteArray(GameStateReader.MAIN_LENGTH)
        put16(ram, GameStateReader.AREA - GameStateReader.MAIN_BASE, 0x021D)
        put16(ram, GameStateReader.MAP_ID - GameStateReader.MAIN_BASE, 0x0221)
        putEncodedName(ram, GameStateReader.PLAYER_NAME - GameStateReader.MAIN_BASE, "Junior")
        put32(ram, GameStateReader.BITS - GameStateReader.MAIN_BASE, 24_560)
        put16(ram, GameStateReader.STORY_STAGE - GameStateReader.MAIN_BASE, 42)
        put32(ram, 0x48DA4 - GameStateReader.MAIN_BASE, 5)
        put32(ram, 0x48DA8 - GameStateReader.MAIN_BASE, 6)
        put32(ram, 0x48DAC - GameStateReader.MAIN_BASE, 3)
        val guilmon = GameStateReader.STATS - GameStateReader.MAIN_BASE + 5 * GameStateReader.PROFILE_STRIDE
        put32(ram, guilmon + 0x18, 123456)
        put16(ram, guilmon + 0x1C, 28)
        put16(ram, guilmon + 0x20, 1280)
        put16(ram, guilmon + 0x22, 1400)
        put16(ram, guilmon + 0x24, 600)
        put16(ram, guilmon + 0x26, 700)
        put16(ram, guilmon + 0x28, 119)
        put16(ram, guilmon - 4, 367)
        put16(ram, guilmon + GameStateReader.DIGIEVOLUTION_OFFSET, 367)
        put16(ram, guilmon + GameStateReader.DIGIEVOLUTION_OFFSET + 2, 25)
        listOf(6, 3).forEach { profile ->
            val base = GameStateReader.STATS - GameStateReader.MAIN_BASE + profile * GameStateReader.PROFILE_STRIDE
            put16(ram, base + 0x1C, 1)
            put16(ram, base + 0x22, 1)
        }

        val snapshot = GameStateReader().parse(ram, GameStateReader.FIGHTST2_SIGNATURE, "Objetivo de prueba")

        assertEquals(GameMode.BATTLE, snapshot.mode)
        assertEquals("Central Park", snapshot.areaName)
        assertEquals("Bosque Alambre Oeste", snapshot.locationTitle)
        assertEquals("Sector Central · Bosque Alambre Oeste", snapshot.radarLabel)
        assertEquals("Bosque Alambre Oeste", snapshot.mapName)
        assertFalse(snapshot.canReorderParty)
        assertFalse(snapshot.canFastTravel)
        assertEquals("Junior", snapshot.tamerName)
        assertEquals("Servidor Asuka", snapshot.serverName)
        assertEquals("Sector Central", snapshot.sectorName)
        assertEquals(24_560L, snapshot.bits)
        assertTrue(snapshot.fishingAvailable)
        assertEquals(42, snapshot.storyStage)
        assertEquals(listOf("Guilmon", "Renamon", "Agumon"), snapshot.party.map { it.name })
        assertEquals(1280, snapshot.party.first().currentHp)
        assertEquals(119, snapshot.party.first().strength)
        assertEquals("Growlmon", snapshot.party.first().activeDigievolutionName)
        assertEquals(25, snapshot.party.first().activeDigievolutionLevel)
        assertEquals(listOf(42, 42, 18), snapshot.party.first().activeSkills.map { it.mp })
        assertTrue(snapshot.gameStarted)
        assertTrue(snapshot.isLive)
    }

    @Test
    fun indoorAsukaRoomUsesRoomBanner() {
        val ram = ByteArray(GameStateReader.MAIN_LENGTH)
        put16(ram, GameStateReader.AREA - GameStateReader.MAIN_BASE, 0x0206)
        put16(ram, GameStateReader.MAP_ID - GameStateReader.MAIN_BASE, 0x0200)
        put16(ram, GameStateReader.STORY_STAGE - GameStateReader.MAIN_BASE, 4)

        val snapshot = GameStateReader().parse(ram, 0L, null)

        assertEquals("Laboratorio Digimon", snapshot.areaName)
        assertEquals("Laboratorio Digimon", snapshot.locationTitle)
        assertEquals("Sector Central · Laboratorio Digimon", snapshot.radarLabel)
        assertEquals("Sector Central · Laboratorio Digimon · 0x0206", snapshot.locationDetail)
        assertEquals("Laboratorio Digimon", snapshot.mapName)
        assertTrue(snapshot.locationDetail.contains("Laboratorio"))
        assertTrue(snapshot.canFastTravel)
        assertTrue(snapshot.canReorderParty)
    }

    @Test
    fun bridgeUsesMapIdWhenAreaStaysOnCityHub() {
        val ram = ByteArray(GameStateReader.MAIN_LENGTH)
        put16(ram, GameStateReader.AREA - GameStateReader.MAIN_BASE, 0x0200)
        put16(ram, GameStateReader.MAP_ID - GameStateReader.MAIN_BASE, 0x0202)
        put16(ram, GameStateReader.STORY_STAGE - GameStateReader.MAIN_BASE, 4)

        val snapshot = GameStateReader().parse(ram, 0L, null)

        assertEquals("Ciudad Asuka", snapshot.areaName)
        assertEquals("Puente Asuka", snapshot.locationTitle)
        assertEquals("Sector Central · Puente Asuka", snapshot.radarLabel)
        assertEquals(0x0202, snapshot.mapId)
        assertTrue(snapshot.fishingAvailable)
    }

    @Test
    fun titleScreenDoesNotInventKotemonFormation() {
        val ram = ByteArray(GameStateReader.MAIN_LENGTH)

        val snapshot = GameStateReader().parse(ram, 0L, null)

        assertEquals(0, snapshot.areaId)
        assertEquals(0, snapshot.storyStage)
        assertTrue(snapshot.party.isEmpty())
        assertTrue(!snapshot.gameStarted)
        assertEquals("Inicia o carga una partida para activar el panel complementario.", snapshot.objective)
    }

    private fun put16(target: ByteArray, offset: Int, value: Int) {
        target[offset] = value.toByte()
        target[offset + 1] = (value ushr 8).toByte()
    }

    private fun put32(target: ByteArray, offset: Int, value: Int) {
        repeat(4) { target[offset + it] = (value ushr (it * 8)).toByte() }
    }

    private fun putEncodedName(target: ByteArray, offset: Int, value: String) {
        value.forEachIndexed { index, character ->
            target[offset + index] = when (character) {
                in 'A'..'Z' -> (0x0E + character.code - 'A'.code).toByte()
                in 'a'..'z' -> (0x28 + character.code - 'a'.code).toByte()
                else -> 0
            }
        }
    }
}
