package com.digitaladventure.dw2003.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AuctionCatalogTest {
    @Test
    fun opensDivineBarrierAfterSeiryuLeader() {
        assertNull(AuctionCatalog.forGuideKey("7d80-7d7c"))
        assertEquals("divine_barrier", AuctionCatalog.forGuideKey("7d14-7d10")?.id)
        assertEquals("divine_barrier", AuctionCatalog.forGuideKey("7bc4-7bc0")?.id)
    }

    @Test
    fun switchesToHazardShieldWhenSepikmonMaskHuntStarts() {
        assertEquals("hazard_shield", AuctionCatalog.forGuideKey("7518-7514")?.id)
        assertEquals("hazard_shield", AuctionCatalog.forGuideKey("71a4-7174")?.id)
        assertEquals("sniper_shield", AuctionCatalog.forGuideKey("6ebc-6eb8")?.id)
    }

    @Test
    fun ignoresUnknownAndPostGameKeys() {
        assertNull(AuctionCatalog.forGuideKey(null))
        assertNull(AuctionCatalog.forGuideKey("8170-816c"))
        assertNull(AuctionCatalog.forGuideKey("28a0-287c"))
    }

    @Test
    fun laterStoryBeatsOpenLaterAuctions() {
        assertEquals("yin_yang_rod", AuctionCatalog.forGuideKey("6350-634c")?.id)
        assertEquals("sniper_cannon", AuctionCatalog.forGuideKey("5134-5130")?.id)
        assertEquals("ronin_armor", AuctionCatalog.forGuideKey("2f34-2f30")?.id)
        assertEquals("brave_armor", AuctionCatalog.forGuideKey("2eb8-2eb4")?.id)
        assertEquals("brave_armor", AuctionCatalog.forGuideKey("2e44-2e40")?.id)
    }
}
