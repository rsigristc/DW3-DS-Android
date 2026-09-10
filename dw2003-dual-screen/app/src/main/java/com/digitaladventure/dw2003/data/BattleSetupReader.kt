package com.digitaladventure.dw2003.data

import com.digitaladventure.dw2003.model.BattleEnemy

object BattleSetupReader {
    const val SETUP_BASE = 0x42B1C
    const val SETUP_LENGTH = 0x50
    const val ARENA_BASE = 0xA4460
    const val ARENA_LENGTH = 0x100
    const val ARENA_STRIDE = 0x20
    const val SCAN_BASE = 0xA4000
    const val SCAN_LENGTH = 0x300
    private const val SLOT_STRIDE = 0x0C
    private const val FIRST_SLOT = 0x18
    private const val PSX_POINTER_MIN = 0x80000000L
    private const val PSX_POINTER_MAX = 0x801FFFFFL

    fun parse(
        setup: ByteArray,
        arena: ByteArray? = null,
        scan: ByteArray? = null,
        spanish: Boolean = true
    ): List<BattleEnemy> {
        val fromSlots = parseCopiedSlots(setup, arena, spanish)
        if (fromSlots.isNotEmpty()) return fromSlots
        val scanned = LinkedHashMap<Int, BattleEnemy>()
        collectPayloads(setup, arena, spanish, scanned)
        collectPayloads(arena, arena, spanish, scanned)
        collectPayloads(scan, arena, spanish, scanned)
        return scanned.values.take(3)
    }

    fun summarizeSlots(setup: ByteArray, spanish: Boolean = true): String {
        if (setup.size < FIRST_SLOT + SLOT_STRIDE) return ""
        return (0 until 3).mapNotNull { index ->
            val base = FIRST_SLOT + index * SLOT_STRIDE
            if (looksLikePointer(setup, base)) {
                val pointer = GameStateReader.u32(setup, base)
                return@mapNotNull "#${index + 1} ptr ${OverlaySignatures.hex(pointer)}"
            }
            val enemyId = GameStateReader.u16(setup, base)
            if (enemyId == 0) return@mapNotNull null
            val template = EnemyCatalog.template(enemyId)
            val name = template?.name ?: "0x${enemyId.toString(16).uppercase()}"
            val level = GameStateReader.u16(setup, base + 4)
            val maxHp = GameStateReader.u16(setup, base + 6)
            "#${index + 1} $name NV$level HP$maxHp"
        }.joinToString("  ·  ")
    }

    private fun parseCopiedSlots(
        setup: ByteArray,
        arena: ByteArray?,
        spanish: Boolean
    ): List<BattleEnemy> {
        if (setup.size < FIRST_SLOT + SLOT_STRIDE) return emptyList()
        return (0 until 3).mapNotNull { index ->
            val base = FIRST_SLOT + index * SLOT_STRIDE
            if (looksLikePointer(setup, base)) return@mapNotNull null
            fromPayload(setup, base, arena, index, spanish)
        }
    }

    private fun collectPayloads(
        window: ByteArray?,
        arena: ByteArray?,
        spanish: Boolean,
        into: MutableMap<Int, BattleEnemy>
    ) {
        if (window == null || window.size < SLOT_STRIDE) return
        var offset = 0
        while (offset + SLOT_STRIDE <= window.size) {
            if (!looksLikePointer(window, offset)) {
                fromPayload(window, offset, arena, into.size, spanish)?.let { enemy ->
                    into.putIfAbsent(enemy.enemyId, enemy)
                }
            }
            offset += 4
        }
    }

    private fun fromPayload(
        bytes: ByteArray,
        offset: Int,
        arena: ByteArray?,
        index: Int,
        spanish: Boolean
    ): BattleEnemy? {
        val enemyId = GameStateReader.u16(bytes, offset)
        val unused = GameStateReader.u16(bytes, offset + 2)
        val level = GameStateReader.u16(bytes, offset + 4)
        val maxHp = GameStateReader.u16(bytes, offset + 6)
        val maxMp = GameStateReader.u16(bytes, offset + 8)
        val multiplier = GameStateReader.u16(bytes, offset + 10)
        if (unused != 0) return null
        if (EnemyCatalog.template(enemyId) == null) return null
        if (maxHp !in 1..9999 || level !in 1..99) return null
        return toEnemy(enemyId, level, maxHp, maxMp, multiplier, liveHp(arena, index, maxHp), spanish)
    }

    private fun toEnemy(
        enemyId: Int,
        level: Int,
        maxHp: Int,
        maxMp: Int,
        multiplier: Int,
        liveHp: Int?,
        spanish: Boolean
    ): BattleEnemy {
        val template = EnemyCatalog.template(enemyId)
        return BattleEnemy(
            enemyId = enemyId,
            name = template?.name ?: "Enemy 0x${enemyId.toString(16).uppercase()}",
            level = level,
            currentHp = liveHp ?: maxHp,
            maxHp = maxHp,
            maxMp = maxMp,
            strength = template?.strength ?: 0,
            defense = template?.defense ?: 0,
            spirit = template?.spirit ?: 0,
            wisdom = template?.wisdom ?: 0,
            speed = template?.speed ?: 0,
            resistances = template?.resistances ?: List(7) { 0 },
            expMultiplier = multiplier,
            attacks = template?.attackIds?.map { id ->
                EnemyCatalog.techniqueLabel(id, spanish)
            } ?: emptyList(),
            loot = EnemyDrops.label(enemyId, spanish),
            liveHp = liveHp != null
        )
    }

    private fun liveHp(arena: ByteArray?, index: Int, maxHp: Int): Int? {
        if (arena == null) return null
        val candidates = listOf(
            (3 + index) * ARENA_STRIDE + 0x18,
            0x70 + index * ARENA_STRIDE,
            0x70 + index * ARENA_STRIDE + 8
        )
        return candidates.firstNotNullOfOrNull { offset ->
            GameStateReader.u16(arena, offset).takeIf { it in 1..maxHp }
        }
    }

    private fun looksLikePointer(bytes: ByteArray, offset: Int): Boolean {
        val word = GameStateReader.u32(bytes, offset)
        return word in PSX_POINTER_MIN..PSX_POINTER_MAX
    }
}
