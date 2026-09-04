package com.digitaladventure.dw2003.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WalkthroughTextFinderTest {
    @Test
    fun readsDwEncodedEnglishWalkthroughFromScratchPointer() {
        val ram = ByteArray(0x200)
        val textOffset = 0x80
        encodeDw("Head East through Wire Forest to reach Seiryu City.").copyInto(ram, textOffset)
        GameMemoryWrite.writeU32(ram, 0x08, 0x80000000L + textOffset)

        val decoded = WalkthroughTextFinder.pointers(ram.copyOfRange(0, 0x40)).mapNotNull { pointer ->
            WalkthroughTextFinder.decodeWindow(ram, (pointer and 0x1FFFFF).toInt())
        }
        assertEquals(
            "Head East through Wire Forest to reach Seiryu City.",
            WalkthroughTextFinder.best(decoded)
        )
        assertEquals(
            CompanionLanguage.ENGLISH,
            WalkthroughTextFinder.language(WalkthroughTextFinder.best(decoded)!!)
        )
    }

    @Test
    fun readsAsciiWalkthroughCopiedIntoScratch() {
        val ram = ByteArray(0x80)
        val ascii = "Talk to the Tamer in Asuka City then enter Central Park.\u0000".toByteArray(Charsets.US_ASCII)
        ascii.copyInto(ram, 0)

        assertEquals(
            "Talk to the Tamer in Asuka City then enter Central Park.",
            WalkthroughTextFinder.decodeWindow(ram)
        )
        assertEquals(CompanionLanguage.ENGLISH, WalkthroughTextFinder.language(WalkthroughTextFinder.decodeWindow(ram)!!))
    }

    @Test
    fun findsAsciiWalkthroughInFullRamScan() {
        val ram = ByteArray(0x400)
        val text = "Search Central Park for the Wire Forest Entrance to the east."
        text.toByteArray(Charsets.US_ASCII).copyInto(ram, 0x120)

        assertEquals(text, WalkthroughTextFinder.find(ram))
    }

    @Test
    fun ignoresMenuNoiseWithoutHints() {
        val ram = ByteArray(0x80)
        encodeDw("Item Sort Map Status").copyInto(ram, 0)
        assertNull(WalkthroughTextFinder.find(ram))
    }

    @Test
    fun detectsSpanishHintLanguage() {
        assertEquals(
            CompanionLanguage.SPANISH,
            WalkthroughTextFinder.language("Dirígete al este por el Bosque Alambre para llegar a Ciudad Seiryu.")
        )
    }
}

class WalkthroughCatalogTest {
    @Test
    fun translatesLiveEnglishTextWhenCompanionIsSpanish() {
        assertEquals(
            "Dirígete al este por el Bosque Alambre para llegar a Ciudad Seiryu.",
            WalkthroughCatalog.localized(
                "Head East through Wire Forest to reach Seiryu City.",
                4,
                CompanionLanguage.SPANISH,
                0x0221
            )
        )
    }

    @Test
    fun usesEnglishSyncPromptWhenFlaweTextIsMissing() {
        assertEquals(
            WalkthroughCatalog.SYNC_PROMPT_EN,
            WalkthroughCatalog.localized(
                WalkthroughCatalog.SYNC_PROMPT_ES,
                0,
                CompanionLanguage.ENGLISH,
                0
            )
        )
    }

    @Test
    fun providesSpanishCompanionHintWhenFlaweHasNoSpanishGuide() {
        val text = WalkthroughCatalog.localized(
            WalkthroughCatalog.SYNC_PROMPT_ES,
            4,
            CompanionLanguage.SPANISH,
            0x021D
        )
        assertTrue(text.contains("Central Park") || text.contains("Bosque Alambre"))
        assertTrue(text.contains(WalkthroughCatalog.SPANISH_NOTE))
    }

    @Test
    fun usesNorthSectorCompanionHintForBootMountain() {
        val text = WalkthroughCatalog.localized(
            WalkthroughCatalog.SYNC_PROMPT_ES,
            0,
            CompanionLanguage.ENGLISH,
            0x0261
        )
        assertTrue(text.contains("Boot Mountain"))
        assertTrue(text.contains("Genbu City"))
    }

    @Test
    fun keepsStartPromptLocalized() {
        assertEquals(
            WalkthroughCatalog.START_PROMPT_EN,
            WalkthroughCatalog.localized(
                WalkthroughCatalog.START_PROMPT_ES,
                0,
                CompanionLanguage.ENGLISH
            )
        )
    }
}

class CompanionLanguageResolverTest {
    @Test
    fun autoUsesDetectedLanguageAndFallsBackToSpanish() {
        assertEquals(
            CompanionLanguage.ENGLISH,
            CompanionLanguageResolver.resolve(CompanionLanguageSetting.AUTO, CompanionLanguage.ENGLISH)
        )
        assertEquals(
            CompanionLanguage.SPANISH,
            CompanionLanguageResolver.resolve(CompanionLanguageSetting.AUTO, null)
        )
        assertEquals(
            CompanionLanguage.SPANISH,
            CompanionLanguageResolver.resolve(CompanionLanguageSetting.SPANISH, CompanionLanguage.ENGLISH)
        )
    }
}

private object GameMemoryWrite {
    fun writeU32(target: ByteArray, offset: Int, value: Long) {
        repeat(4) { index ->
            target[offset + index] = (value ushr (index * 8)).toByte()
        }
    }
}

private fun encodeDw(text: String): ByteArray {
    val out = ArrayList<Byte>()
    text.forEach { character ->
        when (character) {
            in 'A'..'Z' -> out += (0x0E + character.code - 'A'.code).toByte()
            in 'a'..'z' -> out += (0x28 + character.code - 'a'.code).toByte()
            ' ' -> {
                out += 0x01
                out += 0x01
            }
            '.' -> {
                out += 0x01
                out += 0x03
            }
            else -> Unit
        }
    }
    out += 0
    return out.toByteArray()
}
