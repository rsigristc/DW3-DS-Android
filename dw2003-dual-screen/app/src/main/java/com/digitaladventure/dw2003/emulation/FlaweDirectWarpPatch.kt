package com.digitaladventure.dw2003.emulation

import com.digitaladventure.dw2003.data.FlaweFastTravelTable
import com.digitaladventure.dw2003.data.GameStateReader

/**
 * Flawe copies its fast-travel dispatcher to a scratch-RAM window.
 *
 * The unmodified dispatcher validates the map cursor and reads the hovered
 * ASKMAP icon. Replacing those two instructions only while Cross is pressed
 * lets Flawe's own table write the destination and exact spawn.
 * Icon codes come from [FlaweFastTravelTable] (patcher IPS + ddw3 ASKMAP).
 *
 * Combined Flawe 2.0 packs do not always keep the public 80-byte window at
 * `0x8000C000` or a `j/jal` thunk. A unique matching prologue, or a unique
 * `lw *, 0x184(*)` plus a nearby `bne` of that register, is enough.
 */
object FlaweDirectWarpPatch {
    const val DISPATCHER_RAM_OFFSET = 0x0C000
    const val WINDOW_SIZE = 0x50
    const val SYSTEM_RAM_SIZE = 0x200000

    private const val VALIDATION_BRANCH_OFFSET = 0x0C
    private const val ICON_LOAD_OFFSET = 0x4C
    private const val EXPECTED_FIRST_INSTRUCTION = 0x8E230180L // lw v1, 0x180(s1)
    private const val EXPECTED_VALIDATION_BRANCH_PREFIX = 0x14680000L // bne v1, t0, *
    private const val BRANCH_REGISTER_MASK = 0xFFFF0000L
    private const val EXPECTED_ICON_LOAD = 0x8E230184L // lw v1, 0x184(s1)
    private const val LW_OPCODE = 0x23
    private const val BNE_OPCODE = 5
    private const val ICON_IMMEDIATE = 0x184
    private const val LOOKBACK = 0x70
    private const val NOP = 0L
    private const val ORI_OPCODE_PREFIX = 0x34000000L

    data class DispatcherSite(
        val ramOffset: Int,
        val branchOffset: Int,
        val iconLoadOffset: Int,
        val windowSize: Int = WINDOW_SIZE
    )

    fun iconCode(areaId: Int): Int? = FlaweFastTravelTable.iconCode(areaId)

    fun findActiveDispatcherOffsets(ram: ByteArray): List<Int> =
        selectActiveSites(ram).map { it.ramOffset }

    fun selectActiveSite(ram: ByteArray): DispatcherSite? =
        selectActiveSites(ram).singleOrNull()

    fun matchesPreferred(bytes: ByteArray): Boolean =
        bytes.size == WINDOW_SIZE && matchesDispatcher(bytes, 0)

    fun matchesNearby(bytes: ByteArray): Boolean =
        findLooseSites(bytes, 0).isNotEmpty() ||
            (0..bytes.size - WINDOW_SIZE step 4).any { matchesDispatcher(bytes, it) }

    fun prepare(original: ByteArray, areaId: Int): ByteArray? {
        val iconCode = iconCode(areaId) ?: return null
        if (!matchesPreferred(original)) return null

        return original.copyOf().also { patched ->
            GameMemoryController.writeU32(patched, VALIDATION_BRANCH_OFFSET, NOP)
            GameMemoryController.writeU32(
                patched,
                ICON_LOAD_OFFSET,
                oriZero(rt(EXPECTED_ICON_LOAD), iconCode)
            )
        }
    }

    fun prepare(window: ByteArray, areaId: Int, site: DispatcherSite): ByteArray? {
        val iconCode = iconCode(areaId) ?: return null
        if (site.branchOffset < 0 || site.iconLoadOffset < 0) return null
        if (site.branchOffset + 3 >= window.size || site.iconLoadOffset + 3 >= window.size) return null
        val load = GameStateReader.u32(window, site.iconLoadOffset)
        if (!isLwImmediate(load, ICON_IMMEDIATE)) return null
        val destReg = rt(load)
        val branch = GameStateReader.u32(window, site.branchOffset)
        if (!isBneInvolving(branch, destReg)) return null

        return window.copyOf().also { patched ->
            GameMemoryController.writeU32(patched, site.branchOffset, NOP)
            GameMemoryController.writeU32(patched, site.iconLoadOffset, oriZero(destReg, iconCode))
        }
    }

