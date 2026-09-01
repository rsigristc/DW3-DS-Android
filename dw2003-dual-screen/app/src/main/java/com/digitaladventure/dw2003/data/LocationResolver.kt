package com.digitaladventure.dw2003.data

import com.digitaladventure.dw2003.model.GameMode

data class LocationDisplay(
    val title: String,
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

    fun resolve(areaId: Int, mapId: Int): LocationDisplay {
        val areaName = AreaCatalog.name(areaId)
        val mapName = AreaCatalog.knownName(mapId) ?: AreaCatalog.name(if (mapId != 0) mapId else areaId)
        val areaOverlay = isOverlay(areaId)
        val mapOverlay = isOverlay(mapId)

        val fieldId = when {
            !areaOverlay && areaId != 0 -> areaId
            !mapOverlay && mapId != 0 -> mapId
            else -> 0
        }
        val fieldName = if (fieldId != 0) AreaCatalog.name(fieldId) else areaName

        val title = when {
            !mapOverlay && mapId in cityHubs && !areaOverlay && areaId != 0 -> AreaCatalog.name(mapId)
            !areaOverlay && areaId != 0 -> areaName
            !mapOverlay && mapId != 0 -> mapName
            else -> fieldName
        }
        val room = areaName.takeIf {
            !areaOverlay && areaId != 0 && !it.equals(title, ignoreCase = true)
        }
        return LocationDisplay(
            title = title,
            roomName = room,
            mapLabel = if (mapOverlay) mapName else (room ?: mapName),
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
