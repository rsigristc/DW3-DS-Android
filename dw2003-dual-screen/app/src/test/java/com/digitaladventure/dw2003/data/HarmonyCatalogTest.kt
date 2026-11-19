package com.digitaladventure.dw2003.data

import java.io.File
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class HarmonyCatalogTest {
    private val json = File("src/main/assets/harmony/catalog-v50.json").readText()
    private val catalog = HarmonyCatalog(json)

    @Test fun matchesOnlyTheAuditedResidentAndHook() {
        val root = JSONObject(json)
        fun hex(s: String) = s.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
        val memory = mapOf(root.getInt("hookOffset") to hex(root.getString("hook")),
            root.getInt("profileOffset") to hex(root.getString("profileFingerprint")))
        assertTrue(catalog.matches { at, n -> memory[at] ?: ByteArray(n) })
        assertFalse(catalog.matches { _, n -> ByteArray(n) })
        assertFalse(catalog.matches { at, n -> memory[at]?.copyOf()?.also { it[0] = 0 } ?: ByteArray(n) })
        assertEquals(115, catalog.profiles.size)
    }

    @Test fun hasTheCompleteTerriermonRouteAndNewSignatures() {
        assertEquals(setOf(0x6101, 0x6102, 372, 0x602E), catalog.routes(8).map { it.id }.toSet())
        assertEquals("Terriermon", catalog.profile(0x6100)?.name)
        assertEquals("Gargomon", catalog.profile(0x6101)?.name)
        assertEquals("Rapidmon", catalog.profile(0x6102)?.name)
        assertEquals("Petit Twister", catalog.technique(0x6100, 12, 0, emptyList(), false)?.name)
        assertEquals("Gargo Laser", catalog.technique(0x6101, 44, 0, emptyList(), false)?.name)
        assertEquals("Rapid Fire", catalog.technique(0x6102, 50, 0, emptyList(), false)?.name)
        val detail = catalog.details(8, 0x6102, false)!!
        assertTrue(detail.contains("Terriermon: LV 20"))
        assertTrue(detail.contains("Gargomon DV 15"))
        assertTrue(detail.contains("Rapid Fire · DV 70"))
    }

    @Test fun usesScopedMpAccessoriesAndLoadedSkillCosts() {
        assertEquals(162, catalog.technique(0x601B, 104, 0, emptyList(), false)?.mp)
        assertEquals(195, catalog.technique(0x601B, 104, 0x4000, emptyList(), false)?.mp)
        assertEquals(153, catalog.technique(0x601B, 104, 0, listOf(0, 0, 0, 0, 0x144, 0), false)?.mp)
        assertEquals("Dark Inferno", catalog.technique(0x6011, 32, 0, emptyList(), false)?.name)
        assertEquals("Darkness Zone", catalog.technique(0x603C, 273, 0, emptyList(), false)?.name)
        assertEquals(144, catalog.technique(150, 101, 0, emptyList(), false)?.mp)
        assertEquals(800, catalog.technique(150, 101, 0, emptyList(), false)?.power)
        // Flame Ball is inherited, not part of BK MegaGargomon's own repertoire.
        assertEquals(18, catalog.technique(0x602E, 137, 0, emptyList(), false)?.mp)
    }
}
