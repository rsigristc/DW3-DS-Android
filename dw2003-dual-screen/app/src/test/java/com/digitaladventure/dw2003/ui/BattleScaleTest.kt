package com.digitaladventure.dw2003.ui

import com.digitaladventure.dw2003.model.GameMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BattleScaleTest {
    @Test
    fun mapsAlwaysTwoXToBattleOnly() {
        assertEquals(BattleScale.BATTLE_2X, BattleScale.fromPreference("BATTLE_2X"))
        assertEquals(BattleScale.BATTLE_2X, BattleScale.fromPreference("ALWAYS_2X"))
        assertEquals(BattleScale.OFF, BattleScale.fromPreference(null))
        assertEquals(BattleScale.OFF, BattleScale.fromPreference("OFF"))
        assertEquals(listOf(BattleScale.OFF, BattleScale.BATTLE_2X), BattleScale.menuOptions)
        assertFalse(BattleScale.BATTLE_2X.enhancementEnabled(GameMode.EXPLORATION))
        assertTrue(BattleScale.BATTLE_2X.enhancementEnabled(GameMode.BATTLE))
        assertFalse(BattleScale.OFF.enhancementEnabled(GameMode.BATTLE))
    }
}
