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
    private val flaweIcons = setOf(
        0x0200, 0x0201, 0x0202, 0x021D, 0x021F, 0x0220, 0x0228,
        0x022C, 0x022E, 0x0232, 0x023E, 0x023F, 0x025D, 0x0780,
        0x0790, 0x0810, 0x0825, 0x0845, 0x0855
    )

    private val destinations: List<FastTravelDestination> = AreaCatalog.knownFieldIds()
        .filter { it in flaweIcons }
        .map { id ->
            val region = MapRegionCatalog.resolve(id)
            FastTravelDestination(
                areaId = id,
                name = AreaCatalog.name(id),
                server = region.server,
                sector = region.sector
            )
        }

    fun groups(
        storyStage: Int,
        visited: Set<Int>,
        currentAreaId: Int
    ): List<FastTravelGroup> {
        val unlocked = destinations.filter { destination ->
            isUnlocked(destination, storyStage, visited, currentAreaId)
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
        currentAreaId: Int
    ): Boolean {
        if (destination.server == ServerRegion.UNKNOWN) return false
        if (destination.areaId !in flaweIcons) return false
        return destination.areaId == currentAreaId || destination.areaId in visited
    }
}
