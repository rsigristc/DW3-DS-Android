package com.digitaladventure.dw2003.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BattleSetupReaderTest {
    @Test
    fun readsCopiedEncounterPayloadFromBattleSetup() {
        val setup = ByteArray(BattleSetupReader.SETUP_LENGTH)
        put16(setup, 0x18, 0x20)
        put16(setup, 0x1C, 6)
        put16(setup, 0x1E, 220)
        put16(setup, 0x20, 40)
        put16(setup, 0x22, 4)

        val enemies = BattleSetupReader.parse(setup, spanish = false)

        assertEquals(1, enemies.size)
        assertEquals("Kunemon", enemies[0].name)
        assertEquals(6, enemies[0].level)
        assertEquals(220, enemies[0].maxHp)
        assertEquals(50, enemies[0].strength)
        assertEquals(42, enemies[0].defense)
        assertEquals(4, enemies[0].expMultiplier)
        assertEquals(listOf(90, 90, 90, 60, 160, 90, 160), enemies[0].resistances)
        assertFalse(enemies[0].liveHp)
        assertEquals(listOf("Static Elect (50)"), enemies[0].attacks)
        assertEquals("No loot", enemies[0].loot)
    }

    @Test
    fun usesArenaHpWhenPlausible() {
        val setup = ByteArray(BattleSetupReader.SETUP_LENGTH)
        put16(setup, 0x18, 0x20)
        put16(setup, 0x1C, 6)
        put16(setup, 0x1E, 220)
        put16(setup, 0x20, 40)
        put16(setup, 0x22, 4)
        val arena = ByteArray(BattleSetupReader.ARENA_LENGTH)
        put16(arena, 3 * BattleSetupReader.ARENA_STRIDE + 0x18, 180)

        val enemy = BattleSetupReader.parse(setup, arena, spanish = false).single()
        assertEquals(180, enemy.currentHp)
        assertTrue(enemy.liveHp)
    }

    @Test
    fun scansArenaWhenSetupKeepsEncounterPointers() {
        val setup = ByteArray(BattleSetupReader.SETUP_LENGTH)
        put32(setup, 0x18, 0x80092E08.toInt())
        put32(setup, 0x24, 0x80092DFC.toInt())
        put32(setup, 0x30, 0x80092DFC.toInt())
        val arena = ByteArray(BattleSetupReader.ARENA_LENGTH)
        put16(arena, 0x40, 198)
        put16(arena, 0x44, 4)
        put16(arena, 0x46, 51)
        put16(arena, 0x48, 40)
        put16(arena, 0x4A, 2)

        val enemy = BattleSetupReader.parse(setup, arena, spanish = false).single()
        assertEquals("Kiwimon", enemy.name)
        assertEquals(4, enemy.level)
        assertEquals(51, enemy.maxHp)
    }

    @Test
    fun namesYanmamonThunderRayWhenLangTableIsStub() {
        val setup = ByteArray(BattleSetupReader.SETUP_LENGTH)
        put16(setup, 0x18, 67)
        put16(setup, 0x1C, 3)
        put16(setup, 0x1E, 168)
        put16(setup, 0x20, 40)
        put16(setup, 0x22, 16)

        val enemy = BattleSetupReader.parse(setup, spanish = false).single()
        assertEquals("Yanmamon", enemy.name)
        assertEquals(listOf("Thunder Ray (53)"), enemy.attacks)
        assertEquals("No loot", enemy.loot)
    }

    @Test
    fun namesCrabmonScissorsAttackWithPower() {
        val setup = ByteArray(BattleSetupReader.SETUP_LENGTH)
        put16(setup, 0x18, 197)
        put16(setup, 0x1C, 4)
        put16(setup, 0x1E, 88)
        put16(setup, 0x20, 40)
        put16(setup, 0x22, 2)

        val enemy = BattleSetupReader.parse(setup, spanish = true).single()
        assertEquals("Crabmon", enemy.name)
        assertEquals(listOf("Ataque Tijera (53)"), enemy.attacks)
        assertEquals("Scissors Attack (53)", EnemyCatalog.techniqueLabel(222, false))
    }

    private fun put32(bytes: ByteArray, offset: Int, value: Int) {
        bytes[offset] = (value and 0xFF).toByte()
        bytes[offset + 1] = ((value shr 8) and 0xFF).toByte()
        bytes[offset + 2] = ((value shr 16) and 0xFF).toByte()
        bytes[offset + 3] = ((value shr 24) and 0xFF).toByte()
    }

    private fun put16(bytes: ByteArray, offset: Int, value: Int) {
        bytes[offset] = (value and 0xFF).toByte()
        bytes[offset + 1] = ((value shr 8) and 0xFF).toByte()
    }
}
