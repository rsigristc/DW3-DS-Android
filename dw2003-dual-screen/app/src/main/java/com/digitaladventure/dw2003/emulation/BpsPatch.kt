package com.digitaladventure.dw2003.emulation

import java.io.IOException
import java.io.RandomAccessFile
import java.io.File

/** BPS1 with bounded working memory. Integrity is checked by the installer. */
object BpsPatch {
    fun apply(patch: ByteArray, source: File, target: File, expectedSize: Long, progress: (Long) -> Unit) {
        fun invalid(): Nothing = throw IOException("BPS inválido / Invalid BPS")
        if (patch.size < 19 || !patch.copyOfRange(0, 4).contentEquals("BPS1".toByteArray())) invalid()
        val end = patch.size - 12
        var cursor = 4
        fun number(): Long {
            var result = 0L
            var shift = 1L
            repeat(8) {
                if (cursor >= end) invalid()
                val value = patch[cursor++].toInt() and 255
                result += (value and 127) * shift
                if (value and 128 != 0) return result
                shift = shift shl 7
                result += shift
            }
            invalid()
        }
        fun delta(): Long = number().let { if (it and 1L != 0L) -(it shr 1) else it shr 1 }
        val sourceSize = number()
        val targetSize = number()
        val metadata = number()
        if (sourceSize != source.length() || targetSize != expectedSize || metadata > end - cursor) invalid()
        cursor += metadata.toInt()
        var position = 0L
        var sourceRelative = 0L
        var targetRelative = 0L
        val buffer = ByteArray(256 * 1024)
        RandomAccessFile(source, "r").use { input ->
            RandomAccessFile(target, "rw").use { output ->
                output.setLength(0)
                while (position < targetSize) {
                    val command = number()
                    val mode = (command and 3).toInt()
                    var remaining = (command shr 2) + 1
                    if (remaining > targetSize - position) invalid()
                    if (mode == 1 && remaining > end - cursor) invalid()
                    if (mode == 2) sourceRelative += delta()
                    if (mode == 3) targetRelative += delta()
                    while (remaining > 0) {
                        val length = minOf(remaining, buffer.size.toLong()).toInt()
                        when (mode) {
                            0, 2 -> {
                                val offset = if (mode == 0) position else sourceRelative
                                if (offset < 0 || offset > sourceSize - length) invalid()
                                input.seek(offset)
                                input.readFully(buffer, 0, length)
                                if (mode == 2) sourceRelative += length
                            }
                            1 -> {
                                patch.copyInto(buffer, 0, cursor, cursor + length)
                                cursor += length
                            }
                            3 -> {
                                if (targetRelative !in 0 until position) invalid()
                                val available = minOf(position - targetRelative, length.toLong()).toInt()
                                output.seek(targetRelative)
                                output.readFully(buffer, 0, available)
                                var filled = available
                                while (filled < length) {
                                    val count = minOf(filled, length - filled)
                                    buffer.copyInto(buffer, filled, 0, count)
                                    filled += count
                                }
                                targetRelative += length
                            }
                        }
                        output.seek(position)
                        output.write(buffer, 0, length)
                        position += length
                        remaining -= length
                        progress(position)
                    }
                }
                if (cursor != end) invalid()
                output.fd.sync()
            }
        }
    }
}
