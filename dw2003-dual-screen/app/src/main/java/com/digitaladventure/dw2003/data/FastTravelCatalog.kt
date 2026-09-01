package com.digitaladventure.dw2003.data

data class FastTravelDestination(
    val areaId: Int,
    val name: String,
    val server: ServerRegion,
    val sector: SectorRegion,
    val minStory: Int
)

data class FastTravelGroup(
    val server: ServerRegion,
    val sector: SectorRegion,
    val destinations: List<FastTravelDestination>
)

object FastTravelCatalog {
    private val hubs = setOf(
        0x0200, 0x0201, 0x0202, 0x021D, 0x021F, 0x0220, 0x0228,
        0x022C, 0x022E, 0x0232, 0x023E, 0x023F, 0x025D, 0x0780,
        0x0790, 0x0810, 0x0825, 0x0845, 0x0855
    )
    private val sectorStory = mapOf(
        (ServerRegion.ASUKA to SectorRegion.CENTRAL) to 1,
        (ServerRegion.ASUKA to SectorRegion.EAST) to 8,
        (ServerRegion.ASUKA to SectorRegion.SOUTH) to 14,
        (ServerRegion.ASUKA to SectorRegion.WEST) to 20,
        (ServerRegion.ASUKA to SectorRegion.NORTH) to 28,
        (ServerRegion.AMATERASU to SectorRegion.CENTRAL) to 36,
        (ServerRegion.AMATERASU to SectorRegion.EAST) to 42,
        (ServerRegion.AMATERASU to SectorRegion.SOUTH) to 48,
        (ServerRegion.AMATERASU to SectorRegion.WEST) to 54,
        (ServerRegion.AMATERASU to SectorRegion.NORTH) to 60
    )

    private val destinations: List<FastTravelDestination> = AreaCatalog.knownFieldIds().map { id ->
        val region = MapRegionCatalog.resolve(id)
        FastTravelDestination(
            areaId = id,
            name = AreaCatalog.name(id),
            server = region.server,
            sector = region.sector,
            minStory = sectorStory[region.server to region.sector] ?: 99
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

    fun isUnlocked(
        destination: FastTravelDestination,
        storyStage: Int,
        visited: Set<Int>,
        currentAreaId: Int
    ): Boolean {
        if (destination.areaId == currentAreaId) return true
        if (destination.areaId in visited) return true
        if (destination.server == ServerRegion.UNKNOWN) return false
        val sectorVisited = visited.any { id ->
            val region = MapRegionCatalog.resolve(id)
            region.server == destination.server && region.sector == destination.sector
        }
        return destination.areaId in hubs &&
            storyStage >= destination.minStory &&
            (sectorVisited || destination.minStory <= 1)
    }
}