    private fun selectActiveSites(ram: ByteArray): List<DispatcherSite> {
        val jumpTargets = jumpTargets(ram)
        val exact = findExactSites(ram)
        val referencedExact = exact.filter { isJumpReferenced(it.ramOffset, jumpTargets) }
        if (referencedExact.isNotEmpty()) return referencedExact
        if (exact.size == 1) return exact

        val loose = findLooseSites(ram, 0)
        val referencedLoose = loose.filter { isJumpReferenced(it.ramOffset, jumpTargets) }
        if (referencedLoose.isNotEmpty()) return referencedLoose
        if (loose.size == 1) return loose
        val preferredLoose = loose.filter { site ->
            site.ramOffset in DISPATCHER_RAM_OFFSET until (DISPATCHER_RAM_OFFSET + 0x100)
        }
        return if (preferredLoose.size == 1) preferredLoose else emptyList()
    }

    private fun findExactSites(ram: ByteArray): List<DispatcherSite> {
        if (ram.size < WINDOW_SIZE) return emptyList()
        return (0..ram.size - WINDOW_SIZE step 4).mapNotNull { offset ->
            if (matchesDispatcher(ram, offset)) {
                DispatcherSite(offset, VALIDATION_BRANCH_OFFSET, ICON_LOAD_OFFSET, WINDOW_SIZE)
            } else {
                null
            }
        }
    }

    private fun findLooseSites(bytes: ByteArray, baseOffset: Int): List<DispatcherSite> {
        if (bytes.size < 8) return emptyList()
        val sites = mutableListOf<DispatcherSite>()
        for (loadAt in 0..bytes.size - 4 step 4) {
            val load = GameStateReader.u32(bytes, loadAt)
            if (!isLwImmediate(load, ICON_IMMEDIATE)) continue
            val destReg = rt(load)
            val searchFrom = (loadAt - LOOKBACK).coerceAtLeast(0)
            var branchAt = -1
            var walk = loadAt - 4
            while (walk >= searchFrom) {
                val word = GameStateReader.u32(bytes, walk)
                if (isBneInvolving(word, destReg)) {
                    branchAt = walk
                    break
                }
                walk -= 4
            }
            if (branchAt < 0) continue
            val windowStart = preferredWindowStart(bytes, branchAt, loadAt)
            sites += DispatcherSite(
                ramOffset = baseOffset + windowStart,
                branchOffset = branchAt - windowStart,
                iconLoadOffset = loadAt - windowStart,
                windowSize = loadAt + 4 - windowStart
            )
        }
        return sites
    }

    private fun preferredWindowStart(bytes: ByteArray, branchAt: Int, loadAt: Int): Int {
        val classicStart = loadAt - ICON_LOAD_OFFSET
        if (classicStart >= 0 && matchesDispatcher(bytes, classicStart)) return classicStart
        return minOf(branchAt, loadAt)
    }

    private fun matchesDispatcher(bytes: ByteArray, offset: Int): Boolean {
        if (offset < 0 || offset + WINDOW_SIZE > bytes.size) return false
        if (GameStateReader.u32(bytes, offset) != EXPECTED_FIRST_INSTRUCTION) return false
        val branch = GameStateReader.u32(bytes, offset + VALIDATION_BRANCH_OFFSET)
        if (branch and BRANCH_REGISTER_MASK != EXPECTED_VALIDATION_BRANCH_PREFIX) return false
        return GameStateReader.u32(bytes, offset + ICON_LOAD_OFFSET) == EXPECTED_ICON_LOAD
    }

    private fun jumpTargets(ram: ByteArray): Set<Int> {
        val targets = mutableSetOf<Int>()
        var instructionOffset = 0
        while (instructionOffset <= ram.size - 4) {
            val instruction = GameStateReader.u32(ram, instructionOffset).toInt()
            val opcode = instruction ushr 26
            if (opcode == 2 || opcode == 3) {
                targets += instruction and 0x03FFFFFF
            }
            instructionOffset += 4
        }
        return targets
    }

    private fun isJumpReferenced(ramOffset: Int, jumpTargets: Set<Int>): Boolean {
        val jumpTarget = ((ramOffset or 0x80000000.toInt()) ushr 2) and 0x03FFFFFF
        return jumpTarget in jumpTargets
    }

    private fun isLwImmediate(word: Long, immediate: Int): Boolean {
        val opcode = (word ushr 26).toInt()
        val imm = (word and 0xFFFF).toInt()
        return opcode == LW_OPCODE && imm == immediate
    }

    private fun isBneInvolving(word: Long, register: Int): Boolean {
        val opcode = (word ushr 26).toInt()
        if (opcode != BNE_OPCODE) return false
        return rs(word) == register || rt(word) == register
    }

    private fun rs(word: Long): Int = ((word ushr 21) and 0x1F).toInt()

    private fun rt(word: Long): Int = ((word ushr 16) and 0x1F).toInt()

    private fun oriZero(destReg: Int, immediate: Int): Long =
        ORI_OPCODE_PREFIX or (destReg.toLong() shl 16) or immediate.toLong()
}
