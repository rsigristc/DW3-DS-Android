package com.digitaladventure.dw2003.ui

object PanePolicy {
    const val MIN_DUAL_WIDTH_DP = 600f

    fun shouldShowDashboard(widthPx: Int, density: Float, externalDashboardActive: Boolean): Boolean {
        if (externalDashboardActive || density <= 0f) return false
        return widthPx / density >= MIN_DUAL_WIDTH_DP
    }
}
