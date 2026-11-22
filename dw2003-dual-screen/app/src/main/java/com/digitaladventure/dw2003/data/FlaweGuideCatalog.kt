package com.digitaladventure.dw2003.data

import org.json.JSONObject

class FlaweGuideCatalog(program: ByteArray, json: String) {
    private val selector = FlaweGuideSelector(program)
    private val objectives = JSONObject(json)

    fun currentKey(main: ByteArray): String? = selector.select(main)

    fun objective(main: ByteArray, language: CompanionLanguage): String? {
        val key = currentKey(main) ?: return null
        val entry = objectives.optJSONObject(key) ?: return null
        return entry.optString(language.guideKey).ifBlank { entry.optString("en") }.takeIf { it.isNotBlank() }
    }
}
