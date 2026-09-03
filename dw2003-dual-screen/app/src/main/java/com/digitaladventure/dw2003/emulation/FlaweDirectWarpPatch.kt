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

    private const val VALIDATION_BRANCH_OFFSET = 0x0C
    private const val ICON_LOAD_OFFSET = 0x4C
    private const val EXPECTED_FIRST_INSTRUCTION = 0x8E230180L // lw v1, 0x180(s1)
    private const val EXPECTED_VALIDATION_BRANCH = 0x14680271L // bne v1, t0, return
    private const val EXPECTED_ICON_LOAD = 0x8E230184L // lw v1, 0x184(s1)
    private const val NOP = 0L
    private const val ORI_V1_ZERO = 0x34030000L

    private val iconCodes = mapOf(
        0x0200 to 0x14,
        0x021D to 0x1E
    )

    fun iconCode(areaId: Int): Int? = iconCodes[areaId]

    fun prepare(original: ByteArray, areaId: Int): ByteArray? {
        val iconCode = iconCode(areaId) ?: return null
        if (original.size != WINDOW_SIZE) return null
        if (GameStateReader.u32(original, 0) != EXPECTED_FIRST_INSTRUCTION) return null
        if (GameStateReader.u32(original, VALIDATION_BRANCH_OFFSET) != EXPECTED_VALIDATION_BRANCH) return null
        if (GameStateReader.u32(original, ICON_LOAD_OFFSET) != EXPECTED_ICON_LOAD) return null

        return original.copyOf().also { patched ->
            GameMemoryController.writeU32(patched, VALIDATION_BRANCH_OFFSET, NOP)
            GameMemoryController.writeU32(
                patched,
                ICON_LOAD_OFFSET,
                ORI_V1_ZERO or iconCode.toLong()
            )
        }
    }
}
