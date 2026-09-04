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
        assertEquals("+14 STR · +15 CHA", CompanionUiText.equipmentStats(CompanionLanguage.ENGLISH, "+14 FUE · +15 CAR"))
        assertEquals("+20 FIRE", CompanionUiText.equipmentStats(CompanionLanguage.ENGLISH, "+20 FUEGO"))
        assertEquals("1H Weapon", CompanionUiText.equipmentType(CompanionLanguage.ENGLISH, "Arma 1 mano"))
    }
}
