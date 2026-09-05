package com.digitaladventure.dw2003.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FlaweWalkthroughReaderTest {
    private val ram = ByteArray(0x200000)
    private val base = 0x120000

    private fun put(offset: Int, hex: String) {
        hex.chunked(2).map { it.toInt(16).toByte() }.toByteArray().copyInto(ram, offset)
    }

    private fun word(offset: Int, value: Long) {
        repeat(4) { ram[offset + it] = (value ushr (8 * it)).toByte() }
    }

    private fun select(title: Long, first: Long, second: Long) {
        word(0xB200, 0x80000000L + base)
        word(0xB208, title)
        word(0xB20C, first)
        word(0xB210, second)
    }

    private fun read(): String? = FlaweWalkthroughReader.read { offset, length ->
        ram.copyOfRange(offset, offset + length)
    }

    private fun fixture() {
        // Bytes from Flawe's 2.0 standalone PPF, also present in Combined 2.0.
        put(base + 0xD4C, "0800c3ac0c00c4ac1000c5ac")
        put(base + 0x7FEC, "3b360101392c282a2f01011d39363b362a363301011f3c30353a00")
        put(base + 0x801C, "152c282b01011b36393b2f0101362d0101011312283a3b01012430392c01011336392c3a3b011300")
        put(base + 0x8060, "1d39363b362a363301011f3c30353a00")
        put(base + 0x8084, "2128333201013b3601011f2c372c283b30352e0101213634010130350101202c3039403c010121363e2c3900")
        put(base + 0x80D0, "1f2c372c283b30352e010121363400")
    }

    @Test fun followsSelectedRelativeOffsetsWithBothMissionsResident() {
        fixture()
        select(0x80D0, 0x8084, 0x8080)
        assertEquals("Talk to Repeating Tom in Seiryu Tower", read())
        select(0x8060, 0x801C, 0x7FEC)
        assertEquals("Head North of 'East Wire Forest' to reach Protocol Ruins", read())
        select(0x80D0, 0x8084, 0x8080)
        assertEquals("Talk to Repeating Tom in Seiryu Tower", read())
    }

    @Test fun rejectsUnloadedOrOverwrittenModule() {
        fixture()
        select(0x8060, 0x801C, 0x7FEC)
        ram[base + 0xD4C] = 0
        assertNull(read())
    }

    @Test fun rejectsAbsoluteOffsetsAndOutOfRamBasesWithoutReadingOutsideRam() {
        fixture()
        select(0x8060, 0x801C, 0xFFFFFFFFL)
        assertNull(read())
        select(0x8060, 0x801C, 0x7FEC)
        word(0xB200, 0x801FFFF0L)
        assertNull(read())
    }

    @Test fun readsObjectivesOutsideOldVerbAndLocationCatalog() {
        fixture()
        DwTextDecoder.encode("Equip your partners before continuing").copyInto(ram, base + 0x9000)
        select(0x8060, 0x9000, 0)
        assertEquals("Equip your partners before continuing", read())
    }

    @Test fun rejectsTruncatedMemoryRead() {
        assertNull(FlaweWalkthroughReader.read { _, _ -> ByteArray(0) })
    }

    @Test fun rejectsSelectionChangedDuringRead() {
        fixture()
        select(0x8060, 0x801C, 0x7FEC)
        var reads = 0
        assertNull(FlaweWalkthroughReader.read { offset, length ->
            if (offset == 0xB200 && ++reads == 2) select(0x80D0, 0x8084, 0x8080)
            ram.copyOfRange(offset, offset + length)
        })
    }
}
