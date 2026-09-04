package com.digitaladventure.dw2003.data

object CheatCodeParser {
    private val PAIR = Regex("""([0-9A-Fa-f]{8})\s+([0-9A-Fa-f]{4})""")

    fun normalize(raw: String): String? {
        val pairs = PAIR.findAll(raw).map { match ->
            "${match.groupValues[1].uppercase()} ${match.groupValues[2].uppercase()}"
        }.toList()
        return pairs.takeIf { it.isNotEmpty() }?.joinToString("+")
    }
}
