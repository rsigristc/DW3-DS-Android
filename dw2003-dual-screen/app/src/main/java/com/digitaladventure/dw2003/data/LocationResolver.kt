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

    fun resolve(areaId: Int, mapId: Int, stageOverride: Int? = null): LocationDisplay {
        val mapName = AreaCatalog.knownName(mapId) ?: AreaCatalog.name(if (mapId != 0) mapId else areaId)
        val mapOverlay = isOverlay(mapId)
        val publicMapId = (stageOverride?.takeIf { isStage(it) } ?: stageId(areaId, mapId))
            .takeIf { it != 0 } ?: listOf(mapId, areaId).firstOrNull { it != 0 } ?: 0
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
        gameStarted && !isBlockingEvent(areaId, mapId, mode) &&
            (mode == GameMode.EXPLORATION ||
                (mode == GameMode.MANAGEMENT && (areaId == 0x1000 || mapId == 0x1000)))
}

/**
 * AREA and MAP_ID do not update together. PAL often leaves AREA on the map
 * being left; USA often leaves MAP_ID there. Follow whichever stage word
 * changed last so labels do not stick to the previous field.
 */
class LocationTracker {
    private var lastArea = -1
    private var lastMap = -1
    private var current = 0

    fun follow(areaId: Int, mapId: Int, overlayStageId: Int? = null): Int {
        if (overlayStageId != null && LocationResolver.isStage(overlayStageId)) {
            val ramStage = when {
                LocationResolver.isStage(mapId) -> mapId
                LocationResolver.isStage(areaId) -> areaId
                else -> 0
            }
            val overlayFits = ramStage == 0 ||
                overlayStageId == ramStage ||
                LocationResolver.isOverlay(mapId) ||
                sameSector(overlayStageId, ramStage)
            if (overlayFits) {
                current = overlayStageId
                lastArea = areaId
                lastMap = mapId
                return current
            }
        }
        val areaStage = LocationResolver.isStage(areaId)
        val mapStage = LocationResolver.isStage(mapId)
        if (LocationResolver.isOverlay(mapId)) {
            // MAP/START overwrite MAP_ID and often rewind AREA to a previous room.
            // Keep the last real stage; do not follow that rewind.
            if (current == 0 && areaStage) current = areaId
            lastArea = areaId
            lastMap = mapId
            return current
        }
        val areaChanged = areaStage && areaId != lastArea
        val mapChanged = mapStage && mapId != lastMap
        current = when {
            areaChanged && mapChanged -> mapId
            areaChanged -> areaId
            mapChanged -> mapId
            current != 0 -> current
            else -> LocationResolver.stageId(areaId, mapId)
        }
        lastArea = areaId
        lastMap = mapId
        return current
    }

    private fun sameSector(left: Int, right: Int): Boolean {
        val first = MapRegionCatalog.resolve(left)
        val second = MapRegionCatalog.resolve(right)
        return first.server != ServerRegion.UNKNOWN &&
            first.server == second.server &&
            first.sector == second.sector
    }
}
