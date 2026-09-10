package com.digitaladventure.dw2003.data

import com.digitaladventure.dw2003.model.RamChange

object RamWatch {
    fun hexDump(bytes: ByteArray, base: Int, bytesPerLine: Int = 16): String {
        if (bytes.isEmpty()) return "(vacío)"
        val lines = ArrayList<String>((bytes.size + bytesPerLine - 1) / bytesPerLine)
        var offset = 0
        while (offset < bytes.size) {
            val end = minOf(offset + bytesPerLine, bytes.size)
            val hex = (offset until end).joinToString(" ") { index ->
                "%02X".format(bytes[index].toInt() and 0xFF)
            }
            lines += "%06X  %s".format(base + offset, hex)
            offset = end
        }
        return lines.joinToString("\n")
    }

    fun wordChanges(previous: ByteArray?, current: ByteArray, base: Int): List<RamChange> {
        if (previous == null || previous.size != current.size) return emptyList()
        val changes = ArrayList<RamChange>()
        var offset = 0
        while (offset + 1 < current.size) {
            val before = GameStateReader.u16(previous, offset)
            val after = GameStateReader.u16(current, offset)
            if (before != after) {
                changes += RamChange(base + offset, before, after)
            }
            offset += 2
        }
        return changes
    }
}
