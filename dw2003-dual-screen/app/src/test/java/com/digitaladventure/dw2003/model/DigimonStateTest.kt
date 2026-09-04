package com.digitaladventure.dw2003.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DigimonStateTest {
    @Test
    fun totalsIncludeEquipmentBonusesAndKeepBaseRam() {
        val guilmon = GameSnapshot.demo().party.first()
        assertEquals(119, guilmon.strength)
        assertEquals(119 + 116, guilmon.totalStrength)
        assertEquals(91 + 15, guilmon.totalDefense)
        assertEquals(96 - 3, guilmon.totalSpeed)
        assertEquals(72 + 77, guilmon.totalCharisma)
        assertEquals(116, guilmon.equipmentBonuses.strength)
        assertEquals(-3, guilmon.equipmentBonuses.speed)
    }

    @Test
    fun rookieSkillsExposeMpAndPower() {
        val monmon = DigimonState(
            profileId = 2,
            name = "Monmon",
            level = 2,
            experience = 21,
            trainingPoints = 5,
            currentHp = 214,
            maxHp = 214,
            currentMp = 127,
            maxMp = 127,
            strength = 50,
            defense = 52,
            spirit = 30,
            wisdom = 43,
            speed = 65,
            charisma = 9,
            tolerances = listOf(101, 61, 117, 80, 81, 131, 117),
            equipmentIds = listOf(0, 0, 0, 0, 0, 0)
        )
        assertEquals(listOf("Swing Swing"), monmon.activeSkills.map { it.name })
        assertEquals(listOf(20), monmon.activeSkills.map { it.mp })
        assertEquals(listOf(80), monmon.activeSkills.map { it.power })
        assertEquals(50, monmon.totalStrength)
        assertEquals(101, monmon.totalResistances[0])
        assertEquals(listOf("Monmon" to 2), monmon.displayedForms.map { it.name to it.level })
        assertTrue(monmon.displayedForms.single().active)
    }

    @Test
    fun demoGuilmonListsRookieAndActiveChampion() {
        val guilmon = GameSnapshot.demo().party.first()
        assertEquals(listOf("Guilmon" to 28, "Growlmon" to 25), guilmon.displayedForms.map { it.name to it.level })
        assertEquals(listOf(false, true), guilmon.displayedForms.map { it.active })
        assertEquals("Growlmon", guilmon.activeDigievolutionName)
    }
}
