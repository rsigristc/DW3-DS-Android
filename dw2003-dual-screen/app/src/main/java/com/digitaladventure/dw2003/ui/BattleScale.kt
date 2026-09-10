package com.digitaladventure.dw2003.ui

import com.digitaladventure.dw2003.model.GameMode

enum class BattleScale {
    OFF,
    BATTLE_2X,
    ALWAYS_2X;

    fun enhancementEnabled(mode: GameMode): Boolean = when (this) {
        OFF -> false
        ALWAYS_2X -> true
        BATTLE_2X -> mode == GameMode.BATTLE
    }

    companion object {
        val menuOptions: List<BattleScale> = listOf(OFF, ALWAYS_2X)

        fun fromPreference(value: String?): BattleScale = when (value) {
            OFF.name -> OFF
            ALWAYS_2X.name, BATTLE_2X.name, null -> ALWAYS_2X
            else -> ALWAYS_2X
        }
    }
}
