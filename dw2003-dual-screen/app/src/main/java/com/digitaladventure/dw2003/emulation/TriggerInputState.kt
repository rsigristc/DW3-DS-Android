package com.digitaladventure.dw2003.emulation

/** Merge key and axis reports: releasing one source must not release another. */
class TriggerInputState {
    enum class Button { L2, R2 }
    data class Change(val button: Button, val pressed: Boolean)
    private val sources = mutableMapOf<String, MutableSet<Button>>()

    fun digital(source: String, button: Button, pressed: Boolean): Change? {
        val wasPressed = sources.values.any { button in it }
        val held = sources.getOrPut(source) { mutableSetOf() }
        if (pressed) held.add(button) else held.remove(button)
        if (held.isEmpty()) sources.remove(source)
        val isPressed = sources.values.any { button in it }
        return if (wasPressed != isPressed) Change(button, isPressed) else null
    }

    fun axes(device: Int, left: Float, right: Float): List<Change> {
        val source = "axes:$device"
        return listOf(Button.L2 to left, Button.R2 to right).mapNotNull { (button, value) ->
            val held = button in sources[source].orEmpty()
            digital(source, button, value.isFinite() && value > if (held) 0.15f else 0.25f)
        }
    }

    fun releaseAll(): List<Change> {
        val changes = sources.values.flatten().toSet().map { Change(it, false) }
        sources.clear()
        return changes
    }
}
