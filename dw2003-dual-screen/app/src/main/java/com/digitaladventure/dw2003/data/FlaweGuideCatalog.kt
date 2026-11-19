package com.digitaladventure.dw2003.data

import org.json.JSONObject

class FlaweGuideCatalog(program: ByteArray, json: String) {
    private val selector = FlaweGuideSelector(program)
    private val objectives = JSONObject(json)

    fun currentKey(main: ByteArray): String? = selector.select(main)

    fun objective(main: ByteArray, languageCode: Int): String? {
        val key = currentKey(main) ?: return null
        val entry = objectives.optJSONObject(key) ?: return null
        val language = when (languageCode) {
            PalLanguage.SPANISH -> "es"
            PalLanguage.FRENCH -> "fr"
            PalLanguage.GERMAN -> "de"
            PalLanguage.ITALIAN -> "it"
            else -> "en"
        }
        return entry.optString(language).ifBlank { entry.optString("en") }.takeIf { it.isNotBlank() }
    }
}
