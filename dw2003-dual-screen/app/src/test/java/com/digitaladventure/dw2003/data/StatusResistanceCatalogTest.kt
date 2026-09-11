package com.digitaladventure.dw2003.data

import org.junit.Assert.assertEquals
import org.junit.Test

class StatusResistanceCatalogTest {
    @Test
    fun resolvesRookieAndActivePartnerForms() {
        assertEquals(listOf(50, 20, 30, 20, 30), StatusResistanceCatalog.forPartner(3, 0))
        assertEquals(listOf(30, 20, 0, 40, 10), StatusResistanceCatalog.forPartner(5, 367))
    }

    @Test
    fun resolvesEnemyVariantByBattleId() {
        assertEquals(listOf(99, 50, 50, 50, 0), StatusResistanceCatalog.forEnemy(32))
    }
}
