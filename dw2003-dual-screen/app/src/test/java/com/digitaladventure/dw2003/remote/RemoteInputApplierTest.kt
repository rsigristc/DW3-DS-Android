package com.digitaladventure.dw2003.remote

import android.view.KeyEvent
import com.digitaladventure.dw2003.remote.protocol.RemoteButton
import com.digitaladventure.dw2003.remote.protocol.RemoteInputFrame
import org.junit.Assert.assertEquals
import org.junit.Test

class RemoteInputApplierTest {
    @Test
    fun sendsOnlyButtonDeltasAndReleasesOnSessionChange() {
        val keys = mutableListOf<Pair<Int, Int>>()
        val applier = RemoteInputApplier(
            sendKey = { action, keyCode -> keys += action to keyCode },
            sendMotion = { _, _, _ -> }
        )

        applier.apply(frame(1, 1, RemoteButton.CROSS.mask))
        applier.apply(frame(1, 2, RemoteButton.CROSS.mask))
        applier.apply(frame(2, 1, 0))

        assertEquals(
            listOf(
                KeyEvent.ACTION_DOWN to KeyEvent.KEYCODE_BUTTON_B,
                KeyEvent.ACTION_UP to KeyEvent.KEYCODE_BUTTON_B
            ),
            keys
        )
    }

    private fun frame(session: Long, sequence: Int, buttons: Int) = RemoteInputFrame(
        123456, session, sequence, buttons, 0f, 0f, 0f, 0f, 0L
    )
}
