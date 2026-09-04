package com.digitaladventure.dw2003.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DigievolutionCatalogTest {
    @Test fun resolvesFormAndFiltersTechniquesBySkillLevel() {
        assertEquals("Growlmon", DigievolutionCatalog.name(367))
        val techniques = DigievolutionCatalog.techniques(367, 25)
        assertEquals(listOf("Double Power", "Double Guard", "Picking Claw"), techniques.map { it.name })
        assertEquals(listOf(42, 42, 18), techniques.map { it.mp })
        assertEquals(listOf(null, null, 60), techniques.map { it.power })
    }

    @Test fun rejectsUnknownForm() {
        assertEquals(null, DigievolutionCatalog.name(9999))
        assertTrue(DigievolutionCatalog.techniques(9999, 99).isEmpty())
    }
}
