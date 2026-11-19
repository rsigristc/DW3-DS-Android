package com.digitaladventure.dw2003.data

import org.junit.Assert.*
import org.junit.Test

class FlaweRevealedIconsTest {
    private fun main() = ByteArray(GameStateReader.MAIN_LENGTH)
    private fun visit(main: ByteArray, mapId: Int) {
        val bit = mapId and 0xFF
        val offset = FlaweRevealedIcons.FLAGS_20 - GameStateReader.MAIN_BASE + (bit ushr 3)
        main[offset] = (main[offset].toInt() or (1 shl (bit and 7))).toByte()
    }
    @Test fun emptySaveAndTruncatedReadsUnlockNothing() {
        assertTrue(FlaweRevealedIcons.fromMain(main(), 4).isEmpty())
        assertTrue(FlaweRevealedIcons.fromMain(ByteArray(5), 70).isEmpty())
    }
    @Test fun nativeVisitsSeparateTheTwoServers() {
        val main = main()
        listOf(0x0203, 0x021D, 0x0270, 0x029C).forEach { visit(main, it) }
        assertEquals(setOf(0x0200, 0x021D, 0x0270, 0x029C), FlaweRevealedIcons.fromMain(main, 40))
    }
    @Test fun storyDoesNotHideRealVisitsAndThereIsNoTwentyFourIconLimit() {
        val main = main()
        FlaweFastTravelTable.allIcons.forEach { visit(main, it.mapId) }
        val icons = FlaweRevealedIcons.fromMain(main, 4)
        assertTrue(icons.size > 24)
        assertTrue(0x0264 in icons)
        assertTrue(0x02D6 in icons)
    }
    @Test fun oldHeuristicOffsetsCannotUnlockGhostIcons() {
        val main = main()
        for (offset in 0x4B200..0x4B36F) main[offset - GameStateReader.MAIN_BASE] = 1
        assertTrue(FlaweRevealedIcons.fromMain(main, 70).isEmpty())
    }
}
