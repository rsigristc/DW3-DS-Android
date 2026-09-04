package com.digitaladventure.dw2003.emulation

import android.content.Context
import android.os.Build
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CrashLogStore(context: Context) {
    private val appContext = context.applicationContext
    private val file = File(appContext.filesDir, FILE_NAME)
    @Volatile
    private var lastAction: String = "idle"

    fun install() {
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, error ->
            runCatching { write(thread, error) }
            previous?.uncaughtException(thread, error)
        }
    }

    fun note(action: String) {
        lastAction = action.trim().ifBlank { "idle" }
    }

    fun hasLog(): Boolean = file.exists() && file.length() > 0

    fun read(): String? = runCatching { file.takeIf { it.exists() }?.readText() }.getOrNull()

    fun clear() {
        runCatching { file.delete() }
    }

    private fun write(thread: Thread, error: Throwable) {
        val stack = StringWriter()
        error.printStackTrace(PrintWriter(stack))
        val stamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
        val version = runCatching {
            appContext.packageManager.getPackageInfo(appContext.packageName, 0).versionName
        }.getOrNull() ?: "unknown"
        file.writeText(
            "DW2003 Dual Screen crash\n" +
                "time=$stamp\n" +
                "version=$version\n" +
                "sdk=${Build.VERSION.SDK_INT}\n" +
                "thread=${thread.name}\n" +
                "lastAction=$lastAction\n\n" +
                stack.toString()
        )
    }

    companion object {
        const val FILE_NAME = "last-crash.log"
    }
}
