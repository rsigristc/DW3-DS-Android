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
        writeU16ToRam(GameStateReader.AREA, areaId)
        writeU16ToRam(GameStateReader.MAP_ID, areaId)
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

        fun writeU32(target: ByteArray, offset: Int, value: Long) {
            repeat(4) { index ->
                target[offset + index] = (value ushr (index * 8)).toByte()
            }
        }
    }
}
