package com.digitaladventure.dw2003.ui

enum class PaneArrangement(val label: String) {
    AUTO("Automático"),
    GAME_TOP("Juego arriba · panel abajo"),
    DASHBOARD_TOP("Panel arriba · juego abajo"),
    GAME_LEFT("Juego izquierda · panel derecha"),
    DASHBOARD_LEFT("Panel izquierda · juego derecha");

    companion object {
        fun fromPreference(value: String?): PaneArrangement =
            entries.firstOrNull { it.name == value } ?: AUTO
    }
}
