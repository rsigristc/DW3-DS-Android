package com.digitaladventure.dw2003.remote

import android.view.KeyEvent
import com.digitaladventure.dw2003.remote.protocol.RemoteButton
import com.digitaladventure.dw2003.remote.protocol.RemoteInputFrame

class RemoteInputApplier(
    private val sendKey: (action: Int, keyCode: Int) -> Unit,
    private val sendMotion: (source: Int, x: Float, y: Float) -> Unit
) {
    private var sessionId = Long.MIN_VALUE
    private var buttons = 0
    private var leftX = 0f
    private var leftY = 0f
    private var rightX = 0f
    private var rightY = 0f

    fun apply(frame: RemoteInputFrame) {
        if (sessionId != frame.sessionId) {
            releaseAll()
            sessionId = frame.sessionId
        }

        BUTTON_KEYS.forEach { (button, keyCode) ->
            val wasPressed = buttons and button.mask != 0
            val isPressed = frame.buttons and button.mask != 0
            if (wasPressed != isPressed) {
                sendKey(if (isPressed) KeyEvent.ACTION_DOWN else KeyEvent.ACTION_UP, keyCode)
            }
        }
        buttons = frame.buttons

        if (frame.leftX != leftX || frame.leftY != leftY) {
            leftX = frame.leftX
            leftY = frame.leftY
            sendMotion(MOTION_SOURCE_ANALOG_LEFT, leftX, leftY)
        }
        if (frame.rightX != rightX || frame.rightY != rightY) {
            rightX = frame.rightX
            rightY = frame.rightY
            sendMotion(MOTION_SOURCE_ANALOG_RIGHT, rightX, rightY)
        }
    }

    fun releaseAll() {
        BUTTON_KEYS.forEach { (button, keyCode) ->
            if (buttons and button.mask != 0) sendKey(KeyEvent.ACTION_UP, keyCode)
        }
        buttons = 0
        if (leftX != 0f || leftY != 0f) sendMotion(MOTION_SOURCE_ANALOG_LEFT, 0f, 0f)
        if (rightX != 0f || rightY != 0f) sendMotion(MOTION_SOURCE_ANALOG_RIGHT, 0f, 0f)
        leftX = 0f
        leftY = 0f
        rightX = 0f
        rightY = 0f
    }

    companion object {
        const val MOTION_SOURCE_ANALOG_LEFT = 1
        const val MOTION_SOURCE_ANALOG_RIGHT = 2

        private val BUTTON_KEYS = listOf(
            RemoteButton.DPAD_UP to KeyEvent.KEYCODE_DPAD_UP,
            RemoteButton.DPAD_DOWN to KeyEvent.KEYCODE_DPAD_DOWN,
            RemoteButton.DPAD_LEFT to KeyEvent.KEYCODE_DPAD_LEFT,
            RemoteButton.DPAD_RIGHT to KeyEvent.KEYCODE_DPAD_RIGHT,
            RemoteButton.CROSS to KeyEvent.KEYCODE_BUTTON_B,
            RemoteButton.CIRCLE to KeyEvent.KEYCODE_BUTTON_A,
            RemoteButton.SQUARE to KeyEvent.KEYCODE_BUTTON_Y,
            RemoteButton.TRIANGLE to KeyEvent.KEYCODE_BUTTON_X,
            RemoteButton.L1 to KeyEvent.KEYCODE_BUTTON_L1,
            RemoteButton.R1 to KeyEvent.KEYCODE_BUTTON_R1,
            RemoteButton.L2 to KeyEvent.KEYCODE_BUTTON_L2,
            RemoteButton.R2 to KeyEvent.KEYCODE_BUTTON_R2,
            RemoteButton.START to KeyEvent.KEYCODE_BUTTON_START,
            RemoteButton.SELECT to KeyEvent.KEYCODE_BUTTON_SELECT,
            RemoteButton.LEFT_STICK to KeyEvent.KEYCODE_BUTTON_THUMBL,
            RemoteButton.RIGHT_STICK to KeyEvent.KEYCODE_BUTTON_THUMBR
        )
    }
}
