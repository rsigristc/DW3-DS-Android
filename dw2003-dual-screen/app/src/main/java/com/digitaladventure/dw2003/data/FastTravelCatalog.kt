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
    private val destinations: List<FastTravelDestination> = AreaCatalog.knownFieldIds().map { id ->
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
        return destination.areaId == currentAreaId || destination.areaId in visited
    }
}
