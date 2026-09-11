package com.digitaladventure.dw2003.ui

import android.app.ActivityManager
import android.content.Context
import android.os.Build

object DevicePerformance {
    private const val LOW_RAM_BYTES = 3_600L * 1024L * 1024L

    fun isLowEnd(context: Context): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager ?: return false
        if (manager.isLowRamDevice) return true
        val info = ActivityManager.MemoryInfo()
        manager.getMemoryInfo(info)
        if (info.totalMem in 1 until LOW_RAM_BYTES) return true
        if (manager.memoryClass in 1 until 192) return true
        val model = Build.MODEL.uppercase()
        val device = Build.DEVICE.uppercase()
        return LOW_END_MARKERS.any { marker -> marker in model || marker in device }
    }

    private val LOW_END_MARKERS = listOf(
        "A03", "A04", "A05", "A13", "A14", "A15",
        "SM-A032", "SM-A035", "SM-A037", "SM-A045", "SM-A047",
        "SM-A055", "SM-A135", "SM-A145", "SM-A155"
    )
}

enum class PerformanceMode {
    AUTO,
    QUALITY;

    companion object {
        fun fromPreference(value: String?): PerformanceMode =
            entries.firstOrNull { it.name == value } ?: AUTO
    }
}
