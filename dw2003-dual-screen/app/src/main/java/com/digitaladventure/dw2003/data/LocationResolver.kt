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
    fun isOverlay(id: Int): Boolean = AreaCatalog.isOverlay(id)

    fun isStage(id: Int): Boolean = id != 0 && !isOverlay(id)

    /**
     * The transition banner and the two screenshots around Central Park show
     * MAP_ID on the destination while AREA still contains the map being left.
     * Use MAP_ID for every normal stage; AREA remains the fallback for overlays
     * and the short interval before MAP_ID is initialized.
     */
    fun stageId(areaId: Int, mapId: Int): Int {
        val areaStage = isStage(areaId)
        val mapStage = isStage(mapId)
        return when {
            isOverlay(mapId) && areaStage -> areaId
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
