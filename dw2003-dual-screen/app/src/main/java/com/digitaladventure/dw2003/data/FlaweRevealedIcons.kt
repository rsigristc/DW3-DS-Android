package com.digitaladventure.dw2003.data

/**
 * Flawe only draws world-map icons the save already unlocked. Those flags live
 * in the PAL save block before [GameStateReader.STORY_STAGE].
 *
 * Two layouts appear in the same window:
 * - one byte per Asuka MAP_ID at `0x8004B000 + MAP_ID` (Asuka City → `0x8004B200`)
 * - ASKMAP icon-code bits packed from `0x8004B200` (codes 1..46)
 *
 * The first layout that looks like a real atlas (not leftover overlay names)
 * is used. Amaterasu MAP_IDs sit past this window; those hubs stay on prefs.
 */
object FlaweRevealedIcons {
    const val BYTE_MAP_BASE = 0x4B000
    const val ICON_BITS = 0x4B200
    const val ICON_BITS_LENGTH = 8
    const val WINDOW = 0x4B200
    const val WINDOW_LENGTH = 0x170

    fun fromMain(main: ByteArray, storyStage: Int): Set<Int> {
        val candidates = listOf(
            bitsByIconCode(main, ICON_BITS),
            bytesByMapId(main),
            bitsByMapId(main, ICON_BITS)
        )
        return candidates.firstOrNull { raw ->
            val known = raw.filter { it in FastTravelCatalog.askmapIcons }.toSet()
            known.size <= 24 && plausible(sanitize(known, storyStage))
        }?.let { sanitize(it, storyStage) }.orEmpty()
    }

    fun sanitize(ids: Set<Int>, storyStage: Int): Set<Int> {
        var out = ids.filter { it in FastTravelCatalog.askmapIcons }.toSet()
        if (storyStage < 30) out = out - FastTravelCatalog.atlasGhosts
        if (storyStage < 15) out = out.filterNot(::lateAsuka).toSet()
        return out
    }

    fun plausible(ids: Set<Int>): Boolean = ids.isNotEmpty() && ids.size <= 24

    private fun bytesByMapId(main: ByteArray): Set<Int> {
        val origin = BYTE_MAP_BASE - GameStateReader.MAIN_BASE
        return FlaweFastTravelTable.asukaMapIds.filter { mapId ->
            val offset = origin + mapId
            offset in main.indices && main[offset] != 0.toByte()
        }.toSet()
    }

    private fun bitsByIconCode(main: ByteArray, flagBase: Int): Set<Int> {
        val origin = flagBase - GameStateReader.MAIN_BASE
        return FlaweFastTravelTable.asukaIcons.mapNotNull { icon ->
            icon.mapId.takeIf { bitSet(main, origin, icon.iconCode) }
        }.toSet()
    }

    private fun bitsByMapId(main: ByteArray, flagBase: Int): Set<Int> {
        val origin = flagBase - GameStateReader.MAIN_BASE
        return FastTravelCatalog.askmapIcons.filter { mapId ->
            bitSet(main, origin, mapId)
        }.toSet()
    }

    private fun bitSet(main: ByteArray, origin: Int, index: Int): Boolean {
        if (index < 0) return false
        val offset = origin + (index ushr 3)
        if (offset !in main.indices) return false
        return (main[offset].toInt() and (1 shl (index and 7))) != 0
    }

    private fun lateAsuka(mapId: Int): Boolean {
        val sector = MapRegionCatalog.resolve(mapId).sector
        return sector == SectorRegion.SOUTH ||
            sector == SectorRegion.WEST ||
            sector == SectorRegion.NORTH
    }
}
