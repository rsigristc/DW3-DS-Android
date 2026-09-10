package com.digitaladventure.dw2003.data

import com.digitaladventure.dw2003.model.RamChange

/**
 * RAM windows used by Flawe's START menu, world map and fast-travel dispatcher.
 * The debug probe diffs these the same way battle setup/arena are watched.
 */
object FlaweOverlayWatch {
    const val SCRATCH = GameStateReader.SCRATCH_BASE
    const val SCRATCH_LENGTH = GameStateReader.SCRATCH_LENGTH
    const val DISPATCHER = 0x0C000
    const val DISPATCHER_LENGTH = 0x50
    const val V2 = 0x9BBE4
    const val V2_LENGTH = 0x3C
    const val MAP_STATE = 0x1B200
    const val MAP_STATE_LENGTH = 0x20
    const val AREA_WINDOW = 0x48D60
    const val AREA_LENGTH = 0x20
    const val MAP_WINDOW = 0x4B3F0
    const val MAP_LENGTH = 0x20
    const val REVEALED = FlaweRevealedIcons.WINDOW
    const val REVEALED_LENGTH = FlaweRevealedIcons.WINDOW_LENGTH
    const val WIDGET_VIS_OFFSET = 0xB8
    const val WIDGET_VIS_LENGTH = 0x20
    const val WIDGET_ICON_OFFSET = 0x170
    const val WIDGET_ICON_LENGTH = 0x40
    const val CURSOR_OFFSET = 0x180
    const val ICON_OFFSET = 0x184

    data class Snapshot(
        val scratch: ByteArray = ByteArray(0),
        val dispatcher: ByteArray = ByteArray(0),
        val v2: ByteArray = ByteArray(0),
        val mapState: ByteArray = ByteArray(0),
        val areaWindow: ByteArray = ByteArray(0),
        val mapWindow: ByteArray = ByteArray(0),
        val revealed: ByteArray = ByteArray(0),
        val widgetVis: ByteArray = ByteArray(0),
        val widgetIcon: ByteArray = ByteArray(0),
        val widgetBase: Int = 0
    ) {
        fun windows(): List<Pair<Int, ByteArray>> = listOf(
            SCRATCH to scratch,
            DISPATCHER to dispatcher,
            V2 to v2,
            MAP_STATE to mapState,
            AREA_WINDOW to areaWindow,
            MAP_WINDOW to mapWindow,
            REVEALED to revealed,
            (widgetBase + WIDGET_VIS_OFFSET) to widgetVis,
            (widgetBase + WIDGET_ICON_OFFSET) to widgetIcon
        )
    }

    fun collect(readMemory: (Int, Int) -> ByteArray): Snapshot {
        val scratch = readMemory(SCRATCH, SCRATCH_LENGTH)
        val widgetBase = widgetBase(scratch)
        return Snapshot(
            scratch = scratch,
            dispatcher = readMemory(DISPATCHER, DISPATCHER_LENGTH),
            v2 = readMemory(V2, V2_LENGTH),
            mapState = readMemory(MAP_STATE, MAP_STATE_LENGTH),
            areaWindow = readMemory(AREA_WINDOW, AREA_LENGTH),
            mapWindow = readMemory(MAP_WINDOW, MAP_LENGTH),
            revealed = readMemory(REVEALED, REVEALED_LENGTH),
            widgetVis = widgetBase?.let { readMemory(it + WIDGET_VIS_OFFSET, WIDGET_VIS_LENGTH) } ?: ByteArray(0),
            widgetIcon = widgetBase?.let { readMemory(it + WIDGET_ICON_OFFSET, WIDGET_ICON_LENGTH) } ?: ByteArray(0),
            widgetBase = widgetBase ?: 0
        )
    }

    fun widgetBase(scratch: ByteArray): Int? {
        if (scratch.size < 8) return null
        return ramOffset(GameStateReader.u32(scratch, 4), WIDGET_ICON_OFFSET + WIDGET_ICON_LENGTH)
    }

    fun cursorOnIcon(snapshot: Snapshot): Int? = wordAt(snapshot.widgetIcon, CURSOR_OFFSET - WIDGET_ICON_OFFSET)

    fun hoveredIcon(snapshot: Snapshot): Int? = wordAt(snapshot.widgetIcon, ICON_OFFSET - WIDGET_ICON_OFFSET)

