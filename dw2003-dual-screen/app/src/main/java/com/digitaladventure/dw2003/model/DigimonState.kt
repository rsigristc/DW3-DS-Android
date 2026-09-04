package com.digitaladventure.dw2003.model

import com.digitaladventure.dw2003.data.ExperienceTable
import com.digitaladventure.dw2003.data.EquipmentBonuses
import com.digitaladventure.dw2003.data.EquipmentCatalog
import com.digitaladventure.dw2003.data.EquipmentInfo
import com.digitaladventure.dw2003.data.TechniqueCatalog
import com.digitaladventure.dw2003.data.DigievolutionCatalog
import com.digitaladventure.dw2003.data.TechniqueInfo

data class DigimonState(
    val profileId: Int,
    val name: String,
    val level: Int,
    val experience: Long,
    val trainingPoints: Int,
    val currentHp: Int,
    val maxHp: Int,
    val currentMp: Int,
    val maxMp: Int,
    val strength: Int,
    val defense: Int,
    val spirit: Int,
    val wisdom: Int,
    val speed: Int,
    val charisma: Int,
    val tolerances: List<Int>,
    val equipmentIds: List<Int>,
    val activeDigievolutionId: Int = 0,
    val activeDigievolutionLevel: Int = 1
) {
    val hpFraction: Float get() = ratio(currentHp, maxHp)
    val mpFraction: Float get() = ratio(currentMp, maxMp)
    val nextLevelExperience: Long? get() = ExperienceTable.nextLevel(profileId, level)
    val experienceRemaining: Long get() = (nextLevelExperience?.minus(experience) ?: 0L).coerceAtLeast(0L)
    val experienceFraction: Float get() = ExperienceTable.progress(profileId, level, experience)
    val activeDigievolutionName: String
        get() = DigievolutionCatalog.name(activeDigievolutionId) ?: "Forma Rookie"
    val activeSkills: List<TechniqueInfo>
        get() = if (activeDigievolutionId == 0 || activeDigievolutionId == 0xFFFF) {
            listOfNotNull(TechniqueCatalog.signatureInfo(profileId))
        } else {
            DigievolutionCatalog.techniques(activeDigievolutionId, activeDigievolutionLevel)
        }
    val equippedItems: List<EquipmentInfo?> get() = equipmentIds.map(EquipmentCatalog::get)
    val equipmentBonuses: EquipmentBonuses get() = EquipmentBonuses.of(equippedItems)
    val totalStrength: Int get() = strength + equipmentBonuses.strength
    val totalDefense: Int get() = defense + equipmentBonuses.defense
    val totalSpirit: Int get() = spirit + equipmentBonuses.spirit
    val totalWisdom: Int get() = wisdom + equipmentBonuses.wisdom
    val totalSpeed: Int get() = speed + equipmentBonuses.speed
    val totalCharisma: Int get() = charisma + equipmentBonuses.charisma
    val totalResistances: List<Int>
        get() = List(7) { index ->
            (tolerances.getOrNull(index) ?: 0) + equipmentBonuses.resistance(index)
        }

    private fun ratio(value: Int, maximum: Int): Float =
        if (maximum <= 0) 0f else (value.toFloat() / maximum).coerceIn(0f, 1f)
}
