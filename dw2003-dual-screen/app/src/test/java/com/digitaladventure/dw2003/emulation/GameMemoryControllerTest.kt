package com.digitaladventure.dw2003.emulation

import org.junit.Assert.assertEquals
import org.junit.Test

class GameMemoryControllerTest {
    @Test
    fun writesLittleEndianProfileIds() {
        val payload = ByteArray(12)
        GameMemoryController.writeU32(payload, 0, 5)
        GameMemoryController.writeU32(payload, 4, 6)
        GameMemoryController.writeU32(payload, 8, 3)
        assertEquals(5, payload[0].toInt())
        assertEquals(6, payload[4].toInt())
        assertEquals(3, payload[8].toInt())
    }
}
