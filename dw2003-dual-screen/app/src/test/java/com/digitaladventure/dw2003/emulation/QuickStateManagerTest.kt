package com.digitaladventure.dw2003.emulation

import org.junit.Assert.assertEquals
import org.junit.Test

class QuickStateManagerTest {
    @Test
    fun romKeyIsFilesystemSafeAndBounded() {
        assertEquals("abcdef0123", QuickStateManager.safeKey("AB:CD/EF 01-23"))
        assertEquals("rom-desconocida", QuickStateManager.safeKey("---"))
        assertEquals(40, QuickStateManager.safeKey("A".repeat(80)).length)
    }
}
