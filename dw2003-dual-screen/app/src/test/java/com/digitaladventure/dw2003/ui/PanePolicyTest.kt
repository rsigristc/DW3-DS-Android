package com.digitaladventure.dw2003.ui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PanePolicyTest {
    @Test
    fun usesGameOnlyOnCoverAndDualPaneOnUnfoldedWidth() {
        val density = 2.5f
        assertFalse(PanePolicy.shouldShowDashboard(widthPx = 1030, density = density, externalDashboardActive = false))
        assertTrue(PanePolicy.shouldShowDashboard(widthPx = 1812, density = density, externalDashboardActive = false))
        assertFalse(PanePolicy.shouldShowDashboard(widthPx = 1812, density = density, externalDashboardActive = true))
    }
}
