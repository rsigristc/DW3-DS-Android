package com.digitaladventure.dw2003.data

import org.junit.Assert.*
import org.junit.Test

class FlaweMenuStateReaderTest {
    private val ram = ByteArray(0x200000)
    private fun word(at: Int, value: Long) {
        repeat(4) { ram[at + it] = (value ushr (8 * it)).toByte() }
    }
    private fun read() = FlaweMenuStateReader.isFieldMenuVisible { at, size ->
        ram.copyOfRange(at, at + size)
    }
    private fun fixture() {
        word(0xB200, 0x80120000)
        word(0xB204, 0x80150000)
        word(0x120D4C, 0xACC30008)
        word(0x120D50, 0xACC4000C)
        word(0x120D54, 0xACC50010)
    }

    @Test fun fieldMenuVisibilityDoesNotRequireStatusOverlay() {
        fixture()
        assertEquals(false, read())
        ram[0x1500C1] = 1
        assertEquals(true, read())
        ram[0x1500C1] = 0
        assertEquals(false, read())
    }

    @Test fun rejectsStaleCodeAndInvalidPointers() {
        assertNull(read())
        fixture()
        word(0xB204, 0x801FFFFF)
        assertNull(read())
        fixture()
        word(0x120D4C, 0)
        assertNull(read())
    }
}
