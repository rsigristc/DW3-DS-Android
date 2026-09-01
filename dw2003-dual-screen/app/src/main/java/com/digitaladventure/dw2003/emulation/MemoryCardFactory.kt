package com.digitaladventure.dw2003.emulation

/** Produces the same formatted 128 KiB card layout initialized by PCSX-ReARMed. */
object MemoryCardFactory {
    fun createFormatted(): ByteArray {
        val card = ByteArray(AppFileRules.PLAYSTATION_MEMORY_CARD_SIZE.toInt())
        var offset = 0
        card[offset++] = 'M'.code.toByte()
        card[offset++] = 'C'.code.toByte()
        offset += 0x7D
        card[offset++] = 0x0E

        repeat(15) {
            card[offset++] = 0xA0.toByte()
            offset += 0x07
            card[offset++] = 0xFF.toByte()
            card[offset++] = 0xFF.toByte()
            offset += 0x75
            card[offset++] = 0xA0.toByte()
        }

        repeat(20) {
            repeat(4) { card[offset++] = 0xFF.toByte() }
            offset += 0x04
            card[offset++] = 0xFF.toByte()
            card[offset++] = 0xFF.toByte()
            offset += 0x76
        }
        return card
    }
}
