package com.digitaladventure.dw2003.model

data class RamCaptureInfo(
    val stamp: String,
    val hasScreenshot: Boolean,
    val hasState: Boolean,
    val bytes: Long
)

data class RamChange(
    val address: Int,
    val previous: Int,
    val current: Int
)

data class RamProbe(
    val overlaySignature: Long,
    val hookWord: Long = overlaySignature,
    val slotWord: Long = 0L,
    val inBattle: Boolean,
    val scene: String = "",
    val setupSummary: String = "",
    val travelSummary: String = "",
    val setupHex: String,
    val arenaHex: String,
    val travelHex: String = "",
    val changes: List<RamChange> = emptyList(),
    val captures: List<RamCaptureInfo> = emptyList()
) {
    val overlayLabel: String
        get() = "0x${overlaySignature.toString(16).uppercase()}"
}
