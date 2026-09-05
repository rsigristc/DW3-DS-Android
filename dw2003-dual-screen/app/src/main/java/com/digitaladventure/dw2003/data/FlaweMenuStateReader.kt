package com.digitaladventure.dw2003.data

/** The field START menu does not load STSTATUS. Flawe tests this widget's
 * visibility byte before drawing the walkthrough (module +0x27A4..+0x27BC). */
object FlaweMenuStateReader {
    fun isFieldMenuVisible(readMemory: (Int, Int) -> ByteArray): Boolean? {
        val scratch = readMemory(0xB200, 8)
        if (scratch.size != 8) return null
        val module = ramOffset(GameStateReader.u32(scratch, 0), 0xD58) ?: return null
        val widget = ramOffset(GameStateReader.u32(scratch, 4), 0xC2) ?: return null
        val selector = readMemory(module + 0xD4C, 12)
        if (selector.size != 12 ||
            GameStateReader.u32(selector, 0) != 0xACC30008L ||
            GameStateReader.u32(selector, 4) != 0xACC4000CL ||
            GameStateReader.u32(selector, 8) != 0xACC50010L
        ) return null
        val visible = readMemory(widget + 0xC1, 1)
        if (visible.size != 1 || !scratch.contentEquals(readMemory(0xB200, 8))) return null
        return visible[0] != 0.toByte()
    }

    private fun ramOffset(pointer: Long, length: Int): Int? =
        (pointer - 0x80000000L).takeIf { it in 0L..(0x200000 - length).toLong() }?.toInt()
}
