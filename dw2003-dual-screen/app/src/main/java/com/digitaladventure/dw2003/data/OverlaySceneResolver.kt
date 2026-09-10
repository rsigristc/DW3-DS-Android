package com.digitaladventure.dw2003.data

import com.digitaladventure.dw2003.model.GameMode
import com.digitaladventure.dw2003.model.OverlayScene

object OverlaySceneResolver {
    const val MENU_OVERLAY = 0x1000

    fun resolve(
        mode: GameMode,
        areaId: Int,
        mapId: Int,
        fieldMenuVisible: Boolean? = null,
        flaweMapLoaded: Boolean = false
    ): OverlayScene {
        if (mode == GameMode.BATTLE) return OverlayScene.BATTLE
        if (mode == GameMode.MANAGEMENT) return OverlayScene.STATUS
        val menuOverlay = areaId == MENU_OVERLAY || mapId == MENU_OVERLAY
        val startList = fieldMenuVisible == true
        val mapOpen = flaweMapLoaded || (menuOverlay && !startList)
        if (mapOpen && isWarpInProgress(areaId, mapId)) return OverlayScene.FAST_TRAVEL
        if (mapOpen) return OverlayScene.MAP
        if (startList || menuOverlay) return OverlayScene.MENU
        return OverlayScene.FIELD
    }

    private fun isWarpInProgress(areaId: Int, mapId: Int): Boolean {
        if (!AreaCatalog.isField(mapId)) return false
        return areaId == MENU_OVERLAY || (AreaCatalog.isField(areaId) && areaId != mapId)
    }
}
