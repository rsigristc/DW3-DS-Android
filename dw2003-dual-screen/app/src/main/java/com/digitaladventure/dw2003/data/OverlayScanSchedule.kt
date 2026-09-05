package com.digitaladventure.dw2003.data

/** Bounded transition retries; a missing walkthrough never triggers map scans. */
class OverlayScanSchedule {
    private var location: Int? = null
    private var signature: Long? = null
    private var remaining = 0
    private var nextAt = 0L

    fun shouldScan(locationKey: Int, overlaySignature: Long, nowMs: Long): Boolean {
        if (locationKey != location || overlaySignature != signature) {
            location = locationKey
            signature = overlaySignature
            remaining = 3
        }
        if (remaining == 0 || nowMs < nextAt) return false
        remaining--
        nextAt = nowMs + 600
        return true
    }
}
