package com.digitaladventure.dw2003.data

/** Reads the selector used by Flawe's standalone and combined 2.0 PAL patches. */
object FlaweWalkthroughReader {
    const val SCRATCH_BASE = 0xB200
    private const val RAM_SIZE = 0x200000
    private const val WINDOW = 256

    fun read(readMemory: (Int, Int) -> ByteArray): String? {
        val scratch = readMemory(SCRATCH_BASE, 20)
        if (scratch.size != 20) return null
        val pointer = GameStateReader.u32(scratch, 0)
        if (pointer !in 0x80000000L..0x801FFFFFL) return null
        val base = (pointer - 0x80000000L).toInt()
        // The renderer stores title / first line / second line as relative offsets.
        // Verify its selector code before trusting scratch RAM left by another overlay.
        if (base > RAM_SIZE - 0xD58) return null
        val selector = readMemory(base + 0xD4C, 12)
        if (selector.size != 12 ||
            GameStateReader.u32(selector, 0) != 0xACC30008L ||
            GameStateReader.u32(selector, 4) != 0xACC4000CL ||
            GameStateReader.u32(selector, 8) != 0xACC50010L
        ) return null

        fun line(slot: Int): String? {
            val relative = GameStateReader.u32(scratch, slot)
            if (relative == 0L) return ""
            val address = base.toLong() + relative
            if (address !in 0L until RAM_SIZE.toLong()) return null
            val length = minOf(WINDOW, RAM_SIZE - address.toInt())
            val bytes = readMemory(address.toInt(), length)
            if (bytes.size != length || bytes.none { it == 0.toByte() }) return null
            return DwTextDecoder.decode(bytes).replace(Regex("\\s+"), " ").trim()
        }

        val title = line(8) ?: return null
        val first = line(12) ?: return null
        val second = line(16) ?: return null
        if (title.none(Char::isLetter) || first.none(Char::isLetter)) return null
        // Reads are queued on the emulation thread separately. Retry next poll if
        // the game changed the selection while we were fetching its strings.
        if (!scratch.contentEquals(readMemory(SCRATCH_BASE, 20))) return null
        // Do not filter by verbs or place names: both lines are selected by the mod,
        // including objectives and continuations absent from our old phrase catalog.
        return listOf(first, second).filter(String::isNotEmpty).joinToString(" ")
    }
}
