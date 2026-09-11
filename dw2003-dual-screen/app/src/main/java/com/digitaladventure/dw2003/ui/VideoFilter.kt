package com.digitaladventure.dw2003.ui

import com.digitaladventure.dw2003.model.GameMode
import com.swordfish.libretrodroid.ShaderConfig

enum class VideoFilter {
    SHARP,
    ANTIALIAS,
    ANTIALIAS_PLUS;

    fun shader(): ShaderConfig = when (this) {
        SHARP -> ShaderConfig.Sharp
        ANTIALIAS, ANTIALIAS_PLUS -> ShaderConfig.CUT3(
            useDynamicBlend = true,
            staticSharpness = 0.38f,
            blendMinSharpness = 0.15f,
            blendMaxSharpness = 0.48f,
            blendMinContrastEdge = 0.0f,
            blendMaxContrastEdge = 0.85f,
            softEdgesSharpening = true,
            softEdgesSharpeningAmount = 0.35f,
            hardEdgesSearchMaxDistance = 6
        )
    }

    /** 2× battle uses Sharp. None (SHARP + no scale) also uses Sharp. */
    @Suppress("UNUSED_PARAMETER")
    fun shaderFor(mode: GameMode, scale: BattleScale): ShaderConfig =
        if (scale != BattleScale.OFF || this == SHARP) ShaderConfig.Sharp else shader()

    companion object {
        fun fromPreference(value: String?): VideoFilter = when (value) {
            SHARP.name -> SHARP
            ANTIALIAS.name, ANTIALIAS_PLUS.name, null -> ANTIALIAS_PLUS
            else -> ANTIALIAS_PLUS
        }
    }
}
