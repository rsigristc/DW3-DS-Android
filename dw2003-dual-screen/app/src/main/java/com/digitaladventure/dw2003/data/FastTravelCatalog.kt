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
    /** Icons that appear on Flawe's world map, not city interiors or the bridge. */
    private val flaweIcons = setOf(
        0x0200, 0x021D, 0x021F, 0x0220, 0x0228,
        0x022C, 0x022E, 0x0232, 0x023E, 0x025D, 0x0780,
        0x0810, 0x0825, 0x0845, 0x0855
    )

    private val iconGroups: Map<Int, Set<Int>> = mapOf(
        0x0200 to (0x0200..0x021C).toSet(),
        0x021D to setOf(0x021D, 0x021E)
    )

    /** Cycle order of Flawe icons on Asuka Central (L1/R1 on the world map). */
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
        return iconGroups.entries.firstOrNull { stage in it.value }?.key
            ?: stage.takeIf { it in flaweIcons }
            ?: 0
    }

    fun cycleOrder(iconId: Int): List<Int> {
        val region = MapRegionCatalog.resolve(iconId)
        return if (region.server == ServerRegion.ASUKA && region.sector == SectorRegion.CENTRAL) {
            asukaCentralOrder
        } else {
            listOf(iconId)
        }
    }

    fun groups(
        storyStage: Int,
        visited: Set<Int>,
        currentAreaId: Int,
        currentMapId: Int = currentAreaId
    ): List<FastTravelGroup> {
        val currentIcon = iconId(currentAreaId, currentMapId)
        val unlocked = destinations.filter { destination ->
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
