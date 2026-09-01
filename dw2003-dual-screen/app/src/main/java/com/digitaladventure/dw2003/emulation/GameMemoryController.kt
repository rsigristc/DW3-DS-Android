package com.digitaladventure.dw2003.emulation

import com.digitaladventure.dw2003.data.CheatSpec
import com.digitaladventure.dw2003.data.GameStateReader
import com.swordfish.libretrodroid.GLRetroView
import com.swordfish.libretrodroid.LibretroDroid

class GameMemoryController(private val view: GLRetroView) {
    fun reorderParty(profileIds: List<Int>) {
        require(profileIds.size in 1..3) { "La formación activa solo tiene tres ranuras" }
        require(profileIds.all { it in 0..7 }) { "Perfil de compañero fuera de rango" }
        val payload = ByteArray(GameStateReader.ACTIVE_PARTY.size * 4)
        GameStateReader.ACTIVE_PARTY.indices.forEach { index ->
            writeU32(payload, index * 4, (profileIds.getOrNull(index) ?: 0).toLong())
        }
        view.writeMemory(LibretroDroid.MEMORY_SYSTEM_RAM, GameStateReader.ACTIVE_PARTY.first() and RAM_MASK, payload)
    }

    fun requestFastTravel(areaId: Int) {
        require(areaId in 1..0xFFFF) { "Destino de viaje inválido" }
        // These IDs are the live area/map words the companion already reads. Flawe
        // still needs the Map tab open and the status menu closed to start a load.
        writeU16ToRam(GameStateReader.AREA, areaId)
        writeU16ToRam(GameStateReader.MAP_ID, areaId)
    }

    fun readAreaMap(): Pair<Int, Int> {
        val area = view.readMemory(LibretroDroid.MEMORY_SYSTEM_RAM, GameStateReader.AREA and RAM_MASK, 2)
        val map = view.readMemory(LibretroDroid.MEMORY_SYSTEM_RAM, GameStateReader.MAP_ID and RAM_MASK, 2)
        return GameStateReader.u16(area, 0) to GameStateReader.u16(map, 0)
    }

    fun readOverlaySignature(): Long {
        val bytes = view.readMemory(
            LibretroDroid.MEMORY_SYSTEM_RAM,
            GameStateReader.OVERLAY_BASE and RAM_MASK,
            4
        )
        return GameStateReader.u32(bytes, 0)
    }

    fun readTabScan(): ByteArray {
        val overlay = view.readMemory(
            LibretroDroid.MEMORY_SYSTEM_RAM,
            GameStateReader.OVERLAY_BASE and RAM_MASK,
            TAB_SCAN_OVERLAY
        )
        val nearMap = view.readMemory(
            LibretroDroid.MEMORY_SYSTEM_RAM,
            (GameStateReader.MAP_ID - TAB_SCAN_BEFORE) and RAM_MASK,
            TAB_SCAN_WINDOW
        )
        return overlay + nearMap
    }

    fun applyCheats(enabled: List<CheatSpec>) {
        view.resetCheat()
        enabled.forEachIndexed { index, cheat ->
            view.setCheat(index, true, cheat.code)
        }
    }

    private fun writeU16ToRam(address: Int, value: Int) {
        val payload = byteArrayOf(value.toByte(), (value ushr 8).toByte())
        view.writeMemory(LibretroDroid.MEMORY_SYSTEM_RAM, address and RAM_MASK, payload)
    }

    companion object {
        private const val RAM_MASK = 0x1FFFFF
        private const val TAB_SCAN_OVERLAY = 0x400
        private const val TAB_SCAN_BEFORE = 0x40
        private const val TAB_SCAN_WINDOW = 0x80

        fun writeU32(target: ByteArray, offset: Int, value: Long) {
            repeat(4) { index ->
                target[offset + index] = (value ushr (index * 8)).toByte()
            }
        }
    }
}
