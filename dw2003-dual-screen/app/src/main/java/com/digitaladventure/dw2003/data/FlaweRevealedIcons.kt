package com.digitaladventure.dw2003.data

/** PAL FLAGS_20: audited in Harmony, not inferred from atlas-looking bytes.
 * STSTATUS 0x8009A6B8 checks 0x2000 | (fieldMode & 0xFF), value 1.
 * EXE 0x800163B0 selects 0x8004B3C0; 0x80015590 tests that packed bit.
 */
object FlaweRevealedIcons {
    const val FLAGS_20 = 0x4B3C0
    const val FLAGS_LENGTH = 0x1E
    const val WINDOW = FLAGS_20
    const val WINDOW_LENGTH = FLAGS_LENGTH

    @Suppress("UNUSED_PARAMETER")
    fun fromMain(main: ByteArray, storyStage: Int): Set<Int> {
        val origin = FLAGS_20 - GameStateReader.MAIN_BASE
        if (origin < 0 || origin + FLAGS_LENGTH > main.size) return emptySet()
        return (0x0200..0x02D6).mapNotNull { mapId ->
            val index = mapId and 0xFF
            val visited = main[origin + (index ushr 3)].toInt() and (1 shl (index and 7)) != 0
            if (visited) FlaweFastTravelTable.fieldIcon(mapId) else null
        }.toSet()
    }
}
