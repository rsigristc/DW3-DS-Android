package com.digitaladventure.dw2003.data

/**
 * Map names drawn on the transition banner and Flawe/USA world map live in
 * overlay RAM. AREA/MAP_ID often still hold the previous room.
 */
object OverlayLocationFinder {
    data class Label(val id: Int, val text: String, val offset: Int)

    fun labels(ram: ByteArray): List<Label> {
        val found = LinkedHashMap<Int, Label>()
        fun consider(text: String?, offset: Int) {
            val key = text?.trim()?.lowercase() ?: return
            val alias = aliasByName[key] ?: return
            found.putIfAbsent(alias.first, Label(alias.first, alias.second, offset))
        }
        var index = 0
        while (index < ram.size) {
            if (ram[index] == 0.toByte()) { index++; continue }
            val start = index
            while (index < ram.size && ram[index] != 0.toByte()) index++
            // A standalone label is short. Never decode arbitrary overlapping
            // windows of machine code or allocate a Regex per RAM byte.
            if (index - start in 4..96) {
                consider(decodeAsciiLabel(ram, start), start)
                consider(DwTextDecoder.decode(ram, start, 48), start)
            }
        }
        return found.values.toList()
    }

    fun prefer(labels: List<Label>): Int? {
        if (labels.isEmpty()) return null
        val specific = labels.filter { it.id !in HUBS }
        return (specific.ifEmpty { labels }).maxBy { it.text.length }.id
    }

    fun stageId(ram: ByteArray): Int? = prefer(labels(ram))

    private fun decodeAsciiLabel(bytes: ByteArray, start: Int): String? {
        val out = StringBuilder()
        var index = start
        while (index < bytes.size && out.length < 48) {
            when (val value = bytes[index].toInt() and 0xFF) {
                0 -> break
                0x0A, 0x0D -> out.append(' ')
                in 0x20..0x7E -> out.append(value.toChar())
                else -> if (out.length < 3) return null else break
            }
            index++
        }
        return out.toString().trim().takeIf { it.length in 4..40 }
    }

    private val HUBS = setOf(
        0x0200, 0x0201, 0x022E, 0x023E, 0x025D, 0x026F,
        0x0780, 0x0810, 0x0825, 0x0845, 0x0855
    )

    private val aliases: List<Pair<Int, String>> by lazy {
        val rows = mutableListOf<Pair<Int, String>>()
        AreaCatalog.knownFieldIds().forEach { id ->
            AreaCatalog.knownName(id)?.let { rows += id to it }
            ENGLISH[id]?.let { rows += id to it }
        }
        rows.sortedByDescending { it.second.length }
    }

    private val aliasByName by lazy {
        aliases.filter { LocationResolver.isStage(it.first) }
            .distinctBy { it.second.lowercase() }
            .associateBy { it.second.lowercase() }
    }

    private val ENGLISH = mapOf(
        0x0200 to "Asuka City", 0x0201 to "Asuka City 2", 0x0202 to "Asuka Bridge",
        0x0203 to "Main Lobby", 0x0204 to "Main Lobby", 0x0206 to "Digimon Lab",
        0x0207 to "Registration Room", 0x0208 to "Arena Front Desk",
        0x0209 to "Digimon Arena", 0x020A to "Asuka Inn 1F", 0x020B to "Underground Path",
        0x020C to "Asuka Inn 2F", 0x020D to "Smith's Shop", 0x020E to "Junk Shop",
        0x020F to "Lamb Chop", 0x0210 to "Cargo Tower", 0x0211 to "Yellow Cruiser",
        0x0212 to "Underwater Tunnel", 0x0213 to "El Dorado", 0x0214 to "Admin Center 1F",
        0x0215 to "Basement Stairs", 0x0216 to "Prison Tower", 0x0217 to "Admin Center 2F",
        0x0218 to "Master Room", 0x0219 to "A.o.A Headquarters", 0x021A to "Admin Center B1F",
        0x021B to "Asuka Sewers", 0x021C to "Control Room", 0x021D to "Central Park",
        0x021E to "Wire Forest Entrance", 0x021F to "Shell Beach", 0x0220 to "Plug Cape",
        0x0221 to "West Wire Forest", 0x0222 to "East Wire Forest",
        0x0223 to "Forest Inn", 0x0224 to "Forest Inn Basement",
        0x0225 to "Protocol Forest", 0x0226 to "Protocol Ruins",
        0x0227 to "Divermon's Lake", 0x0228 to "Duel Island",
        0x0229 to "Wind Prairie", 0x022A to "Kicking Forest", 0x022B to "Tyranno Valley",
        0x022C to "East Station", 0x022D to "Deep Crevice",
        0x022E to "Seiryu City", 0x022F to "Zephyr Tower", 0x0230 to "Seiryu Tower",
        0x0231 to "Gale Tower"
    )
}
