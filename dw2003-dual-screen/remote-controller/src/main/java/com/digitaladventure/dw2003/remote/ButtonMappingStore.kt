package com.digitaladventure.dw2003.remote

import android.content.Context
import android.view.KeyEvent
import com.digitaladventure.dw2003.remote.protocol.RemoteButton

class ButtonMappingStore(context: Context) {
    private val preferences = context.getSharedPreferences("button-mappings", Context.MODE_PRIVATE)

    var activeProfile: ControllerProfile
        get() = preferences.getString(ACTIVE_PROFILE, null)
            ?.let { stored -> ControllerProfile.entries.firstOrNull { it.name == stored } }
            ?: ControllerProfile.XBOX
        set(value) {
            preferences.edit().putString(ACTIVE_PROFILE, value.name).apply()
        }

    fun actionFor(keyCode: Int): RemoteButton? {
        val stored = preferences.getString(mappingKey(activeProfile, keyCode), null)
        if (stored == DISABLED) return null
        return stored?.let { runCatching { RemoteButton.valueOf(it) }.getOrNull() }
            ?: defaultsFor(activeProfile)[keyCode]
    }

    fun setAction(keyCode: Int, action: RemoteButton?) {
        preferences.edit().putString(mappingKey(activeProfile, keyCode), action?.name ?: DISABLED).apply()
    }

    fun resetActiveProfile() {
        val prefix = "mapping.${activeProfile.name}."
        preferences.edit().also { editor ->
            preferences.all.keys.filter { it.startsWith(prefix) }.forEach(editor::remove)
        }.apply()
    }

    companion object {
        const val DISABLED = "DISABLED"
        private const val ACTIVE_PROFILE = "active-profile"

        val CONFIGURABLE_KEYS = (listOf(
            KeyEvent.KEYCODE_DPAD_UP,
            KeyEvent.KEYCODE_DPAD_DOWN,
            KeyEvent.KEYCODE_DPAD_LEFT,
            KeyEvent.KEYCODE_DPAD_RIGHT,
            KeyEvent.KEYCODE_BUTTON_A,
            KeyEvent.KEYCODE_BUTTON_B,
            KeyEvent.KEYCODE_BUTTON_X,
            KeyEvent.KEYCODE_BUTTON_Y,
            KeyEvent.KEYCODE_BUTTON_L1,
            KeyEvent.KEYCODE_BUTTON_R1,
            KeyEvent.KEYCODE_BUTTON_L2,
            KeyEvent.KEYCODE_BUTTON_R2,
            KeyEvent.KEYCODE_BUTTON_START,
            KeyEvent.KEYCODE_BUTTON_SELECT,
            KeyEvent.KEYCODE_BUTTON_THUMBL,
            KeyEvent.KEYCODE_BUTTON_THUMBR
        ) + (KeyEvent.KEYCODE_BUTTON_1..KeyEvent.KEYCODE_BUTTON_16)).distinct()

        private val STANDARD_DEFAULTS = mapOf(
            KeyEvent.KEYCODE_DPAD_UP to RemoteButton.DPAD_UP,
            KeyEvent.KEYCODE_DPAD_DOWN to RemoteButton.DPAD_DOWN,
            KeyEvent.KEYCODE_DPAD_LEFT to RemoteButton.DPAD_LEFT,
            KeyEvent.KEYCODE_DPAD_RIGHT to RemoteButton.DPAD_RIGHT,
            KeyEvent.KEYCODE_BUTTON_A to RemoteButton.CROSS,
            KeyEvent.KEYCODE_BUTTON_B to RemoteButton.CIRCLE,
            KeyEvent.KEYCODE_BUTTON_X to RemoteButton.SQUARE,
            KeyEvent.KEYCODE_BUTTON_Y to RemoteButton.TRIANGLE,
            KeyEvent.KEYCODE_BUTTON_L1 to RemoteButton.L1,
            KeyEvent.KEYCODE_BUTTON_R1 to RemoteButton.R1,
            KeyEvent.KEYCODE_BUTTON_L2 to RemoteButton.L2,
            KeyEvent.KEYCODE_BUTTON_R2 to RemoteButton.R2,
            KeyEvent.KEYCODE_BUTTON_START to RemoteButton.START,
            KeyEvent.KEYCODE_BUTTON_SELECT to RemoteButton.SELECT,
            KeyEvent.KEYCODE_BUTTON_THUMBL to RemoteButton.LEFT_STICK,
            KeyEvent.KEYCODE_BUTTON_THUMBR to RemoteButton.RIGHT_STICK
        )

        private val GENERIC_DEFAULTS = STANDARD_DEFAULTS + mapOf(
            KeyEvent.KEYCODE_BUTTON_1 to RemoteButton.CROSS,
            KeyEvent.KEYCODE_BUTTON_2 to RemoteButton.CIRCLE,
            KeyEvent.KEYCODE_BUTTON_3 to RemoteButton.SQUARE,
            KeyEvent.KEYCODE_BUTTON_4 to RemoteButton.TRIANGLE,
            KeyEvent.KEYCODE_BUTTON_5 to RemoteButton.L1,
            KeyEvent.KEYCODE_BUTTON_6 to RemoteButton.R1,
            KeyEvent.KEYCODE_BUTTON_7 to RemoteButton.L2,
            KeyEvent.KEYCODE_BUTTON_8 to RemoteButton.R2,
            KeyEvent.KEYCODE_BUTTON_9 to RemoteButton.SELECT,
            KeyEvent.KEYCODE_BUTTON_10 to RemoteButton.START,
            KeyEvent.KEYCODE_BUTTON_11 to RemoteButton.LEFT_STICK,
            KeyEvent.KEYCODE_BUTTON_12 to RemoteButton.RIGHT_STICK
        )

        fun defaultsFor(profile: ControllerProfile): Map<Int, RemoteButton> = when (profile) {
            ControllerProfile.XBOX,
            ControllerProfile.PLAYSTATION -> STANDARD_DEFAULTS
            ControllerProfile.GENERIC -> GENERIC_DEFAULTS
        }

        private fun mappingKey(profile: ControllerProfile, keyCode: Int) =
            "mapping.${profile.name}.$keyCode"
    }
}

enum class ControllerProfile(val displayName: String, val description: String) {
    XBOX("Xbox / AYANEO", "A/B/X/Y por posición física"),
    PLAYSTATION("PlayStation / PSX", "Cruz/Círculo/Cuadrado/Triángulo"),
    GENERIC("Gamepad genérico", "Botones 1–12 y controles Android estándar")
}
