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
    val pointerSlots: IntRange = 0 until 0x40 step 4

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
        "playa", "inn", "posada", "tamer", "digimon", "server", "servidor"
    )

    fun decodeWindow(bytes: ByteArray, start: Int = 0): String? {
        val dw = DwTextDecoder.decode(bytes, start)
        val ascii = decodeAscii(bytes, start)
        return best(listOfNotNull(dw, ascii))
    }

    fun pointers(bytes: ByteArray): List<Long> =
        pointerSlots.map { GameStateReader.u32(bytes, it) }
            .filter { it in 0x80000000L..0x801FFFFFL }

    fun best(candidates: Iterable<String>): String? =
        candidates.map { normalize(it) }
            .filter { score(it, requireHint = false) > Int.MIN_VALUE }
            .maxByOrNull { score(it, requireHint = false) }

    fun find(ram: ByteArray): String? {
        var best: String? = null
        var bestScore = Int.MIN_VALUE

        fun consider(text: String?) {
            val normalized = text?.let(::normalize) ?: return
            val value = score(normalized, requireHint = true)
            if (value > bestScore) {
                best = normalized
                bestScore = value
            }
        }

        var index = 0
        while (index < ram.size) {
            val value = ram[index].toInt() and 0xFF
            when {
                value in 0x0E..0x41 -> {
                    consider(DwTextDecoder.decode(ram, index))
                    index += 4
                }
                value in 0x41..0x5A || value in 0x61..0x7A -> {
                    val ascii = decodeAscii(ram, index)
                    consider(ascii)
                    index += ascii?.length?.coerceAtLeast(1) ?: 1
                }
                else -> index++
            }
        }
        return best
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

    fun score(text: String, requireHint: Boolean = true): Int {
        val words = words(text)
        val letterCount = text.count(Char::isLetter)
        if (letterCount < 8 || words.size < 3) return Int.MIN_VALUE
        val verbCount = words.count { it in englishVerbs || it in spanishVerbs }
        val landmarkCount = words.count { it in landmarks }
        if (requireHint && verbCount == 0 && landmarkCount == 0) return Int.MIN_VALUE
        return verbCount * 1_000 + landmarkCount * 120 + letterCount + words.size * 4
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

    private fun words(text: String): List<String> =
        text.lowercase()
            .split(Regex("[^\\p{L}]+"))
            .filter(String::isNotBlank)

    private fun normalize(text: String): String =
        text.replace('\n', ' ').replace(Regex("\\s+"), " ").trim()
}

object WalkthroughCatalog {
    const val START_PROMPT_ES = "Inicia o carga una partida para activar el panel complementario."
    const val SYNC_PROMPT_ES = "Abre el menú del juego para sincronizar la guía integrada."
    const val START_PROMPT_EN = "Start or load a game to activate the companion panel."
    const val SYNC_PROMPT_EN = "Open the game menu to sync Flawe's integrated walkthrough."
    const val SPANISH_NOTE =
        "Flawe no muestra la guía en el menú español; esta pista la genera el panel."

    fun localized(
        raw: String,
        storyStage: Int,
        language: CompanionLanguage,
        mapId: Int = 0
    ): String {
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
        fallback(storyStage, mapId, language)?.let { hint ->
            return if (language == CompanionLanguage.SPANISH) "$hint $SPANISH_NOTE" else hint
        }
        return when (language) {
            CompanionLanguage.SPANISH -> "$SYNC_PROMPT_ES $SPANISH_NOTE"
            CompanionLanguage.ENGLISH -> SYNC_PROMPT_EN
        }
    }

    private fun String.gameStartedHint(): Boolean =
        this != START_PROMPT_ES && this != START_PROMPT_EN

    private fun fallback(storyStage: Int, mapId: Int, language: CompanionLanguage): String? {
        stageHint(storyStage, language)?.let { return it }
        return mapHint(mapId, language)
    }

    private fun stageHint(storyStage: Int, language: CompanionLanguage): String? =
        when (storyStage) {
            4 -> pair(
                language,
                "Dirígete al este por el Bosque Alambre para llegar a Ciudad Seiryu.",
                "Head east through Wire Forest to reach Seiryu City."
            )
            else -> null
        }

    private fun mapHint(mapId: Int, language: CompanionLanguage): String? =
        when (mapId) {
            in 0x0200..0x021C -> pair(
                language,
                "Termina los recados de Ciudad Asuka y sal hacia Central Park.",
                "Finish the errands in Asuka City and leave toward Central Park."
            )
            0x021D -> pair(
                language,
                "Busca en Central Park la Entrada del Bosque Alambre, al este.",
                "Search Central Park for the Wire Forest Entrance to the east."
            )
            0x021E, 0x0221, 0x0222 -> pair(
                language,
                "Avanza al este por el Bosque Alambre para llegar a Ciudad Seiryu.",
                "Head east through Wire Forest to reach Seiryu City."
            )
            0x021F, 0x0220 -> pair(
                language,
                "Explora la costa de Central Park y vuelve al parque para seguir la historia.",
                "Explore the Central Park coast, then return to the park to continue the story."
            )
            0x022E, 0x022F, 0x0230, 0x0231 -> pair(
                language,
                "Explora Ciudad Seiryu y habla con los habitantes para el siguiente paso.",
                "Explore Seiryu City and talk to the residents for the next story beat."
            )
            0x023E, 0x023F, 0x0240, 0x0241 -> pair(
                language,
                "Explora Ciudad Suzaku y sigue las pistas del sector sur.",
                "Explore Suzaku City and follow the leads in the south sector."
            )
            0x025D, 0x025E -> pair(
                language,
                "Explora Ciudad Byakko y el desierto del sector oeste.",
                "Explore Byakko City and the west-sector desert."
            )
            in 0x0780..0x0855 -> pair(
                language,
                "Continúa la historia en el servidor Amaterasu y habla con los NPC de la ciudad actual.",
                "Continue the story on the Amaterasu Server and talk to NPCs in the current city."
            )
            else -> null
        }

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

    private fun pair(language: CompanionLanguage, spanish: String, english: String): String =
        if (language == CompanionLanguage.ENGLISH) english else spanish

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
            "Explore Central Park and find the Wire Forest Entrance."
    )
}