    fun menuVisibleByte(snapshot: Snapshot): Int? {
        val index = 0xC1 - WIDGET_VIS_OFFSET
        if (index !in snapshot.widgetVis.indices) return null
        return snapshot.widgetVis[index].toInt() and 0xFF
    }

    fun dispatcherLoaded(snapshot: Snapshot): Boolean =
        snapshot.dispatcher.size >= 4 && GameStateReader.u32(snapshot.dispatcher, 0) == 0x8E230180L

    fun v2Loaded(snapshot: Snapshot): Boolean =
        snapshot.v2.size >= 4 && GameStateReader.u32(snapshot.v2, 0) == 0x8C820180L

    fun summarize(
        snapshot: Snapshot,
        areaId: Int,
        mapId: Int,
        fieldMenuVisible: Boolean?,
        flaweMapLoaded: Boolean
    ): String = buildString {
        append("area=0x${areaId.toString(16).uppercase()} map=0x${mapId.toString(16).uppercase()}")
        append(" start=${fieldMenuVisible ?: "?"}")
        append(" mapCode=${if (flaweMapLoaded) 1 else 0}")
        append(" v2=${if (v2Loaded(snapshot)) 1 else 0}")
        append(" c000=${if (dispatcherLoaded(snapshot)) 1 else 0}")
        append(" widget=")
        if (snapshot.widgetBase != 0) {
            append("0x${(snapshot.widgetBase or 0x80000000.toInt()).toUInt().toString(16).uppercase()}")
        } else {
            append("-")
        }
        append(" vis=${menuVisibleByte(snapshot) ?: "-"}")
        append(" cursor=${cursorOnIcon(snapshot) ?: "-"}")
        append(" destIcon=${hoveredIcon(snapshot)?.let { "0x${it.toString(16).uppercase()}" } ?: "-"}")
        val lastMap = if (snapshot.mapWindow.size >= 0x12) {
            GameStateReader.u16(snapshot.mapWindow, 0x10)
        } else {
            0
        }
        append(" last=0x${lastMap.toString(16).uppercase()}")
        if (mapId == OverlaySceneResolver.MENU_OVERLAY && AreaCatalog.isField(areaId)) {
            append(" hover=0x${areaId.toString(16).uppercase()}")
        }
    }

    fun hexDump(snapshot: Snapshot): String = buildString {
        fun block(title: String, bytes: ByteArray, base: Int) {
            if (bytes.isEmpty()) return
            appendLine(title)
            appendLine(RamWatch.hexDump(bytes, base))
        }
        block("SCRATCH 0x8000B200", snapshot.scratch, SCRATCH)
        block("AREA 0x80048D60", snapshot.areaWindow, AREA_WINDOW)
        block("MAP_ID 0x8004B3F0", snapshot.mapWindow, MAP_WINDOW)
        block("REVEALED 0x8004B200", snapshot.revealed, REVEALED)
        block("MAP_STATE 0x8001B200", snapshot.mapState, MAP_STATE)
        block("DISPATCHER 0x8000C000", snapshot.dispatcher, DISPATCHER)
        block("V2 0x8009BBE4", snapshot.v2, V2)
        if (snapshot.widgetBase != 0) {
            block(
                "WIDGET+0xB8 0x${(snapshot.widgetBase + WIDGET_VIS_OFFSET).toString(16).uppercase()}",
                snapshot.widgetVis,
                snapshot.widgetBase + WIDGET_VIS_OFFSET
            )
            block(
                "WIDGET+0x170 0x${(snapshot.widgetBase + WIDGET_ICON_OFFSET).toString(16).uppercase()}",
                snapshot.widgetIcon,
                snapshot.widgetBase + WIDGET_ICON_OFFSET
            )
        }
    }.trimEnd()

    fun wordChanges(previous: Snapshot?, current: Snapshot): List<RamChange> {
        if (previous == null) return emptyList()
        return current.windows().zip(previous.windows()).flatMap { (now, before) ->
            if (now.first != before.first || now.second.size != before.second.size) emptyList()
            else RamWatch.wordChanges(before.second, now.second, now.first)
        }
    }

    private fun wordAt(bytes: ByteArray, offset: Int): Int? {
        if (offset < 0 || offset + 3 >= bytes.size) return null
        return GameStateReader.u32(bytes, offset).toInt()
    }

    private fun ramOffset(pointer: Long, length: Int): Int? =
        (pointer - 0x80000000L).takeIf { it in 0L..(0x200000 - length).toLong() }?.toInt()
}
