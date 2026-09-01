package com.digitaladventure.dw2003.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class TransparencyMaskTest {
    @Test fun clearsOnlyWhiteConnectedToAnEdge() {
        val white = 0xFFFFFFFF.toInt()
        val red = 0xFFFF0000.toInt()
        val pixels = intArrayOf(white, white, white, white, red, white, white, white, white)
        val result = TransparencyMask.clearEdgeConnectedWhite(pixels, 3, 3)
        assertEquals(0, result[0] ushr 24)
        assertEquals(255, result[4] ushr 24)
    }

    @Test fun preservesEnclosedWhiteDetail() {
        val black = 0xFF000000.toInt()
        val white = 0xFFFFFFFF.toInt()
        val pixels = IntArray(25) { black }.also { it[12] = white }
        val result = TransparencyMask.clearEdgeConnectedWhite(pixels, 5, 5)
        assertEquals(255, result[12] ushr 24)
    }
}
