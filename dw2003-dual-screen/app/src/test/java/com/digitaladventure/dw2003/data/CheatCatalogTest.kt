package com.digitaladventure.dw2003.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CheatCatalogTest {
    @Test
    fun exposesPalQualityOfLifeCodes() {
        assertTrue(CheatCatalog.all.any { it.id == "infinite_bits" && it.code.contains("80048DA0") })
        assertTrue(CheatCatalog.all.any { it.id == "no_random_battles" })
        assertEquals("infinite_hp_battle", CheatCatalog.all.first { it.battleOnly }.id)
        assertFalse(CheatCatalog.all.any { it.id == "animation_speed" })
        assertFalse(CheatCatalog.all.any { it.id == "dithering_off" })
        assertFalse(CheatCatalog.all.any { it.id == "skip_music" })
    }
}
