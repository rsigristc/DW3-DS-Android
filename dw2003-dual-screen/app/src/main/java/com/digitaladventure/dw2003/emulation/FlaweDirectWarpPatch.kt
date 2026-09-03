package com.digitaladventure.dw2003.emulation

import com.digitaladventure.dw2003.data.GameStateReader

/**
 * Flawe copies its fast-travel dispatcher to this scratch-RAM window.
 *
 * The unmodified dispatcher validates the map cursor at +0x00C and reads the
 * hovered icon at +0x04C. Replacing those two instructions only while Cross is
 * pressed lets Flawe's own table write the destination and exact spawn.
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
    private const val NOP = 0L
    private const val ORI_V1_ZERO = 0x34030000L

    private val iconCodes = mapOf(
        0x0200 to 0x14,
        0x021D to 0x1E
    )

    fun iconCode(areaId: Int): Int? = iconCodes[areaId]

    fun findActiveDispatcherOffsets(ram: ByteArray): List<Int> {
        val candidates = (0..ram.size - WINDOW_SIZE step 4).filter { offset ->
            matchesDispatcher(ram, offset)
        }
        val referencedTargets = mutableSetOf<Int>()
        (0..ram.size - 4 step 4).forEach { instructionOffset ->
            val instruction = GameStateReader.u32(ram, instructionOffset).toInt()
            val opcode = instruction ushr 26
            if (opcode == 2 || opcode == 3) {
                referencedTargets += instruction and 0x03FFFFFF
            }
        }
        return candidates.filter { candidate ->
            val jumpTarget = ((candidate or 0x80000000.toInt()) ushr 2) and 0x03FFFFFF
            jumpTarget in referencedTargets
        }
    }

    fun prepare(original: ByteArray, areaId: Int): ByteArray? {
        val iconCode = iconCode(areaId) ?: return null
        if (original.size != WINDOW_SIZE) return null
        if (!matchesDispatcher(original, 0)) return null

        return original.copyOf().also { patched ->
            GameMemoryController.writeU32(patched, VALIDATION_BRANCH_OFFSET, NOP)
            GameMemoryController.writeU32(
                patched,
                ICON_LOAD_OFFSET,
                ORI_V1_ZERO or iconCode.toLong()
            )
        }
    }

    private fun matchesDispatcher(bytes: ByteArray, offset: Int): Boolean {
        if (offset < 0 || offset + WINDOW_SIZE > bytes.size) return false
        if (GameStateReader.u32(bytes, offset) != EXPECTED_FIRST_INSTRUCTION) return false
        val branch = GameStateReader.u32(bytes, offset + VALIDATION_BRANCH_OFFSET)
        if (branch and BRANCH_REGISTER_MASK != EXPECTED_VALIDATION_BRANCH_PREFIX) return false
        return GameStateReader.u32(bytes, offset + ICON_LOAD_OFFSET) == EXPECTED_ICON_LOAD
    }
}
