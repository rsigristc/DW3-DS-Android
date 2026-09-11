package com.digitaladventure.dw2003.remote

import com.digitaladventure.dw2003.data.CheatSpec
import com.digitaladventure.dw2003.data.FastTravelCatalog
import com.digitaladventure.dw2003.model.GameSnapshot
import com.digitaladventure.dw2003.remote.protocol.RemoteEquipment
import com.digitaladventure.dw2003.remote.protocol.RemotePartyMember
import com.digitaladventure.dw2003.remote.protocol.RemoteEnemy
import com.digitaladventure.dw2003.remote.protocol.RemoteForm
import com.digitaladventure.dw2003.remote.protocol.RemoteMod
import com.digitaladventure.dw2003.remote.protocol.RemoteSkill
import com.digitaladventure.dw2003.remote.protocol.RemoteTelemetryFrame
import com.digitaladventure.dw2003.remote.protocol.RemoteTravelDestination
import com.digitaladventure.dw2003.remote.protocol.RemoteValue

fun GameSnapshot.toRemoteTelemetry(
    pairingCode: Int,
    sequence: Int,
    muted: Boolean = false,
    fastForward: Boolean = false,
    stateAvailable: Boolean = false,
    modsEnabled: Boolean = false,
    cheats: List<CheatSpec> = emptyList(),
    enabledCheats: Set<String> = emptySet(),
    visitedMaps: Set<Int> = emptySet()
) = RemoteTelemetryFrame(
    pairingCode = pairingCode,
    sequence = sequence,
    sampledAtMillis = sampledAtMillis,
    mode = mode.label,
    gameStarted = gameStarted,
    areaId = areaId,
    mapId = mapId,
    locationTitle = locationTitle,
    locationDetail = locationDetail,
    tamerName = tamerName,
    objective = objective,
    bits = bits,
    storyStage = storyStage,
    serverName = serverName,
    sectorName = sectorName,
    muted = muted,
    fastForward = fastForward,
    stateAvailable = stateAvailable,
    canReorderParty = canReorderParty,
    canFastTravel = canFastTravel && supportsFastTravel,
    travelDestinations = FastTravelCatalog.groups(storyStage, visitedMaps + revealedMapIds, publicMapId, publicMapId)
        .flatMap { group -> group.destinations.map { destination ->
            RemoteTravelDestination(
                destination.areaId, destination.name,
                "${group.server.name} · ${group.sector.name}",
                destination.areaId == FastTravelCatalog.iconId(publicMapId)
            )
        } },
    modsEnabled = modsEnabled,
    mods = cheats.mapIndexed { index, cheat ->
        RemoteMod(index, cheat.label, cheat.detail, cheat.id in enabledCheats, cheat.id.startsWith("custom_"))
    },
    party = party.map { member ->
        val bonuses = member.equipmentBonuses
        RemotePartyMember(
            name = member.name,
            level = member.level,
            currentHp = member.currentHp,
            maxHp = member.maxHp,
            currentMp = member.currentMp,
            maxMp = member.maxMp,
            experience = member.experience,
            nextLevelExperience = member.nextLevelExperience ?: member.experience,
            activeForm = member.activeDigievolutionName,
            profileId = member.profileId,
            trainingPoints = member.trainingPoints,
            forms = member.displayedForms.map { RemoteForm(it.name, it.level, it.active) },
            parameters = listOf(
                RemoteValue("FUERZA", member.totalStrength, bonuses.strength),
                RemoteValue("DEFENSA", member.totalDefense, bonuses.defense),
                RemoteValue("ESPÍRITU", member.totalSpirit, bonuses.spirit),
                RemoteValue("SABIDURÍA", member.totalWisdom, bonuses.wisdom),
                RemoteValue("VELOCIDAD", member.totalSpeed, bonuses.speed),
                RemoteValue("CARISMA", member.totalCharisma, bonuses.charisma)
            ),
            resistances = listOf("FUEGO", "AGUA", "HIELO", "VIENTO", "RAYO", "MÁQUINA", "OSCURIDAD")
                .mapIndexed { index, name -> RemoteValue(name, member.totalResistances[index], bonuses.resistance(index)) },
            statusResistances = listOf("VENENO", "PARÁLISIS", "CONFUSIÓN", "SUEÑO", "K.O.")
                .mapIndexedNotNull { index, name -> member.statusResistances.getOrNull(index)?.let { RemoteValue(name, it) } },
            skills = member.activeSkills.map { RemoteSkill(it.name, it.mp ?: -1, it.power ?: -1) },
            equipment = member.equippedItems.mapIndexed { index, item ->
                val slot = listOf("CABEZA", "CUERPO", "MANO DER.", "MANO IZQ.", "ACCESORIO 1", "ACCESORIO 2")[index]
                RemoteEquipment(slot, item?.name ?: "— VACÍO —", item?.type.orEmpty(), item?.stats.orEmpty())
            }
        )
    },
    enemies = enemies.map { enemy ->
        RemoteEnemy(
            name = enemy.name,
            level = enemy.level,
            currentHp = enemy.currentHp,
            maxHp = enemy.maxHp,
            attacks = enemy.attacks.joinToString(" · "),
            loot = enemy.loot,
            parameters = listOf(
                RemoteValue("FUERZA", enemy.strength), RemoteValue("DEFENSA", enemy.defense),
                RemoteValue("ESPÍRITU", enemy.spirit), RemoteValue("SABIDURÍA", enemy.wisdom),
                RemoteValue("VELOCIDAD", enemy.speed)
            ),
            resistances = listOf("FUEGO", "AGUA", "HIELO", "VIENTO", "RAYO", "MÁQUINA", "OSCURIDAD")
                .mapIndexed { index, name -> RemoteValue(name, enemy.resistances.getOrElse(index) { 0 }) },
            statusResistances = listOf("VENENO", "PARÁLISIS", "CONFUSIÓN", "SUEÑO", "K.O.")
                .mapIndexedNotNull { index, name -> enemy.statusResistances.getOrNull(index)?.let { RemoteValue(name, it) } }
        )
    }
)
