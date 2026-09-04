package com.digitaladventure.dw2003.data

import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

class CustomCheatStore(private val preferences: SharedPreferences) {
    fun all(): List<CheatSpec> = decode(preferences.getString(KEY, "[]"))

    fun add(label: String, code: String): CheatSpec? {
        val normalized = CheatCodeParser.normalize(code) ?: return null
        val trimmed = label.trim().ifBlank { "Mod personalizado" }
        val current = all().toMutableList()
        val nextIndex = (current.mapNotNull { it.id.removePrefix("custom_").toIntOrNull() }.maxOrNull() ?: 0) + 1
        val spec = CheatSpec(
            id = "custom_$nextIndex",
            label = trimmed,
            detail = "Código PAL añadido por el usuario.",
            code = normalized
        )
        current += spec
        persist(current)
        return spec
    }

    fun remove(id: String) {
        persist(all().filterNot { it.id == id })
    }

    fun byId(id: String): CheatSpec? = all().firstOrNull { it.id == id }

    private fun persist(items: List<CheatSpec>) {
        val array = JSONArray()
        items.forEach { spec ->
            array.put(
                JSONObject()
                    .put("id", spec.id)
                    .put("label", spec.label)
                    .put("detail", spec.detail)
                    .put("code", spec.code)
                    .put("battleOnly", spec.battleOnly)
            )
        }
        preferences.edit().putString(KEY, array.toString()).apply()
    }

    private fun decode(raw: String?): List<CheatSpec> {
        val array = runCatching { JSONArray(raw ?: "[]") }.getOrElse { JSONArray() }
        return buildList {
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                val id = item.optString("id")
                val code = CheatCodeParser.normalize(item.optString("code")).orEmpty()
                if (id.isBlank() || code.isBlank()) continue
                add(
                    CheatSpec(
                        id = id,
                        label = item.optString("label").ifBlank { id },
                        detail = item.optString("detail").ifBlank { "Código PAL añadido por el usuario." },
                        code = code,
                        battleOnly = item.optBoolean("battleOnly")
                    )
                )
            }
        }
    }

    companion object {
        const val KEY = "custom_cheats_json"
    }
}
