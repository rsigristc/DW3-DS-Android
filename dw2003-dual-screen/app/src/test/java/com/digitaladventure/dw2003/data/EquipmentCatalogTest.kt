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

    @Test
    fun parsesPositiveNegativeAndElementalBonuses() {
        val sword = EquipmentBonuses.parse("+14 FUE · +15 CAR")
        assertEquals(14, sword.strength)
        assertEquals(15, sword.charisma)
        assertEquals(0, sword.speed)

        val glove = EquipmentBonuses.parse("+46 FUE · -4 VEL · +25 CAR")
        assertEquals(46, glove.strength)
        assertEquals(-4, glove.speed)
        assertEquals(25, glove.charisma)

        val flame = EquipmentBonuses.parse("+20 FUEGO · +3 CAR")
        assertEquals(20, flame.fire)
        assertEquals(3, flame.charisma)

        val machine = EquipmentBonuses.parse("+20 MÁQ · +3 CAR")
        assertEquals(20, machine.machine)
        val dark = EquipmentBonuses.parse("+20 OSC · +3 CAR")
        assertEquals(20, dark.dark)
        assertEquals(0, EquipmentBonuses.parse("Sin bonificación directa").strength)
    }

    @Test
    fun sumsEquippedItemsFromDemoGuilmonLoadout() {
        val bonus = EquipmentBonuses.of(
            listOf(
                EquipmentCatalog.get(148),
                null,
                EquipmentCatalog.get(238),
                null,
                EquipmentCatalog.get(331),
                null
            )
        )
        assertEquals(116, bonus.strength)
        assertEquals(15, bonus.defense)
        assertEquals(-3, bonus.speed)
        assertEquals(77, bonus.charisma)
    }
}
