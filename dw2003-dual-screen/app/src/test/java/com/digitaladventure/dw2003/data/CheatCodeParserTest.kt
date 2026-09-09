package com.digitaladventure.dw2003.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CheatCodeParserTest {
    @Test
    fun normalizesPalPairsAndJoinsThem() {
        assertEquals(
            "80048DA0 E0FF+80048DA2 05F5",
            CheatCodeParser.normalize("80048da0 e0ff\n80048DA2 05f5")
        )
    }

    @Test
    fun rejectsTextWithoutPalPairs() {
        assertNull(CheatCodeParser.normalize("infinite bits"))
        assertNull(CheatCodeParser.normalize(""))
    }

    @Test
    fun acceptsConditionalAndByteCodes() {
        assertEquals(
            "D0084AB4 1023+80084AB4 1040+30048EF1 0003",
            CheatCodeParser.normalize(
                "D0084AB4 1023\n80084AB4 1040\n30048EF1 0003"
            )
        )
    }
}
