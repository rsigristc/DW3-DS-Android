package com.digitaladventure.dw2003.data

enum class CompanionLanguage(val label: String) {
    SPANISH("Español"),
    ENGLISH("English")
}

enum class CompanionLanguageSetting(val label: String) {
    AUTO("Automático / Auto"),
    SPANISH("Español"),
    ENGLISH("English");

    companion object {
        fun fromPreference(value: String?): CompanionLanguageSetting =
            entries.firstOrNull { it.name == value } ?: AUTO
    }
}

object CompanionLanguageResolver {
    fun resolve(setting: CompanionLanguageSetting, detected: CompanionLanguage?): CompanionLanguage =
        when (setting) {
            CompanionLanguageSetting.SPANISH -> CompanionLanguage.SPANISH
            CompanionLanguageSetting.ENGLISH -> CompanionLanguage.ENGLISH
            CompanionLanguageSetting.AUTO -> detected ?: CompanionLanguage.SPANISH
        }
}

/**
 * Flawe's in-game walkthrough is English-only. The European disc stores that
 * text with the DW compact table, but some combined patches also leave a
 * plain ASCII copy in scratch or overlay RAM. Both encodings are accepted.
 */
object WalkthroughTextFinder {
    val pointerSlots: IntProgression = 0 until 0x40 step 4

    private val englishVerbs = setOf(
        "head", "go", "talk", "find", "defeat", "return", "reach",
        "obtain", "bring", "enter", "visit", "search", "meet", "use",
        "proceed", "speak", "collect", "deliver", "follow", "ask", "leave",
        "travel", "move", "check", "open", "climb", "cross", "rescue",
        "help", "take", "give", "show", "report", "investigate", "continue",
        "walk", "look", "wait", "catch", "buy", "train", "challenge", "win"
    )
    private val spanishVerbs = setOf(
        "ve", "dirígete", "dirigete", "habla", "encuentra", "derrota", "vuelve",
        "llega", "consigue", "lleva", "entra", "visita", "busca", "usa",
        "procede", "recoge", "entrega", "sigue", "pregunta", "sal", "viaja",
        "revisa", "abre", "escala", "cruza", "rescata", "ayuda", "toma",
        "muestra", "informa", "investiga", "continúa", "continua", "camina",
        "mira", "espera", "atrapa", "compra", "entrena", "reta", "gana"
    )
    private val landmarks = setOf(
        "asuka", "seiryu", "suzaku", "byakko", "amaterasu", "park", "parque",
        "forest", "bosque", "wire", "alambre", "city", "ciudad", "beach",
        "playa",         "inn", "posada", "tamer", "digimon", "server", "servidor",
        "tower", "torre", "tom", "protocol", "protocolo", "ruins", "ruinas"
    )
    private val GENERIC_PLACE_WORDS = setOf(
        "seiryu", "suzaku", "byakko", "genbu", "asuka", "amaterasu", "digimon"
    )

    fun decodeWindow(bytes: ByteArray, start: Int = 0): String? {
        val dw = DwTextDecoder.decode(bytes, start)
        val ascii = decodeAscii(bytes, start)
        return best(listOfNotNull(dw, ascii))
    }

    fun pointers(bytes: ByteArray): List<Long> =
        pointerSlots.map { GameStateReader.u32(bytes, it) }
            .filter { it in 0x80000000L..0x801FFFFFL }

    fun isWalkthroughHint(text: String): Boolean =
        score(normalize(text), requireHint = true) > Int.MIN_VALUE

    fun best(candidates: Iterable<String>): String? =
        candidates.map { normalize(it) }
            .filter(::isWalkthroughHint)
            .maxByOrNull { score(it, requireHint = true) }

    fun first(candidates: Iterable<String>): String? =
        candidates.map { normalize(it) }.firstOrNull(::isWalkthroughHint)

    fun find(ram: ByteArray): String? = displayedHint(ram)

    fun findAll(ram: ByteArray): List<String> = findAllIndexed(ram).map { it.first }

    /**
     * Fast exact/stem search for Flawe's known START lines. The on-screen
     * English box is ASCII; the next mission stays in the same table, so the
     * heading with more padding (Repeating Tom / Protocol Ruins) wins.
     */
    fun findKnownAscii(ram: ByteArray): String? = findKnown(ram)

