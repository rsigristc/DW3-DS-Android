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
    fun rejectsMenuPromptsAndNpcDialogue() {
        assertTrue(!WalkthroughTextFinder.isWalkthroughHint("Open which booster?"))
        assertTrue(
            !WalkthroughTextFinder.isWalkthroughHint(
                "GatomonWe go to the Administration Center a lot.But we've never seen other people."
            )
        )
        assertTrue(WalkthroughTextFinder.isWalkthroughHint("Talk to Repeating Tom in Seiryu Tower."))
        assertTrue(WalkthroughTextFinder.isStartBoxHint("Talk to Repeating Tom in Seiryu Tower."))
        assertTrue(WalkthroughTextFinder.isStartBoxHint("Head North of 'East Wire Forest' to reach Protocol Ruins"))
        assertTrue(!WalkthroughTextFinder.isStartBoxHint("Return to Seiryu Tower and defeat Seiryu Leader."))
    }

    @Test
    fun displayedHintPrefersStartTitleOverLaterQuest() {
        val ram = ByteArray(0x400)
        val later = "Return to Seiryu Tower and defeat Seiryu Leader."
        val current = "Head North of 'East Wire Forest' to reach Protocol Ruins"
        later.toByteArray(Charsets.US_ASCII).copyInto(ram, 0x20)
        "Protocol Ruins".toByteArray(Charsets.US_ASCII).copyInto(ram, 0x180)
        current.toByteArray(Charsets.US_ASCII).copyInto(ram, 0x1A0)
        assertEquals(current, WalkthroughTextFinder.displayedHint(ram))
    }

    @Test
    fun displayedHintPrefersRepeatingTomOverGuilmonOnCurrentMap() {
        val ram = ByteArray(0x400)
        val stale = "Talk to Guilmon in Asuka Inn 2F."
        val live = "Talk to Repeating Tom in Seiryu Tower."
        stale.toByteArray(Charsets.US_ASCII).copyInto(ram, 0x20)
        "Asuka Inn 2F".toByteArray(Charsets.US_ASCII).copyInto(ram, 0x80)
        "Seiryu Tower".toByteArray(Charsets.US_ASCII).copyInto(ram, 0x180)
        live.toByteArray(Charsets.US_ASCII).copyInto(ram, 0x1A0)
        assertEquals(live, WalkthroughTextFinder.displayedHint(ram))
    }

    @Test
    fun knownAsciiPrefersStandaloneRepeatingTomHeading() {
        val ram = ByteArray(0x400) { 0x7F }
        packTitleAndHint(ram, 0x10, "Protocol Ruins", "Head North of 'East Wire Forest' to reach Protocol Ruins")
        for (index in 0x1C0 until 0x200) ram[index] = 0
        packTitleAndHint(ram, 0x200, "Repeating Tom", "Talk to Repeating Tom in Seiryu Tower.")
        assertEquals(
            "Talk to Repeating Tom in Seiryu Tower.",
            WalkthroughTextFinder.findKnownAscii(ram)
        )
    }

    @Test
    fun knownAsciiPrefersStandaloneProtocolHeading() {
        val ram = ByteArray(0x400) { 0x7F }
        packTitleAndHint(ram, 0x10, "Repeating Tom", "Talk to Repeating Tom in Seiryu Tower.")
        for (index in 0x1C0 until 0x200) ram[index] = 0
        packTitleAndHint(
            ram,
            0x200,
            "Protocol Ruins",
            "Head North of 'East Wire Forest' to reach Protocol Ruins"
        )
        assertEquals(
            "Head North of 'East Wire Forest' to reach Protocol Ruins",
            WalkthroughTextFinder.findKnownAscii(ram)
        )
    }

    @Test
    fun visibleAsciiHintMatchesDistantStartTitle() {
        val ram = ByteArray(0x400) { 0x7F }
        "Protocol Ruins".toByteArray(Charsets.US_ASCII).copyInto(ram, 0x10)
        ram[0x10 + "Protocol Ruins".length] = 0
        val live = "Head North of 'East Wire Forest' to reach Protocol Ruins"
        live.toByteArray(Charsets.US_ASCII).copyInto(ram, 0x200)
        ram[0x200 + live.length] = 0
        assertEquals(live, WalkthroughTextFinder.displayedHint(ram))
    }

    @Test
    fun displayedHintKeepsUniqueKnownStemWhenOtherQuestHasNoTitle() {
        val ram = ByteArray(0x400)
        val stale = "Talk to Guilmon in Asuka Inn 2F."
        val live = "Talk to Repeating Tom in Seiryu Tower."
        stale.toByteArray(Charsets.US_ASCII).copyInto(ram, 0x20)
        live.toByteArray(Charsets.US_ASCII).copyInto(ram, 0x1A0)
        assertEquals(live, WalkthroughTextFinder.displayedHint(ram))
    }

    @Test
    fun knownDwFindsRepeatingTomWithStandaloneHeading() {
        val ram = ByteArray(0x400) { 0x7F }
        for (index in 0x1C0 until 0x200) ram[index] = 0
        val title = DwTextDecoder.encode("Repeating Tom")
        title.copyInto(ram, 0x200)
        ram[0x200 + title.size] = 0
        val body = DwTextDecoder.encode("Talk to Repeating Tom in Seiryu Tower.")
        body.copyInto(ram, 0x200 + title.size + 1)
        assertEquals(
            "Talk to Repeating Tom in Seiryu Tower.",
            WalkthroughTextFinder.findKnown(ram)
        )
    }

    @Test
    fun startBoxPrefersPaddedTomOverProtocolInTheScriptTable() {
        val ram = ByteArray(0x400) { 0x7F }
        packTitleAndHint(ram, 0x10, "Protocol Ruins", "Head North of 'East Wire Forest' to reach Protocol Ruins")
        for (index in 0x1C0 until 0x200) ram[index] = 0
        packTitleAndHint(ram, 0x200, "Seiryu Tower", "Talk to Repeating Tom in Seiryu Tower.")
        assertEquals(
            "Talk to Repeating Tom in Seiryu Tower.",
            WalkthroughTextFinder.displayedHint(ram)
        )
    }

    @Test
    fun startBoxPrefersPaddedProtocolOverLeaderInTheScriptTable() {
        val ram = ByteArray(0x400) { 0x7F }
        packTitleAndHint(ram, 0x10, "Seiryu Tower", "Return to Seiryu Tower and defeat Seiryu Leader.")
        for (index in 0x1C0 until 0x200) ram[index] = 0
        packTitleAndHint(
            ram,
            0x200,
            "Protocol Ruins",
            "Head North of 'East Wire Forest' to reach Protocol Ruins"
        )
        assertEquals(
            "Head North of 'East Wire Forest' to reach Protocol Ruins",
            WalkthroughTextFinder.displayedHint(ram)
        )
    }

    @Test
    fun displayedHintIgnoresPackedScriptTable() {
        val ram = ByteArray(0x200) { 0x7F }
        val stale = "Talk to Repeating Tom in Seiryu Tower."
        val later = "Head North of 'East Wire Forest' to reach Protocol Ruins"
        stale.toByteArray(Charsets.US_ASCII).copyInto(ram, 0x10)
        ram[0x10 + stale.length] = 0
        later.toByteArray(Charsets.US_ASCII).copyInto(ram, 0x10 + stale.length + 1)
        ram[0x10 + stale.length + 1 + later.length] = 0
        assertNull(WalkthroughTextFinder.displayedHint(ram))
    }

    @Test
    fun firstScratchPointerKeepsCurrentQuestOverLaterHint() {
        assertEquals(
            "Head North of 'East Wire Forest' to reach Protocol Ruins",
            WalkthroughTextFinder.first(
                listOf(
                    "Head North of 'East Wire Forest' to reach Protocol Ruins",
                    "Return to Seiryu Tower and defeat Seiryu Leader."
                )
            )
        )
    }

    @Test
    fun prefersNewFlaweHintOverStaleTowerObjective() {
        val next = "Head North of 'East Wire Forest' to reach Protocol Ruins"
        val stale = "Talk to Repeating Tom in Seiryu Tower."
        assertTrue(WalkthroughTextFinder.isWalkthroughHint(next))
        assertEquals(next, WalkthroughTextFinder.best(listOf(stale, next)))
    }

    @Test
    fun prefersHintThatNamesTheCurrentMap() {
        val location = WalkthroughTextFinder.locationKeywords("Torre Seiryu")
        val stale = "Head east through Wire Forest to reach Seiryu City."
        val live = "Talk to Repeating Tom in Seiryu Tower."
        assertTrue(
            WalkthroughTextFinder.score(live, false, location) >
                WalkthroughTextFinder.score(stale, false, location)
        )
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
    fun doesNotInventMapObjectivesWhenFlaweTextIsMissing() {
        assertEquals(
            WalkthroughCatalog.SYNC_PROMPT_ES,
            WalkthroughCatalog.localized(
                WalkthroughCatalog.SYNC_PROMPT_ES,
                4,
                CompanionLanguage.SPANISH,
                0x022E
            )
        )
        assertEquals(
            WalkthroughCatalog.SYNC_PROMPT_EN,
            WalkthroughCatalog.localized(
                WalkthroughCatalog.SYNC_PROMPT_ES,
                4,
                CompanionLanguage.ENGLISH,
                0x0230
            )
        )
        val text = WalkthroughCatalog.localized(
            WalkthroughCatalog.SYNC_PROMPT_ES,
            4,
            CompanionLanguage.ENGLISH,
            0x022E
        )
        assertTrue(!text.contains("Seiryu"))
        assertTrue(!text.contains("Explore"))
    }

    @Test
    fun translatesProtocolRuinsHintWhenCompanionIsSpanish() {
        assertEquals(
            "Ve al norte del Bosque Alambre Este para llegar a las Ruinas Protocolo.",
            WalkthroughCatalog.localized(
                "Head North of 'East Wire Forest' to reach Protocol Ruins",
                4,
                CompanionLanguage.SPANISH,
                0x0230
            )
        )
    }

    @Test
    fun translatesRepeatingTomWhenCompanionIsSpanish() {
        assertEquals(
            "Habla con Repeating Tom en la Torre Seiryu.",
            WalkthroughCatalog.localized(
                "Talk to Repeating Tom in Seiryu Tower.",
                4,
                CompanionLanguage.SPANISH,
                0x0230
            )
        )
    }

    @Test
    fun keepsUsaWalkthroughDisabledWithoutMapHints() {
        assertEquals(
            WalkthroughCatalog.UNAVAILABLE_EN,
            WalkthroughCatalog.localized(
                WalkthroughCatalog.UNAVAILABLE_ES,
                4,
                CompanionLanguage.ENGLISH,
                0x0230
            )
        )
        assertTrue(
            !WalkthroughCatalog.localized(
                WalkthroughCatalog.UNAVAILABLE_ES,
                4,
                CompanionLanguage.SPANISH,
                0x0230
            ).contains("Seiryu")
        )
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

private fun packTitleAndHint(ram: ByteArray, offset: Int, title: String, hint: String) {
    title.toByteArray(Charsets.US_ASCII).copyInto(ram, offset)
    ram[offset + title.length] = 0
    hint.toByteArray(Charsets.US_ASCII).copyInto(ram, offset + title.length + 1)
    ram[offset + title.length + 1 + hint.length] = 0
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
