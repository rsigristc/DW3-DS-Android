package com.digitaladventure.dw2003.data

import org.junit.Assert.assertEquals
import org.junit.Test

class DwTextDecoderTest {
    @Test
    fun decodesLatinCharactersAndControls() {
        val encoded = byteArrayOf(
            0x11, 0x30, 0x2e, 0x30, 0x34, 0x36, 0x35, // Digimon
            0x01, 0x01, // space
            0x24, 0x36, 0x39, 0x33, 0x2b, // World
            0x02, 0x01, // newline
            0x06, 0x04, 0x04, 0x07, // 2003
            0x00
        )
        assertEquals("Digimon World\n2003", DwTextDecoder.decode(encoded))
    }

    @Test
    fun encodeRoundTripsWalkthroughStem() {
        val text = "Talk to Repeating Tom in Seiryu Tower."
        assertEquals(text, DwTextDecoder.decode(DwTextDecoder.encode(text)))
    }
}
