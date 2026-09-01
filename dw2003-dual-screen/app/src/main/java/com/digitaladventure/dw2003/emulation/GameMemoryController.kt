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

    fun applyCheats(enabled: List<CheatSpec>) {
        view.resetCheat()
        enabled.forEachIndexed { index, cheat ->
            view.setCheat(index, true, cheat.code)
        }
    }

    companion object {
        private const val RAM_MASK = 0x1FFFFF

        fun writeU32(target: ByteArray, offset: Int, value: Long) {
            repeat(4) { index ->
                target[offset + index] = (value ushr (index * 8)).toByte()
            }
        }
    }
}
