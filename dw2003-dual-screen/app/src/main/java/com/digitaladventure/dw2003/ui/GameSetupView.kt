package com.digitaladventure.dw2003.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.Typeface
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Space
import android.widget.TextView
import com.digitaladventure.dw2003.R
import com.digitaladventure.dw2003.data.CompanionLanguage
import java.util.Locale

@SuppressLint("ViewConstructor")
class GameSetupView(
    context: Context,
    onSelectRom: () -> Unit,
    onDemo: () -> Unit,
    onImportBios: () -> Unit,
    onImportSave: () -> Unit,
    onExportSave: () -> Unit,
    biosInstalled: Boolean,
    hasSave: Boolean,
    modsEnabled: Boolean = false,
    onModsChanged: ((Boolean) -> Unit)? = null,
    paneArrangementLabel: String = "Automático",
    onPaneArrangement: (() -> Unit)? = null,
    private val language: CompanionLanguage = CompanionLanguage.SPANISH,
    languageLabel: String = "Automático / Auto",
    onLanguage: (() -> Unit)? = null,
    gameHudLabel: String? = null,
    onGameHud: (() -> Unit)? = null,
    imageOptionsLabel: String? = null,
    onImageOptions: (() -> Unit)? = null,
    performanceLabel: String? = null,
    onPerformance: (() -> Unit)? = null,
    idleModeLabel: String? = null,
    onIdleMode: (() -> Unit)? = null,
    idleDelayLabel: String? = null,
    onIdleDelay: (() -> Unit)? = null,
    remoteCompanionLabel: String? = null,
    onRemoteCompanion: (() -> Unit)? = null,
    ramProbeEnabled: Boolean = false,
    onRamProbeChanged: ((Boolean) -> Unit)? = null,
    onClose: (() -> Unit)? = null,
    allowDemo: Boolean = onClose == null,
    onReturnToStart: (() -> Unit)? = null,
    hasCrashLog: Boolean = false,
    onViewCrashLog: (() -> Unit)? = null,
    onCheckUpdate: (() -> Unit)? = null,
    initialTab: Tab = lastSelectedTab
) : LinearLayout(context) {
    enum class Tab { GAME, APP }

    private val page = LinearLayout(context)

    init {
        orientation = VERTICAL
        background = GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(Color.rgb(2, 9, 15), Color.rgb(4, 25, 35), Color.rgb(2, 9, 15))
        )
        setPadding(dp(20), dp(20), dp(20), dp(20))

        val versionName = installedVersionName()
        addView(label(context.getString(R.string.setup_title), 26f, Color.WHITE, true))
        addView(label(
            context.getString(R.string.setup_subtitle, versionName),
            15f,
            CYAN,
            true
        ).apply { letterSpacing = .12f })
        addView(Space(context), LayoutParams(1, dp(14)))

        val tabs = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER
        }
        val gameTab = tabButton(pick(language, "Juego", "Game"))
        val appTab = tabButton(pick(language, "App", "App"))
        tabs.addView(gameTab, LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f).apply { marginEnd = dp(8) })
        tabs.addView(appTab, LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f).apply { marginStart = dp(8) })
        addView(tabs, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
        addView(Space(context), LayoutParams(1, dp(12)))

        page.orientation = VERTICAL
        page.gravity = Gravity.CENTER
        val scroll = ScrollView(context).apply {
            isFillViewport = true
            addView(page, android.widget.FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
        }
        addView(scroll, LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f))

        fun showTab(tab: Tab) {
            lastSelectedTab = tab
            styleTab(gameTab, tab == Tab.GAME)
            styleTab(appTab, tab == Tab.APP)
            page.removeAllViews()
            if (tab == Tab.GAME) {
                fillGameTab(
                    versionName, onSelectRom, onDemo, allowDemo, onImportBios, onImportSave,
                    onExportSave, biosInstalled, hasSave, onReturnToStart
                )
            } else {
                fillAppTab(
                    versionName, modsEnabled, onModsChanged, paneArrangementLabel, onPaneArrangement,
                    languageLabel, onLanguage, gameHudLabel, onGameHud, imageOptionsLabel, onImageOptions,
                    performanceLabel, onPerformance, idleModeLabel, onIdleMode, idleDelayLabel, onIdleDelay,
                    remoteCompanionLabel, onRemoteCompanion, ramProbeEnabled, onRamProbeChanged
                )
            }
        }
        gameTab.setOnClickListener { showTab(Tab.GAME) }
        appTab.setOnClickListener { showTab(Tab.APP) }
        showTab(initialTab)

        addView(Space(context), LayoutParams(1, dp(10)))
        if (onCheckUpdate != null) {
            addView(actionButton(pick(language, "Buscar actualización", "Check for update"), onCheckUpdate, outlined = true))
            addView(Space(context), LayoutParams(1, dp(8)))
        }
        if (onViewCrashLog != null) {
            addView(actionButton(
                pick(
                    language,
                    if (hasCrashLog) "Ver último crash" else "Registro de crash (vacío)",
                    if (hasCrashLog) "View last crash" else "Crash log (empty)"
                ),
                onViewCrashLog,
                outlined = true,
                enabled = hasCrashLog
            ))
            addView(Space(context), LayoutParams(1, dp(8)))
        }
        if (onClose != null) {
            addView(actionButton(pick(language, "Volver al juego", "Return to the game"), onClose, outlined = true))
        }
    }

    private fun fillGameTab(
        versionName: String,
        onSelectRom: () -> Unit,
        onDemo: () -> Unit,
        allowDemo: Boolean,
        onImportBios: () -> Unit,
        onImportSave: () -> Unit,
        onExportSave: () -> Unit,
        biosInstalled: Boolean,
        hasSave: Boolean,
        onReturnToStart: (() -> Unit)?
    ) {
        page.addView(label(
            pick(language, "COMPANION NATIVO PARA AYN THOR + GALAXY Z FOLD", "NATIVE COMPANION FOR AYN THOR + GALAXY Z FOLD"),
            12f,
            MUTED,
            true
        ))
        page.addView(label(
            pick(
                language,
                context.getString(R.string.setup_legal),
                "Select your personal copy of Digimon World 2003 (Europe). The app does not include a ROM, BIOS, or game assets."
            ),
            13f,
            Color.rgb(189, 210, 219),
            false
        ).apply {
            gravity = Gravity.CENTER
            maxWidth = dp(620)
            setPadding(0, dp(14), 0, dp(22))
        })
        page.addView(actionButton(pick(language, context.getString(R.string.select_rom), "Select BIN"), onSelectRom))
        if (allowDemo) {
            page.addView(Space(context), LayoutParams(1, dp(9)))
            page.addView(actionButton(pick(language, context.getString(R.string.demo_mode), "Explore interface"), onDemo, outlined = true))
        }
        page.addView(Space(context), LayoutParams(1, dp(19)))
        page.addView(label(pick(language, "ARCHIVOS DEL USUARIO", "USER FILES"), 10f, CYAN, true))
        page.addView(label(
            "BIOS: ${if (biosInstalled) pick(language, "INSTALADO", "INSTALLED") else "HLE / ${pick(language, "NO IMPORTADO", "NOT IMPORTED")}"}  ·  MEMORY CARD: ${if (hasSave) "128 KiB" else pick(language, "SIN PARTIDA", "NO SAVE")}",
            11f,
            MUTED,
            false
        ).apply { setPadding(0, dp(7), 0, dp(10)) })
        page.addView(actionButton(pick(language, context.getString(R.string.import_bios), "Import European BIOS"), onImportBios, outlined = true))
        page.addView(Space(context), LayoutParams(1, dp(8)))
        page.addView(actionButton(pick(language, context.getString(R.string.import_save), "Import Memory Card"), onImportSave, outlined = true))
        page.addView(Space(context), LayoutParams(1, dp(8)))
        page.addView(actionButton(pick(language, context.getString(R.string.export_save), "Export Memory Card"), onExportSave, outlined = true, enabled = hasSave))
        if (onReturnToStart != null) {
            page.addView(Space(context), LayoutParams(1, dp(16)))
            page.addView(actionButton(
                pick(language, "Volver a la pantalla inicial", "Return to the start screen"),
                onReturnToStart,
                outlined = true
            ))
        }
        page.addView(Space(context), LayoutParams(1, dp(20)))
        page.addView(label(
            pick(
                language,
                "ROM compatibles: SLES-03936, Flawe's Mod 2.0 y SLUS-01436 USA (sin viaje rápido ni guía de Flawe)",
                "Compatible ROMs: SLES-03936, Flawe's Mod 2.0 and SLUS-01436 USA (no Flawe fast travel or walkthrough)"
            ),
            11f,
            MUTED,
            false
        ))
        if (!biosInstalled) {
            page.addView(label(
                pick(
                    language,
                    "Sin BIOS europeo el guardado dentro del juego puede quedarse en «Comprobando la Tarjeta de Memoria». Importa scph5502 o un BIOS PAL de 512 KiB.",
                    "Without a European BIOS, in-game saving can freeze on “Checking Memory Card”. Import scph5502 or a 512 KiB PAL BIOS."
                ),
                11f,
                Color.rgb(244, 181, 61),
                false
            ).apply { setPadding(0, dp(10), 0, 0) })
        }
        page.addView(label(
            pick(language, "Versión $versionName", "Version $versionName"),
            11f,
            MUTED,
            true
        ).apply { setPadding(0, dp(16), 0, 0) })
    }

    private fun fillAppTab(
        versionName: String,
        modsEnabled: Boolean,
        onModsChanged: ((Boolean) -> Unit)?,
        paneArrangementLabel: String,
        onPaneArrangement: (() -> Unit)?,
        languageLabel: String,
        onLanguage: (() -> Unit)?,
        gameHudLabel: String?,
        onGameHud: (() -> Unit)?,
        imageOptionsLabel: String?,
        onImageOptions: (() -> Unit)?,
        performanceLabel: String?,
        onPerformance: (() -> Unit)?,
        idleModeLabel: String?,
        onIdleMode: (() -> Unit)?,
        idleDelayLabel: String?,
        onIdleDelay: (() -> Unit)?,
        remoteCompanionLabel: String?,
        onRemoteCompanion: (() -> Unit)?,
        ramProbeEnabled: Boolean,
        onRamProbeChanged: ((Boolean) -> Unit)?
    ) {
        page.addView(label(pick(language, "OPCIONES DE LA APP", "APP OPTIONS"), 10f, CYAN, true))
        page.addView(label(
            pick(language, "Versión $versionName", "Version $versionName"),
            11f,
            MUTED,
            true
        ).apply { setPadding(0, dp(7), 0, dp(4)) })
        var modsOn = modsEnabled
        fun modsLabel(enabled: Boolean) = if (enabled) {
            pick(language, "Pestaña de mods: activa", "Mods tab: on")
        } else {
            pick(language, "Pestaña de mods: oculta", "Mods tab: hidden")
        }
        val modsButton = actionButton(modsLabel(modsOn), {}, outlined = !modsOn)
        modsButton.setOnClickListener {
            modsOn = !modsOn
            modsButton.text = modsLabel(modsOn)
            styleFilled(modsButton, modsOn)
            onModsChanged?.invoke(modsOn)
        }
        page.addView(Space(context), LayoutParams(1, dp(8)))
        page.addView(modsButton)
        page.addView(hint(
            pick(
                language,
                "Si la activas, la segunda pantalla muestra una pestaña Mods con códigos PAL opcionales.",
                "When enabled, the second screen shows a Mods tab with optional PAL codes."
            )
        ))
        if (onPaneArrangement != null) {
            page.addView(Space(context), LayoutParams(1, dp(8)))
            page.addView(
                actionButton(
                    "${pick(language, "Distribución de pantallas", "Screen layout")}: $paneArrangementLabel",
                    onPaneArrangement,
                    outlined = true
                )
            )
        }
        if (onLanguage != null) {
            page.addView(Space(context), LayoutParams(1, dp(8)))
            page.addView(
                actionButton(
                    "${pick(language, "Idioma del panel", "Companion language")}: $languageLabel",
                    onLanguage,
                    outlined = true
                )
            )
            page.addView(hint(
                pick(
                    language,
                    "Automático copia el texto de la guía de Flawe en START. Si no hay texto en vivo, pide abrir START; no inventa objetivos por mapa.",
                    "Automatic copies Flawe's START walkthrough. If that text is missing, it asks you to open START and does not invent map objectives."
                )
            ))
        }
        if (onGameHud != null && gameHudLabel != null) {
            page.addView(Space(context), LayoutParams(1, dp(8)))
            page.addView(actionButton(gameHudLabel, onGameHud, outlined = true))
            page.addView(hint(
                pick(
                    language,
                    "Oculta o muestra Guardar / Cargar / 2× / Sonido encima del juego. Esos botones viven en el panel complementario.",
                    "Hide or show Save / Load / 2× / Sound over the game. Those buttons live on the companion pane."
                )
            ))
        }
        if (onImageOptions != null && imageOptionsLabel != null) {
            page.addView(Space(context), LayoutParams(1, dp(8)))
            page.addView(
                actionButton(
                    "${pick(language, "Imagen", "Image")}: $imageOptionsLabel",
                    onImageOptions,
                    outlined = true
                )
            )
            page.addView(hint(
                pick(
                    language,
                    "AA+ suaviza 2D y 3D. 2× 3D nítido solo en batalla. Ninguno deja el frame nativo, más ligero en móviles de gama baja.",
                    "AA+ smooths 2D and 3D. 3D 2× is sharp in battle only. None keeps the native frame, lighter on low-end phones."
                )
            ))
        }
        if (onPerformance != null && performanceLabel != null) {
            page.addView(Space(context), LayoutParams(1, dp(8)))
            page.addView(
                actionButton(
                    "${pick(language, "Rendimiento", "Performance")}: $performanceLabel",
                    onPerformance,
                    outlined = true
                )
            )
            page.addView(hint(
                pick(
                    language,
                    "Automático detecta gama baja (poca RAM o modelos como A03) y prioriza fluidez: sin AA+/2×, sin dithering y frameskip auto. Calidad mantiene el look por defecto.",
                    "Automatic detects low-end devices (low RAM or models such as A03) and prefers smoothness: no AA+/2×, no dithering, auto frameskip. Quality keeps the default look."
                )
            ))
        }
        if (onIdleMode != null && idleModeLabel != null) {
            page.addView(Space(context), LayoutParams(1, dp(8)))
            page.addView(
                actionButton(
                    "${pick(language, "Protección OLED", "OLED protection")}: $idleModeLabel",
                    onIdleMode,
                    outlined = true
                )
            )
            page.addView(hint(
                pick(
                    language,
                    "El temporizador solo se reinicia si tocas el panel o cambias de pestaña. Atenuar, desplazar o apagar evitan quemado aunque el mapa cambie un poco.",
                    "The timer resets only if you touch the pane or change tabs. Dim, shift or turn off help prevent burn-in even when the map name ticks."
                )
            ))
        }
        if (onIdleDelay != null && idleDelayLabel != null) {
            page.addView(Space(context), LayoutParams(1, dp(8)))
            page.addView(
                actionButton(
                    "${pick(language, "Espera OLED", "OLED delay")}: $idleDelayLabel",
                    onIdleDelay,
                    outlined = true
                )
            )
        }
        if (onRemoteCompanion != null && remoteCompanionLabel != null) {
            page.addView(Space(context), LayoutParams(1, dp(8)))
            page.addView(
                actionButton(
                    "Pocket Companion: $remoteCompanionLabel",
                    onRemoteCompanion,
                    outlined = true
                )
            )
            page.addView(hint(
                pick(
                    language,
                    "Conecta una AYANEO por Wi-Fi para usar sus controles físicos y mostrar la telemetría del juego.",
                    "Connect an AYANEO over Wi-Fi to use its physical controls and show game telemetry."
                )
            ))
        }
        if (onRamProbeChanged != null) {
            var probeOn = ramProbeEnabled
            fun probeLabel(enabled: Boolean) = if (enabled) {
                pick(language, "Sonda RAM: activa", "RAM probe: on")
            } else {
                pick(language, "Sonda RAM: oculta", "RAM probe: hidden")
            }
            val probeButton = actionButton(probeLabel(probeOn), {}, outlined = !probeOn)
            probeButton.setOnClickListener {
                probeOn = !probeOn
                probeButton.text = probeLabel(probeOn)
                styleFilled(probeButton, probeOn)
                onRamProbeChanged.invoke(probeOn)
            }
            page.addView(Space(context), LayoutParams(1, dp(8)))
            page.addView(probeButton)
            page.addView(hint(
                pick(
                    language,
                    "Añade una pestaña RAM. En menú, mapa y viaje rápido captura el scratch Flawe, el widget (+0xC1 / +0x180 / +0x184) y los dispatchers. En combate sigue 0x80042B1C / 0x800A4460.",
                    "Adds a RAM tab. Menu, map and fast travel capture Flawe scratch, the widget (+0xC1 / +0x180 / +0x184) and dispatchers. Battle still uses 0x80042B1C / 0x800A4460."
                )
            ))
        }
        page.addView(Space(context), LayoutParams(1, dp(12)))
    }

    private fun hint(text: String) = label(text, 11f, MUTED, false).apply {
        setPadding(0, dp(8), 0, 0)
    }

    private fun tabButton(text: String) = actionButton(text, {}, outlined = true)

    private fun styleTab(button: Button, selected: Boolean) {
        styleFilled(button, selected)
    }

    private fun styleFilled(button: Button, filled: Boolean) {
        button.background = GradientDrawable().apply {
            cornerRadius = dp(8).toFloat()
            setColor(if (filled) CYAN else Color.TRANSPARENT)
            if (!filled) setStroke(dp(1), CYAN)
        }
        button.setTextColor(if (filled) Color.rgb(2, 16, 22) else CYAN)
    }

    private fun label(text: String, sp: Float, color: Int, bold: Boolean) = TextView(context).apply {
        this.text = text
        textSize = sp
        setTextColor(color)
        gravity = Gravity.CENTER
        textLocale = if (language == CompanionLanguage.ENGLISH) Locale.ENGLISH else Locale("es", "ES")
        setTypeface(Typeface.DEFAULT, if (bold) Typeface.BOLD else Typeface.NORMAL)
    }

    private fun actionButton(text: String, action: () -> Unit, outlined: Boolean = false, enabled: Boolean = true) = Button(context).apply {
        this.text = text
        isAllCaps = true
        isEnabled = enabled
        alpha = if (enabled) 1f else .45f
        setTextColor(if (outlined) CYAN else Color.rgb(2, 16, 22))
        textLocale = if (language == CompanionLanguage.ENGLISH) Locale.ENGLISH else Locale("es", "ES")
        setTypeface(Typeface.DEFAULT, Typeface.BOLD)
        minWidth = dp(250)
        background = GradientDrawable().apply {
            cornerRadius = dp(8).toFloat()
            setColor(if (outlined) Color.TRANSPARENT else CYAN)
            if (outlined) setStroke(dp(1), CYAN)
        }
        setOnClickListener { action() }
    }

    private fun installedVersionName(): String =
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull().orEmpty().ifBlank { "1.4.0" }

    private fun pick(language: CompanionLanguage, spanish: String, english: String) =
        CompanionUiText.pick(language, spanish, english)

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    companion object {
        private val CYAN = Color.rgb(31, 213, 242)
        private val MUTED = Color.rgb(112, 159, 177)
        var lastSelectedTab: Tab = Tab.GAME
    }
}
