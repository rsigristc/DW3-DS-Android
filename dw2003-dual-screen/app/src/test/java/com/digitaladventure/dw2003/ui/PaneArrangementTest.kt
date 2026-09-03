package com.digitaladventure.dw2003.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class PaneArrangementTest {
    @Test
    fun restoresEveryArrangementAndFallsBackToAutomatic() {
        PaneArrangement.entries.forEach { arrangement ->
            assertEquals(arrangement, PaneArrangement.fromPreference(arrangement.name))
        }
        assertEquals(PaneArrangement.AUTO, PaneArrangement.fromPreference(null))
        assertEquals(PaneArrangement.AUTO, PaneArrangement.fromPreference("INVALID"))
    }
}
