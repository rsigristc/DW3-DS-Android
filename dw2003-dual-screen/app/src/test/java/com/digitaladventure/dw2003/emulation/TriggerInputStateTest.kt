package com.digitaladventure.dw2003.emulation

import org.junit.Assert.*
import org.junit.Test
import com.digitaladventure.dw2003.emulation.TriggerInputState.Button.*

class TriggerInputStateTest {
    @Test fun axisAndKeyReportsDoNotReleaseEachOther() {
        val state = TriggerInputState()
        assertEquals(TriggerInputState.Change(L2, true), state.digital("keys:1", L2, true))
        assertTrue(state.axes(1, 1f, 0f).isEmpty())
        assertNull(state.digital("keys:1", L2, false))
        assertEquals(listOf(TriggerInputState.Change(L2, false)), state.axes(1, 0f, 0f))
    }
    @Test fun bothTriggersAndTouchCanBeHeldTogether() {
        val state = TriggerInputState()
        assertEquals(2, state.axes(1, 1f, 1f).size)
        assertNull(state.digital("touch", R2, true))
        assertEquals(listOf(TriggerInputState.Change(L2, false)), state.axes(1, 0f, 0f))
        assertEquals(listOf(TriggerInputState.Change(R2, false)), state.releaseAll())
        assertTrue(state.releaseAll().isEmpty())
    }
    @Test fun triggerThresholdHasHysteresis() {
        val state = TriggerInputState()
        assertTrue(state.axes(1, .2f, 0f).isEmpty())
        assertEquals(1, state.axes(1, .3f, 0f).size)
        assertTrue(state.axes(1, .2f, 0f).isEmpty())
        assertEquals(listOf(TriggerInputState.Change(L2, false)), state.axes(1, .1f, 0f))
    }
}
