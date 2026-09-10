package com.digitaladventure.dw2003.data

import com.digitaladventure.dw2003.model.GameMode
import org.junit.Assert.assertEquals
import org.junit.Test

class OverlaySignaturesTest {
    @Test
    fun prefersFightstgSlotOverFlaweHook() {
        val hook = 0x800C1548L
        val slot = GameStateReader.FIGHTST2_SIGNATURE
        assertEquals(GameMode.BATTLE, OverlaySignatures.mode(hook, slot))
        assertEquals(slot, OverlaySignatures.preferred(hook, slot))
    }

    @Test
    fun hookAloneIsNotBattle() {
        assertEquals(GameMode.EXPLORATION, OverlaySignatures.mode(0x800C1548L))
    }
}