    fun findKnown(ram: ByteArray): String? {
        val hits = LinkedHashMap<String, KnownHit>()
        KNOWN_STEMS.forEach { stem ->
            considerKnown(hits, ram, stem, indexOfAscii(ram, stem), ascii = true)
            considerKnown(hits, ram, stem, indexOfBytes(ram, DwTextDecoder.encode(stem)), ascii = false)
        }
        if (hits.isEmpty()) return null
        val ranked = hits.values.map { hit ->
            val titlePad = knownTitle(hit.text)?.let { title ->
                indexOfStandaloneTitle(ram, title)?.let { leadingZeros(ram, it) }
            } ?: -1
            hit to titlePad
        }
        val bestTitlePad = ranked.maxOf { it.second }
        if (bestTitlePad >= 0) {
            val byTitle = ranked.filter { it.second == bestTitlePad }
            val live = byTitle.filter { !it.first.text.contains("defeat seiryu leader", ignoreCase = true) }
            return (live.ifEmpty { byTitle }).maxBy { it.first.pad }.first.text
        }
        if (hits.size == 1) return hits.values.single().text
        val padded = hits.values.filter { it.pad >= WIDGET_MIN_PAD }
        val live = padded.filter { !it.text.contains("defeat seiryu leader", ignoreCase = true) }
        return (live.ifEmpty { padded }).maxByOrNull { it.pad }?.text
    }

    /**
     * Flawe's START box is a short title immediately followed by a hint that
     * repeats that title at the end ("Protocol Ruins" + "… to reach Protocol
     * Ruins"). The next mission ("Return to Seiryu Tower and defeat Seiryu
     * Leader") stays in the string table and must not win. Padding is measured
     * before the title, because title and body are packed with a single NUL.
     */
    fun displayedHint(ram: ByteArray): String? {
        findKnown(ram)?.let { return it }
        val boxes = startBoxes(ram)
        if (boxes.isEmpty()) {
            val hints = findAllIndexed(ram)
            return hints.singleOrNull()
                ?.takeIf { leadingZeros(ram, it.second) >= WIDGET_MIN_PAD }
                ?.first
                ?: visibleStartHint(ram)
        }
        val echoed = boxes.filter { it.echoesTitle }
        if (echoed.isEmpty()) {
            return boxes.filter { it.titlePad >= WIDGET_MIN_PAD }
                .maxByOrNull { it.titlePad }
                ?.hint
        }
        val bestPad = echoed.maxOf { it.titlePad }
        val winners = echoed.filter { it.titlePad == bestPad }
        if (winners.size == 1 || bestPad >= WIDGET_MIN_PAD) {
            return winners.maxBy { score(it.hint) }.hint
        }
        return visibleStartHint(ram)
    }

    fun isStartBoxHint(text: String): Boolean =
        startBoxTitle(text) != null

    private data class StartBox(
        val hint: String,
        val hintOffset: Int,
        val title: String,
        val titleOffset: Int,
        val titlePad: Int,
        val echoesTitle: Boolean
    )

    private fun startBoxes(ram: ByteArray): List<StartBox> {
        val hints = findAllIndexed(ram)
        if (hints.isEmpty()) return emptyList()
        val catalogTitles = OverlayLocationFinder.labels(ram)
        return hints.mapNotNull { (hint, hintOffset) ->
            val catalog = catalogTitles
                .filter { label ->
                    label.offset < hintOffset &&
                        hintOffset - label.offset <= 96 &&
                        hint.contains(label.text, ignoreCase = true)
                }
                .maxByOrNull { it.offset }
            val previous = titleBefore(ram, hintOffset)
            val title = when {
                catalog != null && previous != null ->
                    if (catalog.offset >= previous.second) catalog.text to catalog.offset
                    else previous
                catalog != null -> catalog.text to catalog.offset
                previous != null -> previous
                else -> return@mapNotNull null
            }
            val echo = echoesTitle(hint, title.first)
            if (!echo && catalog == null) return@mapNotNull null
            StartBox(
                hint = hint,
                hintOffset = hintOffset,
                title = title.first,
                titleOffset = title.second,
                titlePad = leadingZeros(ram, title.second),
                echoesTitle = echo
            )
        }
    }

    private fun titleBefore(ram: ByteArray, hintOffset: Int): Pair<String, Int>? {
        var index = hintOffset - 1
        while (index >= 0 && ram[index].toInt() == 0 && hintOffset - index <= 8) index--
        if (index < 0) return null
        var start = index
        while (start > 0 && ram[start - 1].toInt() != 0 && hintOffset - start <= 64) start--
        val text = decodeLabel(ram, start) ?: return null
        if (!isStartTitle(text) || hintOffset - start > 96) return null
        return text to start
    }

