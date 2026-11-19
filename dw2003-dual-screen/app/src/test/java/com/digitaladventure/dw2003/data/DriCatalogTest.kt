package com.digitaladventure.dw2003.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DriCatalogTest {
    @Test
    fun agumonAndGuilmonOpenAfterSeiryuWhenLevelFits() {
        val notices = DriCatalog.forState("7d14-7d10", 42, emptySet())
        assertEquals(listOf("agumon", "guilmon"), notices.filter { it.ready }.map { it.id })
    }

    @Test
    fun ignoresAgentsBeforeTheirStoryBeat() {
        val early = DriCatalog.forState("8084-8080", 42, emptySet())
        assertTrue(early.isEmpty())
    }

    @Test
    fun highPartyLevelBlocksButStillExplainsTheLab() {
        val notices = DriCatalog.forState("7d14-7d10", 80, emptySet())
        assertTrue(notices.none { it.ready })
        assertEquals(listOf("agumon", "guilmon"), notices.map { it.id })
        assertTrue(notices.first().label(true).contains("laboratorio"))
    }

    @Test
    fun skipsARookieAlreadyInTheParty() {
        val notices = DriCatalog.forState("7d14-7d10", 42, setOf(3))
        assertEquals(listOf("guilmon"), notices.map { it.id })
    }

    @Test
    fun veemonNeedsByakkoAndALowCombinedLevel() {
        assertTrue(DriCatalog.forState("7d14-7d10", 20, emptySet()).none { it.id == "veemon" })
        val ready = DriCatalog.forState("6350-634c", 20, emptySet()).single { it.id == "veemon" }
        assertTrue(ready.ready)
    }

    @Test
    fun laterAgentsNeedWestSectorAndSubmarimonBeat() {
        assertTrue(DriCatalog.forState("66bc-6690", 70, emptySet()).none { it.id in setOf("patamon", "kotemon") })
        val afterSea = DriCatalog.forState("6250-61ec", 70, emptySet())
        assertEquals(
            listOf("patamon", "renamon", "kotemon", "kumamon", "monmon"),
            afterSea.filter { it.ready }.map { it.id }
        )
    }
}
