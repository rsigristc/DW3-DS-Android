package com.digitaladventure.dw2003.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TechniqueCatalogTest {
    @Test
    fun mapsRookieSignatureTechniques() {
        assertEquals("Pepper Breath", TechniqueCatalog.signatureFor(3))
        assertEquals("Diamond Storm", TechniqueCatalog.signatureFor(6))
        assertNull(TechniqueCatalog.signatureFor(99))
    }
}
