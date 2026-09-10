package com.digitaladventure.dw2003.model

data class BattleEnemy(
    val enemyId: Int,
    val name: String,
    val level: Int,
    val currentHp: Int,
    val maxHp: Int,
    val maxMp: Int,
    val strength: Int,
    val defense: Int,
    val spirit: Int,
    val wisdom: Int,
    val speed: Int,
    val resistances: List<Int>,
    val expMultiplier: Int,
    val attacks: List<String>,
    val loot: String = "",
    val liveHp: Boolean
) {
    val hpFraction: Float
        get() = if (maxHp <= 0) 0f else (currentHp.toFloat() / maxHp).coerceIn(0f, 1f)
}
