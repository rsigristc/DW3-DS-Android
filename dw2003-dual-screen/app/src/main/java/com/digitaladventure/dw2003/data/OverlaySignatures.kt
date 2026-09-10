package com.digitaladventure.dw2003.data

import com.digitaladventure.dw2003.model.GameMode

object OverlaySignatures {
    /** First word of FIGHTSTG when it loads at 0x80082CB0. */
    const val SLOT_BASE = 0x82CB0
    const val FIELDSTG = 0x800839C4L

    fun mode(vararg words: Long): GameMode = when {
        words.any { it == GameStateReader.FIGHTST2_SIGNATURE } -> GameMode.BATTLE
        words.any { it == GameStateReader.STSTATUS_SIGNATURE } -> GameMode.MANAGEMENT
        else -> GameMode.EXPLORATION
    }

    fun preferred(hook: Long, slot: Long): Long = when {
        slot == GameStateReader.FIGHTST2_SIGNATURE || slot == GameStateReader.STSTATUS_SIGNATURE -> slot
        hook == GameStateReader.FIGHTST2_SIGNATURE || hook == GameStateReader.STSTATUS_SIGNATURE -> hook
        slot != 0L -> slot
        else -> hook
    }

    fun hex(word: Long): String = "0x${word.toString(16).uppercase()}"

    fun label(word: Long, spanish: Boolean): String = when (word) {
        GameStateReader.FIGHTST2_SIGNATURE -> "FIGHTST2"
        GameStateReader.STSTATUS_SIGNATURE -> "STSTATUS"
        FIELDSTG -> "FIELDSTG"
        0x800C1548L -> if (spanish) "gancho Flawe" else "Flawe hook"
        0L -> "—"
        else -> hex(word)
    }
}