    private fun isStartTitle(text: String): Boolean =
        text.length in 4..40 &&
            text.count(Char::isLetter) >= 4 &&
            !isWalkthroughHint(text)

    private fun echoesTitle(hint: String, title: String): Boolean {
        val body = hint.lowercase().trim().trimEnd('.')
        val head = title.lowercase().trim().trimEnd('.')
        return head.isNotBlank() && body.endsWith(head)
    }

    private fun startBoxTitle(hint: String): String? {
        val tail = normalize(hint).lowercase().trim().trimEnd('.')
        return START_TITLE_TAILS.firstOrNull { tail.endsWith(it) }
    }

    private fun visibleStartHint(ram: ByteArray): String? {
        val ascii = findAllIndexed(ram, asciiOnly = true).map { it.first }.filter(::isStartBoxHint)
        val labels = OverlayLocationFinder.labels(ram)
        val echoed = ascii.filter { hint ->
            labels.any { label -> echoesTitle(hint, label.text) }
        }
        return when {
            echoed.size == 1 -> echoed.single()
            echoed.isNotEmpty() -> echoed.maxBy { score(it) }
            ascii.size == 1 -> ascii.single()
            else -> findAll(ram).filter(::isStartBoxHint).distinct().singleOrNull()
        }
    }

    private fun findAllIndexed(ram: ByteArray, asciiOnly: Boolean = false): List<Pair<String, Int>> {
        val found = LinkedHashMap<String, Int>()
        fun consider(text: String?, offset: Int) {
            val normalized = text?.let(::normalize) ?: return
            if (!isWalkthroughHint(normalized)) return
            found.putIfAbsent(normalized, offset)
        }
        var index = 0
        while (index < ram.size) {
            val value = ram[index].toInt() and 0xFF
            when {
                !asciiOnly && value in 0x0E..0x41 -> {
                    consider(DwTextDecoder.decode(ram, index), index)
                    index += 4
                }
                value in 0x41..0x5A || value in 0x61..0x7A -> {
                    val ascii = decodeAscii(ram, index)
                    consider(ascii, index)
                    index += ascii?.length?.coerceAtLeast(1) ?: 1
                }
                else -> index++
            }
        }
        return found.entries.map { it.key to it.value }
    }

    fun language(text: String): CompanionLanguage? {
        val words = words(text)
        val englishHits = words.count { it in englishVerbs }
        val spanishHits = words.count { it in spanishVerbs }
        return when {
            englishHits > spanishHits -> CompanionLanguage.ENGLISH
            spanishHits > englishHits -> CompanionLanguage.SPANISH
            words.any { it in englishVerbs } -> CompanionLanguage.ENGLISH
            words.any { it in spanishVerbs } -> CompanionLanguage.SPANISH
            else -> null
        }
    }

    fun keywords(text: String): Set<String> = words(text).toSet()

    fun locationKeywords(vararg labels: String): Set<String> {
        val words = labels.flatMap { keywords(it) }.toMutableSet()
        if ("torre" in words) words += "tower"
        if ("tower" in words) words += "torre"
        if ("ciudad" in words) words += "city"
        if ("city" in words) words += "ciudad"
        if ("bosque" in words) words += "forest"
        if ("forest" in words) words += "bosque"
        if ("parque" in words) words += "park"
        if ("park" in words) words += "parque"
        return words
    }

    fun score(text: String, requireHint: Boolean = true, locationWords: Set<String> = emptySet()): Int {
        val normalized = normalize(text)
        val words = words(normalized)
        if (normalized.count(Char::isLetter) < 12 || words.size !in 4..28) return Int.MIN_VALUE
        if (normalized.length > 160) return Int.MIN_VALUE
        if (DIALOGUE_NOISE.containsMatchIn(normalized) || GLUED_SPEAKER.containsMatchIn(normalized)) {
            return Int.MIN_VALUE
        }
        val first = words.firstOrNull() ?: return Int.MIN_VALUE
        val startsWithVerb = first in englishVerbs || first in spanishVerbs
        val verbCount = words.count { it in englishVerbs || it in spanishVerbs }
        val landmarkCount = words.count { it in landmarks }
        val locationCount = words.count { it in locationWords }
        val distinctive = locationWords - GENERIC_PLACE_WORDS
        val distinctiveHits = words.count { it in distinctive }
        if (requireHint && (!startsWithVerb || landmarkCount == 0)) return Int.MIN_VALUE
        if (!requireHint && verbCount == 0 && landmarkCount == 0) return Int.MIN_VALUE
        return verbCount * 1_000 +
            landmarkCount * 120 +
            locationCount * 500 +
            distinctiveHits * 800 +
            (if (startsWithVerb) 400 else 0) -
            (normalized.length - 80).coerceAtLeast(0) * 6
    }

