package com.digitaladventure.dw2003.ui

import android.app.Presentation
import android.content.Context
import android.os.Bundle
import android.view.Display
import android.view.WindowManager
import com.digitaladventure.dw2003.model.GameSnapshot

class CompanionPresentation(
    outerContext: Context,
    display: Display,
    actions: DashboardActions
) : Presentation(outerContext, display) {
    private val dashboard = DigiviceDashboardView(context, actions)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
        setContentView(dashboard)
    }

    fun submitSnapshot(snapshot: GameSnapshot) = dashboard.submitSnapshot(snapshot)
    fun setControlsVisible(visible: Boolean) { dashboard.controlsVisible = visible }
    fun setModsEnabled(enabled: Boolean) { dashboard.modsEnabled = enabled }
    fun setEnabledCheats(ids: Set<String>) { dashboard.enabledCheats = ids }
    fun setVisitedMaps(ids: Set<Int>) { dashboard.visitedMaps = ids }
}
