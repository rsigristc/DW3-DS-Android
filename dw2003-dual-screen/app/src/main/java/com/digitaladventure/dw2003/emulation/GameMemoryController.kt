package com.digitaladventure.dw2003.emulation

import com.digitaladventure.dw2003.data.CheatSpec
import com.digitaladventure.dw2003.data.GameStateReader
import com.digitaladventure.dw2003.data.FlaweMenuStateReader
import com.swordfish.libretrodroid.GLRetroView
import com.swordfish.libretrodroid.LibretroDroid

data class FlaweDirectWarpToken(
    val dispatcherRamOffset: Int,
    val originalDispatcher: ByteArray
)

class GameMemoryController(private val view: GLRetroView) {
    fun isFieldMenuVisible(): Boolean? = FlaweMenuStateReader.isFieldMenuVisible { offset, length ->
        view.readMemory(LibretroDroid.MEMORY_SYSTEM_RAM, offset, length)
    }

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

    fun hasPreferredFlaweDispatcher(): Boolean {
        val preferred = view.readMemory(
            LibretroDroid.MEMORY_SYSTEM_RAM,
            FlaweDirectWarpPatch.DISPATCHER_RAM_OFFSET,
            FlaweDirectWarpPatch.WINDOW_SIZE
        )
        return FlaweDirectWarpPatch.matchesPreferred(preferred)
    }

    fun hasFlaweDispatcher(): Boolean {
        if (FlaweDirectWarpPatch.matchesV2(view.readMemory(
                LibretroDroid.MEMORY_SYSTEM_RAM, FlaweDirectWarpPatch.V2_RAM_OFFSET,
                FlaweDirectWarpPatch.V2_WINDOW_SIZE
            ))) return true
        if (hasPreferredFlaweDispatcher()) return true
        val nearby = view.readMemory(
            LibretroDroid.MEMORY_SYSTEM_RAM,
            FlaweDirectWarpPatch.DISPATCHER_RAM_OFFSET,
            0x100
        )
        return FlaweDirectWarpPatch.matchesNearby(nearby)
    }

    fun beginDirectFlaweWarp(areaId: Int): FlaweDirectWarpToken? {
        val v2 = view.readMemory(LibretroDroid.MEMORY_SYSTEM_RAM,
            FlaweDirectWarpPatch.V2_RAM_OFFSET, FlaweDirectWarpPatch.V2_WINDOW_SIZE)
        if (FlaweDirectWarpPatch.matchesV2(v2)) {
            val patched = FlaweDirectWarpPatch.prepare(v2, areaId, FlaweDirectWarpPatch.v2Site(0)) ?: return null
            view.writeMemory(LibretroDroid.MEMORY_SYSTEM_RAM, FlaweDirectWarpPatch.V2_RAM_OFFSET, patched)
            return FlaweDirectWarpToken(FlaweDirectWarpPatch.V2_RAM_OFFSET, v2)
        }
        val preferred = view.readMemory(
            LibretroDroid.MEMORY_SYSTEM_RAM,
            FlaweDirectWarpPatch.DISPATCHER_RAM_OFFSET,
            FlaweDirectWarpPatch.WINDOW_SIZE
        )
        FlaweDirectWarpPatch.prepare(preferred, areaId)?.let { patched ->
            view.writeMemory(
                LibretroDroid.MEMORY_SYSTEM_RAM,
                FlaweDirectWarpPatch.DISPATCHER_RAM_OFFSET,
                patched
            )
            return FlaweDirectWarpToken(
                FlaweDirectWarpPatch.DISPATCHER_RAM_OFFSET,
                preferred
            )
        }

        val ram = view.readMemory(
            LibretroDroid.MEMORY_SYSTEM_RAM,
            0,
            FlaweDirectWarpPatch.SYSTEM_RAM_SIZE
        )
        val site = FlaweDirectWarpPatch.selectActiveSite(ram) ?: return null
        val original = ram.copyOfRange(site.ramOffset, site.ramOffset + site.windowSize)
        val patched = FlaweDirectWarpPatch.prepare(
            original,
            areaId,
            site.copy(ramOffset = 0)
        ) ?: return null
        view.writeMemory(LibretroDroid.MEMORY_SYSTEM_RAM, site.ramOffset, patched)
        return FlaweDirectWarpToken(site.ramOffset, original)
    }

    fun restoreDirectFlaweWarp(token: FlaweDirectWarpToken) {
        view.writeMemory(
            LibretroDroid.MEMORY_SYSTEM_RAM,
            token.dispatcherRamOffset,
            token.originalDispatcher
        )
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
