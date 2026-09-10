package com.digitaladventure.dw2003.emulation

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.digitaladventure.dw2003.model.RamCaptureInfo
import com.digitaladventure.dw2003.model.RamChange
import com.digitaladventure.dw2003.model.RamProbe
import com.swordfish.libretrodroid.GLRetroView
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RamCaptureStore(
    private val context: Context,
    private val directory: File,
    @Suppress("unused") private val view: GLRetroView
) {
    private val clock = SimpleDateFormat("HHmmss-SSS", Locale.US)
    private var lastCaptureAt = 0L

    @Volatile
    var latest: List<RamCaptureInfo> = emptyList()
        private set

    init {
        directory.mkdirs()
        publicDir()?.mkdirs()
        latest = listCaptures()
    }

    fun maybeCapture(probe: RamProbe, changes: List<RamChange>, force: Boolean = false) {
        if (!force && changes.isEmpty()) return
        val now = System.currentTimeMillis()
        if (!force && now - lastCaptureAt < DEBOUNCE_MS) return
        lastCaptureAt = now
        val stamp = clock.format(Date(now))
        val text = File(directory, "$stamp.txt")
        text.writeText(buildString {
            appendLine("time=$stamp")
            appendLine("overlay=${probe.overlayLabel} hook=${probe.hookWord.toString(16)} slot=${probe.slotWord.toString(16)}")
            appendLine("battle=${probe.inBattle}")
            appendLine("scene=${probe.scene}")
            appendLine("setup=${probe.setupSummary}")
            if (probe.travelSummary.isNotBlank()) appendLine("travel=${probe.travelSummary}")
            appendLine("CHANGES")
            if (changes.isEmpty()) appendLine("(scene)")
            changes.forEach { change ->
                appendLine("0x${change.address.toString(16).uppercase()} ${change.previous} -> ${change.current}")
            }
            if (probe.travelHex.isNotBlank()) {
                appendLine("TRAVEL")
                appendLine(probe.travelHex)
            }
            appendLine("SETUP")
            appendLine(probe.setupHex)
            appendLine("ARENA")
            appendLine(probe.arenaHex)
        })
        exportPublic(text, "text/plain")
        prune()
        latest = listCaptures()
    }

    private fun exportPublic(file: File, mime: String) {
        val copied = publicDir()?.let { dir ->
            runCatching { file.copyTo(File(dir, file.name), overwrite = true) }.isSuccess
        } ?: false
        if (copied || Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, file.name)
            put(MediaStore.MediaColumns.MIME_TYPE, mime)
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/DDW3")
        }
        val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values) ?: return
        runCatching {
            context.contentResolver.openOutputStream(uri)?.use { output ->
                file.inputStream().use { input -> input.copyTo(output) }
            }
        }
    }

    private fun publicDir(): File? {
        val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "DDW3")
        return if (dir.exists() || dir.mkdirs()) dir else null
    }

    private fun prune() {
        val files = directory.listFiles().orEmpty().sortedByDescending { it.lastModified() }
        val stamps = files.map { it.name.substringBefore('.') }.distinct()
        stamps.drop(KEEP).forEach { stamp ->
            files.filter { it.name.startsWith(stamp) }.forEach { it.delete() }
        }
    }

    private fun listCaptures(): List<RamCaptureInfo> =
        directory.listFiles().orEmpty()
            .filter { it.extension == "txt" }
            .sortedByDescending { it.lastModified() }
            .take(KEEP)
            .map { file ->
                RamCaptureInfo(
                    stamp = file.nameWithoutExtension,
                    hasScreenshot = false,
                    hasState = false,
                    bytes = file.length()
                )
            }

    companion object {
        private const val DEBOUNCE_MS = 1500L
        private const val KEEP = 16
    }
}
