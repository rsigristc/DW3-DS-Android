package com.digitaladventure.dw2003.data

/** Decoder for the compact code-point table used by the European DW2003 files. */
object DwTextDecoder {
    private val escapes = mapOf(
        0x01 to " ", 0x02 to ",", 0x03 to ".", 0x04 to ",", 0x05 to ".",
        0x07 to ":", 0x08 to ";", 0x0F to "/", 0x12 to "…", 0x13 to "'",
        0x14 to "\"", 0x15 to "(", 0x16 to ")", 0x19 to "+", 0x1A to "-",
        0x1B to "✕", 0x1C to "=", 0x1D to "<", 0x1E to ">", 0x2A to "%",
        0x2B to "&", 0x2F to "○", 0x32 to "□", 0x34 to "△", 0x37 to "→",
        0x38 to "←", 0x39 to "↑", 0x3A to "↓", 0x3B to "¡", 0x3F to "¿",
        0x45 to "Ñ", 0x49 to "Ü", 0x4B to "à", 0x4F to "ç", 0x50 to "è",
        0x51 to "é", 0x54 to "ì", 0x57 to "ñ", 0x58 to "ò", 0x5C to "ù",
        0x5E to "ü", 0x5F to "á", 0x60 to "í", 0x61 to "ó", 0x62 to "ú",
        0x68 to "Á", 0x69 to "É", 0x6A to "Í", 0x6B to "Ó", 0x6C to "Ú"
    )

    fun decode(bytes: ByteArray): String {
        val out = StringBuilder()
        var index = 0
        while (index < bytes.size && out.length < 240) {
            val value = bytes[index].toInt() and 0xFF
            when {
                value == 0x00 -> break
                value in 0x04..0x0D -> out.append(('0'.code + value - 0x04).toChar())
                value in 0x0E..0x27 -> out.append(('A'.code + value - 0x0E).toChar())
                value in 0x28..0x41 -> out.append(('a'.code + value - 0x28).toChar())
                value == 0xE6 -> out.append('?')
                value == 0xE7 -> out.append('!')
                value == 0xE9 -> out.append('~')
                value == 0x01 && index + 1 < bytes.size -> {
                    out.append(escapes[bytes[++index].toInt() and 0xFF] ?: "")
                }
                value == 0x02 && index + 1 < bytes.size -> {
                    when (bytes[++index].toInt() and 0xFF) {
                        0x01 -> out.append('\n')
                        0x03 -> out.append(" · ")
                        0x09 -> out.append("Tamer")
                        0x05 -> if (index + 1 < bytes.size) index++
                    }
                }
            }
            index++
        }
        return out.toString().replace(Regex("[ \\t]+"), " ").trim()
    }
}
