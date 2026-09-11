package com.digitaladventure.dw2003.ui

import com.digitaladventure.dw2003.data.CheatCatalog
import com.digitaladventure.dw2003.data.CompanionLanguage
import com.digitaladventure.dw2003.data.CompanionLanguageSetting
import com.digitaladventure.dw2003.data.SectorRegion
import com.digitaladventure.dw2003.data.ServerRegion
import com.digitaladventure.dw2003.model.GameMode
import org.junit.Assert.assertEquals
import org.junit.Test

class CompanionUiTextTest {
    @Test
    fun localizesModesServersAndAsukaCity() {
        assertEquals("Exploration", CompanionUiText.mode(CompanionLanguage.ENGLISH, GameMode.EXPLORATION))
        assertEquals("Asuka Server", CompanionUiText.server(CompanionLanguage.ENGLISH, ServerRegion.ASUKA))
        assertEquals("Central Sector", CompanionUiText.sector(CompanionLanguage.ENGLISH, SectorRegion.CENTRAL))
        assertEquals("Asuka City", CompanionUiText.area(CompanionLanguage.ENGLISH, 0x0200))
        assertEquals("Ciudad Asuka", CompanionUiText.area(CompanionLanguage.SPANISH, 0x0200))
        assertEquals(
            "Central Sector · Digimon Lab · 0x0206",
            CompanionUiText.locationDetail(CompanionLanguage.ENGLISH, 0x0200, 0x0206)
        )
        assertEquals("Digimon Arena", CompanionUiText.area(CompanionLanguage.ENGLISH, 0x0209))
        assertEquals("Estadio Digimon", CompanionUiText.area(CompanionLanguage.SPANISH, 0x0209))
        assertEquals("Boot Mountain", CompanionUiText.area(CompanionLanguage.ENGLISH, 0x0261))
        assertEquals("Montaña de Bota", CompanionUiText.area(CompanionLanguage.SPANISH, 0x0261))
        assertEquals("Chamber Room", CompanionUiText.area(CompanionLanguage.ENGLISH, 0x026D))
        assertEquals("Magasta B1F", CompanionUiText.area(CompanionLanguage.ENGLISH, 0x02DA))
        assertEquals(
            "North Sector · Genbu City · 0x026F",
            CompanionUiText.locationDetail(CompanionLanguage.ENGLISH, 0x026F, 0x026F)
        )
    }

    @Test
    fun localizesCheatsPaneAndEquipmentAbbreviations() {
        val bits = CheatCatalog.byId("infinite_bits")!!
        assertEquals("Max bits", CompanionUiText.cheatLabel(CompanionLanguage.ENGLISH, bits))
        assertEquals(
            "Game top · panel bottom",
            CompanionUiText.paneArrangement(CompanionLanguage.ENGLISH, PaneArrangement.GAME_TOP)
        )
        assertEquals("Automatic", CompanionUiText.languageSetting(CompanionLanguage.ENGLISH, CompanionLanguageSetting.AUTO))
        assertEquals("SAVE", CompanionUiText.quickAction(CompanionLanguage.ENGLISH, QuickAction.SAVE_STATE, true, false, false))
        assertEquals("SOUND OFF", CompanionUiText.quickAction(CompanionLanguage.ENGLISH, QuickAction.TOGGLE_MUTE, true, false, true))
        assertEquals("SONIDO ON", CompanionUiText.quickAction(CompanionLanguage.SPANISH, QuickAction.TOGGLE_MUTE, true, false, false))
        assertEquals("2× in battle", CompanionUiText.battleScale(CompanionLanguage.ENGLISH, BattleScale.BATTLE_2X))
        assertEquals("Always 2×", CompanionUiText.battleScale(CompanionLanguage.ENGLISH, BattleScale.ALWAYS_2X))
        assertEquals("AA+ (2D+3D)", CompanionUiText.videoFilter(CompanionLanguage.ENGLISH, VideoFilter.ANTIALIAS_PLUS))
        assertEquals("AA+ (2D+3D)", CompanionUiText.imageOptions(CompanionLanguage.SPANISH, BattleScale.OFF, VideoFilter.ANTIALIAS_PLUS))
        assertEquals("Ninguno", CompanionUiText.imageOptions(CompanionLanguage.SPANISH, BattleScale.OFF, VideoFilter.SHARP))
        assertEquals("3D 2× (battle only)", CompanionUiText.imageOptions(CompanionLanguage.ENGLISH, BattleScale.BATTLE_2X, VideoFilter.SHARP))
        assertEquals("Automático (gama baja)", CompanionUiText.performanceMode(CompanionLanguage.SPANISH, PerformanceMode.AUTO))
        assertEquals("AA+", CompanionUiText.imageOptionsShort(CompanionLanguage.ENGLISH, BattleScale.OFF, VideoFilter.ANTIALIAS_PLUS))
        assertEquals("2× BAT", CompanionUiText.imageOptionsShort(CompanionLanguage.ENGLISH, BattleScale.BATTLE_2X, VideoFilter.SHARP))
        assertEquals("2× ON", CompanionUiText.battleScaleShort(CompanionLanguage.ENGLISH, BattleScale.ALWAYS_2X))
        assertEquals("Sin encuentros leídos", CompanionUiText.emptyEnemies(CompanionLanguage.SPANISH))
        assertEquals("No encounters read", CompanionUiText.emptyEnemies(CompanionLanguage.ENGLISH))
        assertEquals("Menú", CompanionUiText.scene(CompanionLanguage.SPANISH, com.digitaladventure.dw2003.model.OverlayScene.MENU))
        assertEquals("Fast travel", CompanionUiText.scene(CompanionLanguage.ENGLISH, com.digitaladventure.dw2003.model.OverlayScene.FAST_TRAVEL))
        assertEquals("NATIVE", CompanionUiText.battleScaleShort(CompanionLanguage.ENGLISH, BattleScale.OFF))
        assertEquals("Off", CompanionUiText.idleMode(CompanionLanguage.ENGLISH, CompanionIdleMode.OFF))
        assertEquals("Atenuar panel", CompanionUiText.idleMode(CompanionLanguage.SPANISH, CompanionIdleMode.DIM))
        assertEquals("Apagar panel", CompanionUiText.idleMode(CompanionLanguage.SPANISH, CompanionIdleMode.SLEEP))
        assertEquals("30 seconds", CompanionUiText.idleDelay(CompanionLanguage.ENGLISH, CompanionIdleDelay.S30))
        assertEquals("+14 STR · +15 CHA", CompanionUiText.equipmentStats(CompanionLanguage.ENGLISH, "+14 FUE · +15 CAR"))
        assertEquals("+20 FIRE", CompanionUiText.equipmentStats(CompanionLanguage.ENGLISH, "+20 FUEGO"))
        assertEquals("+20 MCH · +3 CHA", CompanionUiText.equipmentStats(CompanionLanguage.ENGLISH, "+20 MÁQ · +3 CAR"))
        assertEquals("1H Weapon", CompanionUiText.equipmentType(CompanionLanguage.ENGLISH, "Arma 1 mano"))
    }
}
