package com.digitaladventure.dw2003.ui

import com.digitaladventure.dw2003.model.GameMode
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BattleScaleTest {
    @Test
    fun enablesEnhancedResolutionOnlyInBattleByDefault() {
        assertTrue(BattleScale.BATTLE_2X.enhancementEnabled(GameMode.BATTLE))
        assertFalse(BattleScale.BATTLE_2X.enhancementEnabled(GameMode.EXPLORATION))
        assertFalse(BattleScale.BATTLE_2X.enhancementEnabled(GameMode.MANAGEMENT))
        assertTrue(BattleScale.ALWAYS_2X.enhancementEnabled(GameMode.EXPLORATION))
        assertFalse(BattleScale.OFF.enhancementEnabled(GameMode.BATTLE))
    }
}
