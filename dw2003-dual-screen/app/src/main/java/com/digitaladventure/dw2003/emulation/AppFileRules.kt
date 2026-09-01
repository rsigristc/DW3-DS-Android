package com.digitaladventure.dw2003.emulation

object AppFileRules {
    const val PLAYSTATION_BIOS_SIZE = 512L * 1024L
    const val PLAYSTATION_MEMORY_CARD_SIZE = 128L * 1024L
    private const val VGS_HEADER_SIZE = 64L
    private const val GME_HEADER_SIZE = 3904L

    fun isValidBiosSize(sizeBytes: Long): Boolean = sizeBytes == PLAYSTATION_BIOS_SIZE

    fun isValidMemoryCardSize(sizeBytes: Long): Boolean = sizeBytes == PLAYSTATION_MEMORY_CARD_SIZE

    fun hasMemoryCardSignature(data: ByteArray): Boolean =
        data.size.toLong() == PLAYSTATION_MEMORY_CARD_SIZE &&
            data[0] == 'M'.code.toByte() && data[1] == 'C'.code.toByte()

    fun memoryCardPayloadOffset(sizeBytes: Long): Int? = when (sizeBytes) {
        PLAYSTATION_MEMORY_CARD_SIZE -> 0
        PLAYSTATION_MEMORY_CARD_SIZE + VGS_HEADER_SIZE -> VGS_HEADER_SIZE.toInt()
        PLAYSTATION_MEMORY_CARD_SIZE + GME_HEADER_SIZE -> GME_HEADER_SIZE.toInt()
        else -> null
    }

    fun isEuropeanPlayStationBios(data: ByteArray): Boolean {
        if (data.size.toLong() != PLAYSTATION_BIOS_SIZE) return false
        val executableSignature = data[1] == 0x00.toByte() &&
            data[2] == 0x08.toByte() &&
            data[3] == 0x3C.toByte() &&
            data[4] == 0x3F.toByte()
        val signature = "PS compatible".toByteArray()
        val compatibleSignature = signature.indices.all { index -> data[0x12C + index] == signature[index] }
        val europeanMarker = data[0x7FF51] == ' '.code.toByte() && data[0x7FF52] == 'E'.code.toByte()
        return (executableSignature || compatibleSignature) && europeanMarker
    }
}
