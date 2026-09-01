package com.digitaladventure.dw2003.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EquipmentCatalogTest {
    @Test
    fun resolvesEquipmentNameTypeAndBonuses() {
        val item = EquipmentCatalog.get(92)!!
        assertEquals("Short Sword", item.name)
        assertEquals("Arma 1 mano", item.type)
        assertTrue(item.stats.contains("+14 FUE"))
        assertNull(EquipmentCatalog.get(0))
    }
}