    private fun leadingZeros(ram: ByteArray, offset: Int): Int {
        var count = 0
        var index = offset - 1
        while (index >= 0 && ram[index].toInt() == 0 && count < 64) {
            count++
            index--
        }
        return count
    }

    private fun decodeAscii(bytes: ByteArray, start: Int): String? {
        val out = StringBuilder()
        var index = start.coerceAtLeast(0)
        while (index < bytes.size && out.length < 240) {
            when (val value = bytes[index].toInt() and 0xFF) {
                0 -> break
                0x0A, 0x0D -> out.append(' ')
                in 0x20..0x7E -> out.append(value.toChar())
                else -> if (out.length < 8) return null else break
            }
            index++
        }
        val normalized = normalize(out.toString())
        return normalized.takeIf { it.count(Char::isLetter) >= 8 }
    }

    private fun decodeLabel(bytes: ByteArray, start: Int): String? {
        val ascii = decodeAscii(bytes, start)
        if (ascii != null && isStartTitle(ascii)) return ascii
        val shortAscii = decodeShortAscii(bytes, start)
        if (shortAscii != null && isStartTitle(shortAscii)) return shortAscii
        val dw = normalize(DwTextDecoder.decode(bytes, start, 48))
        return dw.takeIf { isStartTitle(it) }
    }

    private fun decodeShortAscii(bytes: ByteArray, start: Int): String? {
        val out = StringBuilder()
        var index = start.coerceAtLeast(0)
        while (index < bytes.size && out.length < 48) {
            when (val value = bytes[index].toInt() and 0xFF) {
                0 -> break
                0x0A, 0x0D -> out.append(' ')
                in 0x20..0x7E -> out.append(value.toChar())
                else -> if (out.length < 3) return null else break
            }
            index++
        }
        return normalize(out.toString()).takeIf { it.length in 4..40 }
    }

    private fun words(text: String): List<String> =
        text.lowercase()
            .split(Regex("[^\\p{L}]+"))
            .filter(String::isNotBlank)

    private fun normalize(text: String): String =
        text.replace('\n', ' ').replace(Regex("\\s+"), " ").trim()

    private data class KnownHit(val text: String, val offset: Int, val pad: Int, val stem: String)

    private fun knownTitle(hint: String): String? = when {
        hint.contains("Repeating Tom", ignoreCase = true) -> "Repeating Tom"
        hint.contains("Protocol Ruins", ignoreCase = true) -> "Protocol Ruins"
        else -> null
    }

    private fun considerKnown(
        hits: MutableMap<String, KnownHit>,
        ram: ByteArray,
        stem: String,
        offset: Int?,
        ascii: Boolean
    ) {
        if (offset == null) return
        val text = if (ascii) {
            decodeAscii(ram, offset) ?: return
        } else {
            normalize(DwTextDecoder.decode(ram, offset))
        }
        if (!isWalkthroughHint(text) && !isStartBoxHint(text)) return
        val normalized = normalize(text)
        val hit = KnownHit(normalized, offset, leadingZeros(ram, offset), stem)
        val previous = hits[normalized]
        if (previous == null || hit.pad > previous.pad) hits[normalized] = hit
    }

    private fun indexOfStandaloneTitle(ram: ByteArray, title: String): Int? =
        indexOfStandalone(ram, title.toByteArray(Charsets.US_ASCII))
            ?: indexOfStandalone(ram, DwTextDecoder.encode(title))

    private fun indexOfStandalone(ram: ByteArray, needle: ByteArray): Int? {
        var from = 0
        while (true) {
            val offset = indexOfBytes(ram, needle, from) ?: return null
            if (offset == 0) return offset
            val previous = ram[offset - 1].toInt() and 0xFF
            if (previous == 0 || previous == 0x0A || previous == 0x0D) return offset
            from = offset + 1
        }
    }

    private fun indexOfBytes(ram: ByteArray, needle: ByteArray, from: Int = 0): Int? {
        if (needle.isEmpty() || needle.size > ram.size) return null
        val last = ram.size - needle.size
        var index = from.coerceAtLeast(0)
        while (index <= last) {
            var match = true
            for (cursor in needle.indices) {
                if (ram[index + cursor] != needle[cursor]) {
                    match = false
                    break
                }
            }
            if (match) return index
            index++
        }
        return null
    }

