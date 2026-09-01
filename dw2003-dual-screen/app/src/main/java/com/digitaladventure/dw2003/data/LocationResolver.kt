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
    private val indoorRooms = setOf(
        0x0203, 0x0204, 0x0206, 0x0207, 0x0208, 0x020A, 0x020C, 0x020D, 0x020E, 0x020F,
        0x0210, 0x0213, 0x0214, 0x0217, 0x0218, 0x021C,
        0x0223, 0x0224, 0x0238, 0x0239, 0x023F, 0x0240,
        0x0790, 0x0795, 0x0800, 0x0830, 0x0835
    )

    fun isOverlay(id: Int): Boolean = AreaCatalog.isOverlay(id)

    fun resolve(areaId: Int, mapId: Int): LocationDisplay {
        val areaName = AreaCatalog.name(areaId)
        val mapName = AreaCatalog.knownName(mapId) ?: AreaCatalog.name(if (mapId != 0) mapId else areaId)
        val areaOverlay = isOverlay(areaId)
        val mapOverlay = isOverlay(mapId)
        val indoor = isCityInterior(areaId, mapId)
        val publicMapId = when {
            indoor -> mapId
            !areaOverlay && areaId != 0 -> areaId
            !mapOverlay && mapId != 0 -> mapId
            else -> areaId
        }
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
            roomName = areaName.takeIf {
                indoor && !areaOverlay && areaId != 0 && !it.equals(title, ignoreCase = true)
            },
            mapLabel = if (mapOverlay) mapName else title,
            areaId = areaId,
            mapId = if (mapId != 0) mapId else areaId
        )
    }

    fun isCityInterior(areaId: Int, mapId: Int): Boolean =
        !isOverlay(areaId) &&
            mapId in cityHubs &&
            areaId != mapId &&
            areaId in indoorRooms

    fun isBlockingEvent(areaId: Int, mapId: Int, mode: GameMode): Boolean {
        if (mode == GameMode.BATTLE) return true
        return AreaCatalog.isBlockingEvent(areaId) || AreaCatalog.isBlockingEvent(mapId)
    }

    fun canReorderParty(areaId: Int, mapId: Int, mode: GameMode, gameStarted: Boolean): Boolean =
        gameStarted && mode != GameMode.BATTLE && !isBlockingEvent(areaId, mapId, mode)

    fun canFastTravel(areaId: Int, mapId: Int, mode: GameMode, gameStarted: Boolean): Boolean =
        gameStarted && mode == GameMode.EXPLORATION && !isBlockingEvent(areaId, mapId, mode)
}
