package com.digitaladventure.dw2003.ui

import com.digitaladventure.dw2003.model.GameMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BattleScaleTest {
    @Test
    fun mapsLegacyBattleOnlyPreferenceToAlways() {
        assertEquals(BattleScale.ALWAYS_2X, BattleScale.fromPreference("BATTLE_2X"))
        assertEquals(BattleScale.ALWAYS_2X, BattleScale.fromPreference(null))
        assertEquals(BattleScale.OFF, BattleScale.fromPreference("OFF"))
        assertEquals(listOf(BattleScale.OFF, BattleScale.ALWAYS_2X), BattleScale.menuOptions)
        assertTrue(BattleScale.ALWAYS_2X.enhancementEnabled(GameMode.EXPLORATION))
        assertFalse(BattleScale.OFF.enhancementEnabled(GameMode.BATTLE))
    }
}
