package com.digitaladventure.dw2003.ui

import com.digitaladventure.dw2003.model.GameMode
import com.swordfish.libretrodroid.ShaderConfig
import org.junit.Assert.assertTrue
import org.junit.Test

class VideoFilterTest {
    @Test
    fun defaultsToAaPlus() {
        assertTrue(VideoFilter.fromPreference(null) == VideoFilter.ANTIALIAS_PLUS)
        assertTrue(VideoFilter.fromPreference("ANTIALIAS") == VideoFilter.ANTIALIAS_PLUS)
        assertTrue(VideoFilter.ANTIALIAS_PLUS.shader() is ShaderConfig.CUT3)
        assertTrue(VideoFilter.SHARP.shader() is ShaderConfig.Sharp)
    }

    @Test
    fun twoXModeUsesSharpInFieldAndBattle() {
        val plus = VideoFilter.ANTIALIAS_PLUS
        assertTrue(plus.shaderFor(GameMode.EXPLORATION, BattleScale.OFF) is ShaderConfig.CUT3)
        assertTrue(plus.shaderFor(GameMode.BATTLE, BattleScale.OFF) is ShaderConfig.CUT3)
        assertTrue(plus.shaderFor(GameMode.EXPLORATION, BattleScale.BATTLE_2X) is ShaderConfig.Sharp)
        assertTrue(plus.shaderFor(GameMode.BATTLE, BattleScale.BATTLE_2X) is ShaderConfig.Sharp)
        assertTrue(VideoFilter.SHARP.shaderFor(GameMode.EXPLORATION, BattleScale.OFF) is ShaderConfig.Sharp)
        assertTrue(VideoFilter.SHARP.shaderFor(GameMode.BATTLE, BattleScale.OFF) is ShaderConfig.Sharp)
    }
}
