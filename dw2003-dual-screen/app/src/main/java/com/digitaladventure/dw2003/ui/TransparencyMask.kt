package com.digitaladventure.dw2003.ui

import java.util.ArrayDeque

/** Removes only near-white pixels connected to an image edge, preserving white sprite details. */
object TransparencyMask {
    fun clearEdgeConnectedWhite(pixels: IntArray, width: Int, height: Int, threshold: Int = 245): IntArray {
        if (width <= 0 || height <= 0 || pixels.size != width * height) return pixels.copyOf()
        val result = pixels.copyOf()
        val visited = BooleanArray(result.size)
        val queue = ArrayDeque<Int>()
        fun enqueue(index: Int) {
            if (!visited[index] && isNearWhite(result[index], threshold)) {
                visited[index] = true
                queue.add(index)
            }
        }
        repeat(width) { x -> enqueue(x); enqueue((height - 1) * width + x) }
        repeat(height) { y -> enqueue(y * width); enqueue(y * width + width - 1) }
        while (queue.isNotEmpty()) {
            val index = queue.removeFirst()
            result[index] = result[index] and 0x00FFFFFF
            val x = index % width
            val y = index / width
            if (x > 0) enqueue(index - 1)
            if (x + 1 < width) enqueue(index + 1)
            if (y > 0) enqueue(index - width)
            if (y + 1 < height) enqueue(index + width)
        }
        return result
    }

    private fun isNearWhite(color: Int, threshold: Int): Boolean =
        color ushr 24 != 0 && (color shr 16 and 0xFF) >= threshold &&
            (color shr 8 and 0xFF) >= threshold && (color and 0xFF) >= threshold
}
