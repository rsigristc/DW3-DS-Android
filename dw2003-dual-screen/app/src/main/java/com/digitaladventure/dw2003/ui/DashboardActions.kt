package com.digitaladventure.dw2003.ui

data class DashboardActions(
    val onAppSettings: () -> Unit = {},
    val onToggleControls: () -> Unit = {},
    val onToggleGameHud: () -> Unit = {},
    val onQuickAction: (QuickAction) -> Unit = {},
    val onFastTravel: (Int) -> Unit = {},
    val onOpenGameMap: () -> Unit = {},
    val onPartyMove: (fromIndex: Int, toIndex: Int) -> Unit = { _, _ -> },
    val onCheatToggle: (id: String, enabled: Boolean) -> Unit = { _, _ -> },
    val onAddCustomCheat: () -> Unit = {},
    val onRemoveCustomCheat: (String) -> Unit = {}
)
