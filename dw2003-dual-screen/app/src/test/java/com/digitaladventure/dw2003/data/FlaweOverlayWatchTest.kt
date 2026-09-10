package com.digitaladventure.dw2003.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FlaweOverlayWatchTest {
    private val ram = ByteArray(0x200000)

    private fun word(at: Int, value: Long) {
        repeat(4) { ram[at + it] = (value ushr (8 * it)).toByte() }
    }

    @Test
    fun readsMenuWidgetAndHoveredAskmapIcon() {
        word(0xB200, 0x80120000)
        word(0xB204, 0x80150000)
        ram[0x1500C1] = 1
        word(0x150180, 1)
        word(0x150184, 0x1E)
        word(0x9BBE4, 0x8C820180)
        word(0xC000, 0x8E230180)
        word(0x48D68, 0x021D)
        word(0x4B3F8, 0x1000)

        val snapshot = FlaweOverlayWatch.collect { at, size -> ram.copyOfRange(at, at + size) }
        assertEquals(0x150000, snapshot.widgetBase)
        assertEquals(1, FlaweOverlayWatch.menuVisibleByte(snapshot))
        assertEquals(1, FlaweOverlayWatch.cursorOnIcon(snapshot))
        assertEquals(0x1E, FlaweOverlayWatch.hoveredIcon(snapshot))
        assertTrue(FlaweOverlayWatch.v2Loaded(snapshot))
        assertTrue(FlaweOverlayWatch.dispatcherLoaded(snapshot))

        val summary = FlaweOverlayWatch.summarize(snapshot, 0x021D, 0x1000, true, true)
        assertTrue(summary.contains("destIcon=0x1E"))
        assertTrue(summary.contains("start=true"))
        val dump = FlaweOverlayWatch.hexDump(snapshot)
        assertTrue(dump.contains("SCRATCH"))
        assertTrue(dump.contains("REVEALED"))
    }

    @Test
    fun diffsHoveredIconBetweenPolls() {
        word(0xB204, 0x80150000)
        word(0x150184, 0x14)
        val first = FlaweOverlayWatch.collect { at, size -> ram.copyOfRange(at, at + size) }
        word(0x150184, 0x1E)
        val second = FlaweOverlayWatch.collect { at, size -> ram.copyOfRange(at, at + size) }
        val change = FlaweOverlayWatch.wordChanges(first, second)
            .single { it.address == 0x150184 }
        assertEquals(0x14, change.previous)
        assertEquals(0x1E, change.current)
    }
}
