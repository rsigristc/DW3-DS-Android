package com.digitaladventure.dw2003.data

/** Read-only evaluator of Flawe 2.0's objective decision routine.
 * Only its arithmetic, loads and branches are supported. No PSX code is run
 * in the emulator and no game memory is written. Input is one existing RAM
 * snapshot, so objectives can update without loading English menu assets.
 */
class FlaweGuideSelector(private val program: ByteArray) {
    fun select(main: ByteArray): String? {
        val registers = IntArray(32)
        var pc = ENTRY
        var next = pc + 4
        repeat(512) {
            if (pc == RESULT) {
                if ((3..5).any { registers[it] !in 0x2800..0x8200 }) return null
                return "%04x-%04x".format(registers[4], registers[5])
            }
            if (pc < ENTRY || pc + 4 > ENTRY + program.size || pc % 4 != 0) return null
            val instruction = GameStateReader.u32(program, pc - ENTRY).toInt()
            val op = instruction ushr 26
            val rs = (instruction ushr 21) and 31
            val rt = (instruction ushr 16) and 31
            val rd = (instruction ushr 11) and 31
            val shift = (instruction ushr 6) and 31
            val immediate = instruction and 0xFFFF
            val signed = immediate.toShort().toInt()
            var after = next + 4
            fun branch(taken: Boolean) {
                if (taken) after = pc + 4 + signed * 4
            }
            when (op) {
                0 -> when (instruction and 63) {
                    0 -> registers[rd] = registers[rt] shl shift
                    3 -> registers[rd] = registers[rt] shr shift
                    33 -> registers[rd] = registers[rs] + registers[rt]
                    37 -> registers[rd] = registers[rs] or registers[rt]
                    else -> return null
                }
                1 -> when (rt) {
                    0 -> branch(registers[rs] < 0)
                    1 -> branch(registers[rs] >= 0)
                    else -> return null
                }
                4 -> branch(registers[rs] == registers[rt])
                5 -> branch(registers[rs] != registers[rt])
                9 -> registers[rt] = registers[rs] + signed
                11 -> registers[rt] = if (Integer.compareUnsigned(registers[rs], signed) < 0) 1 else 0
                12 -> registers[rt] = registers[rs] and immediate
                15 -> registers[rt] = immediate shl 16
                32, 35, 36, 37 -> {
                    val address = registers[rs].toLong().and(0xFFFFFFFFL) + signed - 0x80048D00L
                    val length = when (op) { 35 -> 4; 37 -> 2; else -> 1 }
                    if (address < 0 || address + length > main.size) return null
                    val at = address.toInt()
                    registers[rt] = when (op) {
                        32 -> main[at].toInt()
                        36 -> main[at].toInt() and 255
                        37 -> GameStateReader.u16(main, at)
                        else -> GameStateReader.u32(main, at).toInt()
                    }
                }
                else -> return null
            }
            registers[0] = 0
            pc = next
            next = after
        }
        return null
    }

    companion object {
        private const val ENTRY = 0xC68
        private const val RESULT = 0xD4C
    }
}