    private fun indexOfAscii(ram: ByteArray, text: String, from: Int = 0): Int? =
        indexOfBytes(ram, text.toByteArray(Charsets.US_ASCII), from)

    private const val WIDGET_MIN_PAD = 8
    private val KNOWN_STEMS = listOf(
        "Talk to Repeating Tom",
        "Head North of",
        "Return to Seiryu Tower and defeat"
    )
    private val START_TITLE_TAILS = listOf(
        "protocol ruins", "seiryu tower", "asuka inn 2f", "asuka inn 1f",
        "seiryu city", "east wire forest", "wire forest", "central park"
    )

    private val DIALOGUE_NOISE = Regex(
        """\?|\bwhich\b|\bi'm\b|\bi am\b|\bwe've\b|\bsorry\b""",
        RegexOption.IGNORE_CASE
    )
    private val GLUED_SPEAKER = Regex("""[a-z][A-Z]""")
}

object WalkthroughCatalog {
    const val START_PROMPT_ES = "Inicia o carga una partida para activar el panel complementario."
    const val SYNC_PROMPT_ES = "Abre el menú del juego para sincronizar la guía integrada."
    const val START_PROMPT_EN = "Start or load a game to activate the companion panel."
    const val SYNC_PROMPT_EN = "Open the game menu to sync Flawe's integrated walkthrough."
    const val UNAVAILABLE_ES =
        "Esta versión USA no incluye la guía de Flawe. El panel no muestra objetivos del mod."
    const val UNAVAILABLE_EN =
        "This USA version has no Flawe walkthrough. The companion does not show the mod's objectives."

    fun localized(
        raw: String,
        storyStage: Int,
        language: CompanionLanguage,
        mapId: Int = 0
    ): String {
        if (raw == UNAVAILABLE_ES || raw == UNAVAILABLE_EN) {
            return when (language) {
                CompanionLanguage.SPANISH -> UNAVAILABLE_ES
                CompanionLanguage.ENGLISH -> UNAVAILABLE_EN
            }
        }
        val live = raw.takeUnless { it == START_PROMPT_ES || it == SYNC_PROMPT_ES || it == START_PROMPT_EN || it == SYNC_PROMPT_EN }
        if (live != null) {
            return translateKnown(live, language)
        }
        if (!raw.gameStartedHint()) {
            return when (language) {
                CompanionLanguage.SPANISH -> START_PROMPT_ES
                CompanionLanguage.ENGLISH -> START_PROMPT_EN
            }
        }
        return when (language) {
            CompanionLanguage.SPANISH -> SYNC_PROMPT_ES
            CompanionLanguage.ENGLISH -> SYNC_PROMPT_EN
        }
    }

    private fun String.gameStartedHint(): Boolean =
        this != START_PROMPT_ES && this != START_PROMPT_EN

    private fun translateKnown(text: String, language: CompanionLanguage): String {
        val normalized = text.replace('\n', ' ').replace(Regex("\\s+"), " ").trim()
        knownPairs.forEach { (spanish, english) ->
            if (language == CompanionLanguage.SPANISH && normalized.equals(english, ignoreCase = true)) {
                return spanish
            }
            if (language == CompanionLanguage.ENGLISH && normalized.equals(spanish, ignoreCase = true)) {
                return english
            }
        }
        return normalized
    }

    private val knownPairs = listOf(
        "Dirígete al este por el Bosque Alambre para llegar a Ciudad Seiryu." to
            "Head East through Wire Forest to reach Seiryu City.",
        "Dirígete al este por el Bosque Alambre para llegar a Ciudad Seiryu." to
            "Head east through Wire Forest to reach Seiryu City.",
        "Busca en Central Park la Entrada del Bosque Alambre, al este." to
            "Search Central Park for the Wire Forest Entrance to the east.",
        "Termina los recados de Ciudad Asuka y sal hacia Central Park." to
            "Finish the errands in Asuka City and leave toward Central Park.",
        "Explora Central Park y localiza la entrada al Bosque Alambre." to
            "Explore Central Park and find the Wire Forest Entrance.",
        "Habla con Repeating Tom en la Torre Seiryu." to
            "Talk to Repeating Tom in Seiryu Tower.",
        "Ve al norte del Bosque Alambre Este para llegar a las Ruinas Protocolo." to
            "Head North of 'East Wire Forest' to reach Protocol Ruins",
        "Vuelve a la Torre Seiryu y derrota al Líder Seiryu." to
            "Return to Seiryu Tower and defeat Seiryu Leader."
    )
}
