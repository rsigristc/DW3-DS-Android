package com.digitaladventure.dw2003.data

import java.io.File
import org.junit.Assert.*
import org.junit.Test

class HarmonyGameStateReaderTest {
    private val catalog = HarmonyCatalog(File("src/main/assets/harmony/catalog-v50.json").readText())

    private fun put(bytes: ByteArray, offset: Int, value: Int, length: Int = 2) {
        repeat(length) { bytes[offset + it] = (value ushr (it * 8)).toByte() }
    }
    private fun main(): ByteArray = ByteArray(GameStateReader.MAIN_LENGTH).also {
        put(it, GameStateReader.AREA - GameStateReader.MAIN_BASE, 0x0200)
        put(it, GameStateReader.MAP_ID - GameStateReader.MAIN_BASE, 0x0200)
        GameStateReader.ACTIVE_PARTY.forEachIndexed { i, at -> put(it, at - GameStateReader.MAIN_BASE, if (i == 0) 8 else -1, 4) }
        // Deliberately invalid adjacent story bytes: they are NOT member eight.
        put(it, GameStateReader.STATS - GameStateReader.MAIN_BASE + 8 * GameStateReader.PROFILE_STRIDE + 0x1C, 99)
    }
    private fun extra(): ByteArray = ByteArray(HarmonyCatalog.RECORD_LENGTH).also {
        put(it, 4, 11, 4)
        put(it, 12 + 0x1C, 20)
        put(it, 12 + 0x20, 400)
        put(it, 12 + 0x22, 500)
        put(it, 12 + 0x24, 80)
        put(it, 12 + 0x26, 100)
    }

    @Test fun readsNinthPartnerFromItsSeparateRecordOnlyInVerifiedHarmony() {
        val main = main()
        val extra = extra()
        val snapshot = GameStateReader(catalog).parse(main, 0, null, harmonyActive = true, harmonyPartner = extra)
        val partner = snapshot.party.single()
        assertEquals(8, partner.profileId)
        assertEquals("Terriermon", partner.name)
        assertEquals(20, partner.level)
        assertEquals(400, partner.currentHp)
        assertEquals("Petit Twister", partner.activeSkills.single().name)
        assertEquals(ExperienceTable.nextLevel(3, 20), partner.nextLevelExperience)
        assertTrue(snapshot.supportsHarmony)
        assertTrue(GameStateReader(catalog).parse(main, 0, null, harmonyPartner = extra).party.isEmpty())
        assertTrue(GameStateReader(catalog).parse(main, 0, null, harmonyActive = true).party.isEmpty())
        extra[4] = 0
        assertTrue(GameStateReader(catalog).parse(main, 0, null, harmonyActive = true, harmonyPartner = extra).party.isEmpty())
    }

    @Test fun recognizesD6MarkerAndShowsOnlyActuallyLearnedNewTechniques() {
        val extra = extra()
        put(extra, 8, 0x6101)
        val entry = 12 + GameStateReader.DIGIEVOLUTION_OFFSET
        put(extra, entry, 0x6101)
        extra[entry + 2] = 10
        extra[entry + 3] = 0xD6.toByte()
        put(extra, entry + 8, 132)
        put(extra, entry + 10, 131 or 0x2000)
        val partner = GameStateReader(catalog).parse(main(), 0, null,
            harmonyActive = true, harmonyPartner = extra).party.single()
        assertEquals("Gargomon", partner.activeDigievolutionName)
        assertEquals(10, partner.activeDigievolutionLevel)
        assertEquals(listOf("Terriermon", "Gargomon"), partner.displayedForms.map { it.name })
        assertEquals(listOf("Wing Buster", "Bug Buster"), partner.activeSkills.map { it.name })
        assertFalse(partner.activeSkills.any { it.name == "Gargo Laser" })
        assertTrue(partner.displayedForms.last().details!!.contains("Gargo Laser"))
    }
}
