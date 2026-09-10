package com.digitaladventure.dw2003.data

import com.digitaladventure.dw2003.model.GameMode
import com.digitaladventure.dw2003.model.OverlayScene
import org.junit.Assert.assertEquals
import org.junit.Test

class OverlaySceneResolverTest {
    @Test
    fun battleAndStatusWin() {
        assertEquals(
            OverlayScene.BATTLE,
            OverlaySceneResolver.resolve(GameMode.BATTLE, 0x021D, 0x021D)
        )
        assertEquals(
            OverlayScene.STATUS,
            OverlaySceneResolver.resolve(GameMode.MANAGEMENT, 0x1000, 0x1000)
        )
    }

    @Test
    fun fieldStartListIsMenu() {
        assertEquals(
            OverlayScene.MENU,
            OverlaySceneResolver.resolve(
                GameMode.EXPLORATION,
                0x021D,
                0x021D,
                fieldMenuVisible = true
            )
        )
    }

    @Test
    fun flaweDispatcherIsMap() {
        assertEquals(
            OverlayScene.MAP,
            OverlaySceneResolver.resolve(
                GameMode.EXPLORATION,
                0x021D,
                0x021D,
                flaweMapLoaded = true
            )
        )
    }

    @Test
    fun menuOverlayWithoutStartListIsMap() {
        assertEquals(
            OverlayScene.MAP,
            OverlaySceneResolver.resolve(GameMode.EXPLORATION, 0x1000, 0x1000)
        )
    }

    @Test
    fun destinationMapIdDuringMapIsFastTravel() {
        assertEquals(
            OverlayScene.FAST_TRAVEL,
            OverlaySceneResolver.resolve(
                GameMode.EXPLORATION,
                0x1000,
                0x0229,
                flaweMapLoaded = true
            )
        )
    }
}
