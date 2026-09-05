package com.digitaladventure.dw2003.data

import java.io.File
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class FlaweGuideSelectorTest {
    private val program = File("src/main/assets/guide/selector.bin").readBytes()
    private val json = File("src/main/assets/guide/objectives.json").readText()
    private val selector = FlaweGuideSelector(program)
    private fun fixture() = ByteArray(GameStateReader.MAIN_LENGTH).also {
        it[0x4B370 - GameStateReader.MAIN_BASE] = 4
        it[0x4B3C5 - GameStateReader.MAIN_BASE] = 0x40
        it[0x48D68 - GameStateReader.MAIN_BASE] = 0x30
        it[0x48D69 - GameStateReader.MAIN_BASE] = 2
    }

    @Test fun tomAndProtocolMatchFlaweWithTheSameStoryStage() {
        val main = fixture()
        assertEquals("8084-8080", selector.select(main))
        // Talking to Tom sets this flag; stage 4 itself does not change.
        main[0x4B3B6 - GameStateReader.MAIN_BASE] = 0x80.toByte()
        assertEquals("801c-7fec", selector.select(main))
    }

    @Test fun translationsFollowEachPalLanguageWithoutAnyMenuPointer() {
        val catalog = FlaweGuideCatalog(program, json)
        val main = fixture()
        val unchanged = main.copyOf()
        assertEquals("Habla con Repeating Tom en la Torre Seiryu.", catalog.objective(main, 6))
        assertEquals("Talk to Repeating Tom in Seiryu Tower", catalog.objective(main, 2))
        assertTrue(catalog.objective(main, 3)!!.startsWith("Parle"))
        assertTrue(catalog.objective(main, 4)!!.startsWith("Parla"))
        assertTrue(catalog.objective(main, 5)!!.startsWith("Sprich"))
        assertArrayEquals(unchanged, main)
    }

    @Test fun everyNonEmptyObjectiveHasAllFourTranslations() {
        val entries = JSONObject(json)
        assertEquals(157, entries.length())
        entries.keys().forEach { key ->
            val entry = entries.getJSONObject(key)
            if (entry.getString("en").isNotBlank()) {
                listOf("es", "fr", "de", "it").forEach {
                    assertTrue("$key/$it", entry.getString(it).isNotBlank())
                }
            }
        }
    }

    @Test fun unsupportedCodeAndShortRamCannotEscapeTheEvaluator() {
        assertNull(selector.select(ByteArray(0)))
        assertNull(FlaweGuideSelector(ByteArray(4) { 0xff.toByte() }).select(fixture()))
        assertNull(FlaweGuideSelector(ByteArray(0x2000)).select(fixture()))
    }
}
