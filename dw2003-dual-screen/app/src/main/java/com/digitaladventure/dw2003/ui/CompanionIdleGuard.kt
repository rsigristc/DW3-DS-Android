package com.digitaladventure.dw2003.ui

enum class CompanionIdleMode {
    OFF,
    DIM,
    PIXEL_SHIFT,
    SLEEP;

    companion object {
        fun fromPreference(value: String?): CompanionIdleMode =
            entries.firstOrNull { it.name == value } ?: OFF
    }
}

enum class CompanionIdleDelay(val millis: Long) {
    S30(30_000L),
    S60(60_000L),
    S120(120_000L);

    companion object {
        fun fromPreference(value: String?): CompanionIdleDelay =
            entries.firstOrNull { it.name == value } ?: S30
    }
}
