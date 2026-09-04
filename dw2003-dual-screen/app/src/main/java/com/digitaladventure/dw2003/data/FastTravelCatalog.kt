package com.digitaladventure.dw2003.data

data class FastTravelDestination(
    val areaId: Int,
    val name: String,
    val server: ServerRegion,
    val sector: SectorRegion
)

data class FastTravelGroup(
    val server: ServerRegion,
    val sector: SectorRegion,
    val destinations: List<FastTravelDestination>
)

object FastTravelCatalog {
    /** Amaterasu city icons. Square swaps server; Flawe's IPS table only encodes Asuka MAP_IDs. */
    private val amaterasuIcons = setOf(0x0780, 0x0810, 0x0825, 0x0845, 0x0855)

    private val flaweIcons: Set<Int> = FlaweFastTravelTable.asukaMapIds + amaterasuIcons

    /**
     * Field tiles that share a nearby Flawe icon. Keys must themselves be Flawe icons.
     * Wire Forest Entrance, beaches and inns have their own ASKMAP slots, so they
     * are not folded into Central Park / Asuka City.
     */
    private val iconGroups: Map<Int, Set<Int>> = mapOf(
        0x0200 to (0x0200..0x021C).toSet(),
        0x0222 to setOf(0x0221, 0x0222),
        0x0225 to setOf(0x0225, 0x0226),
        0x022E to (0x022E..0x0231).toSet(),
        0x0234 to setOf(0x0233, 0x0234),
        0x023E to (0x023E..0x0241).toSet(),
        0x0242 to setOf(0x0242, 0x0243, 0x0244, 0x0245, 0x0246),
        0x024D to (0x024D..0x0257).toSet(),
        0x025B to setOf(0x025A, 0x025B, 0x025C),
        0x025D to (0x025D..0x0260).toSet(),
        0x0268 to setOf(0x0268, 0x0269),
        0x026F to setOf(
            0x026A, 0x026B, 0x026C, 0x026D, 0x026E, 0x026F,
            0x02D7, 0x02D8, 0x02D9, 0x02DA, 0x02DB, 0x02DC, 0x02DD, 0x02DE, 0x02DF
        ),
        0x0780 to setOf(0x0780, 0x0785, 0x0790, 0x0795, 0x0800, 0x0805),
        0x0810 to setOf(0x0810, 0x0820),
        0x0825 to setOf(0x0825, 0x0830, 0x0835, 0x0840),
        0x0845 to setOf(0x0845, 0x0850)
    )

    /** Vertical D-pad order of the currently exposed Flawe icons on Asuka Central. */
    val asukaCentralOrder: List<Int> = listOf(0x0200, 0x021D)

    private val destinations: List<FastTravelDestination> = flaweIcons.map { id ->
        val region = MapRegionCatalog.resolve(id)
        FastTravelDestination(
            areaId = id,
            name = AreaCatalog.name(id),
            server = region.server,
            sector = region.sector
        )
    }

    fun iconId(areaId: Int, mapId: Int = areaId): Int {
        val stage = LocationResolver.stageId(areaId, mapId)
        if (stage in flaweIcons) return stage
        return iconGroups.entries.firstOrNull { stage in it.value }?.key ?: 0
    }

    fun cycleOrder(iconId: Int): List<Int> {
        val region = MapRegionCatalog.resolve(iconId)
        return if (region.server == ServerRegion.ASUKA && region.sector == SectorRegion.CENTRAL) {
            asukaCentralOrder
        } else {
            listOf(iconId)
        }
    }

    fun rememberedIcons(visited: Set<Int>, currentAreaId: Int, currentMapId: Int = currentAreaId): Set<Int> {
        return (visited + currentAreaId + currentMapId).mapNotNull { tile ->
            iconId(tile).takeIf { it in flaweIcons }
        }.toSet()
    }

    fun groups(
        storyStage: Int,
        visited: Set<Int>,
        currentAreaId: Int,
        currentMapId: Int = currentAreaId
    ): List<FastTravelGroup> {
        val currentIcon = iconId(currentAreaId, currentMapId)
        val unlockedIcons = rememberedIcons(visited, currentAreaId, currentMapId)
        val unlocked = destinations.filter { destination ->
            destination.areaId in unlockedIcons ||
                isUnlocked(destination, storyStage, visited, currentIcon)
        }
        return unlocked
            .groupBy { it.server to it.sector }
            .entries
            .sortedWith(compareBy({ it.key.first.ordinal }, { it.key.second.ordinal }))
            .map { (key, values) ->
                FastTravelGroup(key.first, key.second, values.sortedBy { it.name })
            }
    }

    @Suppress("UNUSED_PARAMETER")
    fun isUnlocked(
        destination: FastTravelDestination,
        storyStage: Int,
        visited: Set<Int>,
        currentIconId: Int
    ): Boolean {
        if (destination.server == ServerRegion.UNKNOWN) return false
        if (destination.areaId !in flaweIcons) return false
        if (destination.areaId == currentIconId) return true
        return visited.any { tile -> iconId(tile) == destination.areaId }
    }
}
