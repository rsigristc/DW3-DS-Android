package com.digitaladventure.dw2003.data

import com.digitaladventure.dw2003.model.GameMode

data class LocationDisplay(
    val title: String,
    val detail: String,
    val radarLabel: String,
    val publicMapId: Int,
    val roomName: String?,
    val mapLabel: String,
    val areaId: Int,
    val mapId: Int
)

object LocationResolver {
    private val cityHubs = setOf(
        0x0200, 0x0201, 0x022E, 0x023E, 0x025D,
        0x0780, 0x0810, 0x0825, 0x0845, 0x0855
    )

    fun isOverlay(id: Int): Boolean = AreaCatalog.isOverlay(id)

    fun isCityHub(id: Int): Boolean = id in cityHubs

    fun isStage(id: Int): Boolean = id != 0 && !isOverlay(id)

    /**
     * In-game banners (Posada, Salón, Puente Asuka) use the specific stage.
     * `AREA` often stays on the city hub (`0x0200`) while `MAP_ID` is the room
     * or bridge; interiors can also store the room in `AREA` and the hub in
     * `MAP_ID`. Prefer the non-hub stage so the companion matches the overlay.
     */
    fun stageId(areaId: Int, mapId: Int): Int {
        val areaStage = isStage(areaId)
        val mapStage = isStage(mapId)
        val areaHub = isCityHub(areaId)
        val mapHub = isCityHub(mapId)
        return when {
            areaHub && mapStage && !mapHub -> mapId
            mapHub && areaStage && !areaHub -> areaId
            mapStage && isOverlay(areaId) -> mapId
            areaStage && isOverlay(mapId) -> areaId
            mapStage && areaStage -> mapId
            mapStage -> mapId
            areaStage -> areaId
            else -> areaId
        }
    }

    fun resolve(areaId: Int, mapId: Int): LocationDisplay {
        val mapName = AreaCatalog.knownName(mapId) ?: AreaCatalog.name(if (mapId != 0) mapId else areaId)
        val mapOverlay = isOverlay(mapId)
        val publicMapId = stageId(areaId, mapId).takeIf { it != 0 } ?: listOf(mapId, areaId).firstOrNull { it != 0 } ?: 0
        val title = AreaCatalog.name(publicMapId.takeIf { it != 0 } ?: areaId)
        val region = MapRegionCatalog.resolve(publicMapId).let { mapRegion ->
            if (mapRegion.server == ServerRegion.UNKNOWN) MapRegionCatalog.resolve(areaId) else mapRegion
        }
        val radarLabel = "${region.sector.label} · $title"
        return LocationDisplay(
            title = title,
            detail = "$radarLabel · 0x${AreaCatalog.hex(publicMapId)}",
            radarLabel = radarLabel,
            publicMapId = publicMapId,
            roomName = null,
            mapLabel = if (mapOverlay) mapName else title,
            areaId = areaId,
            mapId = if (mapId != 0) mapId else areaId
        )
    }

    fun isBlockingEvent(areaId: Int, mapId: Int, mode: GameMode): Boolean {
        if (mode == GameMode.BATTLE) return true
        return AreaCatalog.isBlockingEvent(areaId) || AreaCatalog.isBlockingEvent(mapId)
    }

    fun canReorderParty(areaId: Int, mapId: Int, mode: GameMode, gameStarted: Boolean): Boolean =
        gameStarted && mode != GameMode.BATTLE && !isBlockingEvent(areaId, mapId, mode)

    fun canFastTravel(areaId: Int, mapId: Int, mode: GameMode, gameStarted: Boolean): Boolean =
        gameStarted && mode == GameMode.EXPLORATION && !isBlockingEvent(areaId, mapId, mode)
}
