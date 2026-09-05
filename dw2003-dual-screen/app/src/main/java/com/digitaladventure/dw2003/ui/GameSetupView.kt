package com.digitaladventure.dw2003.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Space
import android.widget.TextView
import com.digitaladventure.dw2003.R
import com.digitaladventure.dw2003.data.CompanionLanguage

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
    language: CompanionLanguage = CompanionLanguage.SPANISH,
    languageLabel: String = "Automático / Auto",
    onLanguage: (() -> Unit)? = null,
    gameHudLabel: String? = null,
    onGameHud: (() -> Unit)? = null,
    battleScaleLabel: String? = null,
    onBattleScale: (() -> Unit)? = null,
    onClose: (() -> Unit)? = null,
    allowDemo: Boolean = onClose == null,
    onReturnToStart: (() -> Unit)? = null,
    hasCrashLog: Boolean = false,
    onViewCrashLog: (() -> Unit)? = null,
    onCheckUpdate: (() -> Unit)? = null
) : ScrollView(context) {
    private val content = LinearLayout(context)

    init {
        isFillViewport = true
        background = GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(Color.rgb(2, 9, 15), Color.rgb(4, 25, 35), Color.rgb(2, 9, 15))
        )
        content.orientation = LinearLayout.VERTICAL
        content.gravity = Gravity.CENTER
        content.setPadding(dp(28), dp(28), dp(28), dp(28))
        addView(content, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))

        val versionName = installedVersionName()
        content.addView(label(context.getString(R.string.setup_title), 26f, Color.WHITE, true))
        content.addView(label(
            context.getString(R.string.setup_subtitle, versionName),
            15f,
            CYAN,
            true
        ).apply {
            letterSpacing = .12f
        })
        content.addView(Space(context), LinearLayout.LayoutParams(1, dp(24)))
        content.addView(label(
            pick(language, "COMPANION NATIVO PARA AYN THOR + GALAXY Z FOLD", "NATIVE COMPANION FOR AYN THOR + GALAXY Z FOLD"),
            12f,
            MUTED,
            true
        ))
        content.addView(label(
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
        content.addView(actionButton(pick(language, context.getString(R.string.select_rom), "Select BIN"), onSelectRom))
        if (allowDemo) {
            content.addView(Space(context), LinearLayout.LayoutParams(1, dp(9)))
            content.addView(actionButton(pick(language, context.getString(R.string.demo_mode), "Explore interface"), onDemo, outlined = true))
        }
        content.addView(Space(context), LinearLayout.LayoutParams(1, dp(19)))
        content.addView(label(pick(language, "ARCHIVOS DEL USUARIO", "USER FILES"), 10f, CYAN, true))
        content.addView(label(
            "BIOS: ${if (biosInstalled) pick(language, "INSTALADO", "INSTALLED") else "HLE / ${pick(language, "NO IMPORTADO", "NOT IMPORTED")}"}  ·  MEMORY CARD: ${if (hasSave) "128 KiB" else pick(language, "SIN PARTIDA", "NO SAVE")}",
            11f,
            MUTED,
            false
        ).apply { setPadding(0, dp(7), 0, dp(10)) })
        content.addView(actionButton(pick(language, context.getString(R.string.import_bios), "Import European BIOS"), onImportBios, outlined = true))
        content.addView(Space(context), LinearLayout.LayoutParams(1, dp(8)))
        content.addView(actionButton(pick(language, context.getString(R.string.import_save), "Import Memory Card"), onImportSave, outlined = true))
        content.addView(Space(context), LinearLayout.LayoutParams(1, dp(8)))
        content.addView(actionButton(pick(language, context.getString(R.string.export_save), "Export Memory Card"), onExportSave, outlined = true, enabled = hasSave))
        content.addView(Space(context), LinearLayout.LayoutParams(1, dp(19)))
        content.addView(label(pick(language, "OPCIONES DE LA APP", "APP OPTIONS"), 10f, CYAN, true))
        content.addView(label(
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
        val modsButton = actionButton(
            modsLabel(modsOn),
            {},
            outlined = !modsOn
        )
        modsButton.setOnClickListener {
            modsOn = !modsOn
            modsButton.text = modsLabel(modsOn)
            modsButton.background = GradientDrawable().apply {
                cornerRadius = dp(8).toFloat()
                setColor(if (modsOn) CYAN else Color.TRANSPARENT)
                if (!modsOn) setStroke(dp(1), CYAN)
            }
            modsButton.setTextColor(if (modsOn) Color.rgb(2, 16, 22) else CYAN)
            onModsChanged?.invoke(modsOn)
        }
        content.addView(Space(context), LinearLayout.LayoutParams(1, dp(8)))
        content.addView(modsButton)
        content.addView(label(
            pick(
                language,
                "Si la activas, la segunda pantalla muestra una pestaña Mods con códigos PAL opcionales.",
                "When enabled, the second screen shows a Mods tab with optional PAL codes."
            ),
            11f,
            MUTED,
            false
        ).apply { setPadding(0, dp(8), 0, 0) })
        if (onPaneArrangement != null) {
            content.addView(Space(context), LinearLayout.LayoutParams(1, dp(8)))
            content.addView(
                actionButton(
                    "${pick(language, "Distribución de pantallas", "Screen layout")}: $paneArrangementLabel",
                    onPaneArrangement,
                    outlined = true
                )
            )
        }
        if (onLanguage != null) {
            content.addView(Space(context), LinearLayout.LayoutParams(1, dp(8)))
            content.addView(
                actionButton(
                    "${pick(language, "Idioma del panel", "Companion language")}: $languageLabel",
                    onLanguage,
                    outlined = true
                )
            )
            content.addView(label(
                pick(
                    language,
                    "Automático copia el texto de la guía de Flawe en START. Si no hay texto en vivo, pide abrir START; no inventa objetivos por mapa.",
                    "Automatic copies Flawe's START walkthrough. If that text is missing, it asks you to open START and does not invent map objectives."
                ),
                11f,
                MUTED,
                false
            ).apply { setPadding(0, dp(8), 0, 0) })
        }
        if (onGameHud != null && gameHudLabel != null) {
            content.addView(Space(context), LinearLayout.LayoutParams(1, dp(8)))
            content.addView(actionButton(gameHudLabel, onGameHud, outlined = true))
            content.addView(label(
                pick(
                    language,
                    "Oculta o muestra Guardar / Cargar / 2× / Sonido encima del juego. Esos botones viven en el panel complementario.",
                    "Hide or show Save / Load / 2× / Sound over the game. Those buttons live on the companion pane."
                ),
                11f,
                MUTED,
                false
            ).apply { setPadding(0, dp(8), 0, 0) })
        }
        if (onBattleScale != null && battleScaleLabel != null) {
            content.addView(Space(context), LinearLayout.LayoutParams(1, dp(8)))
            content.addView(
                actionButton(
                    "${pick(language, "Resolución en combate", "Battle resolution")}: $battleScaleLabel",
                    onBattleScale,
                    outlined = true
                )
            )
            content.addView(label(
                pick(
                    language,
                    "El render NEON puede doblar la resolución interna (2×) en combate 3D. No hay 4× en este núcleo; fuera de combate vuelve a nativo para no ensuciar el 2D.",
                    "The NEON renderer can double internal resolution (2×) in 3D battles. This core has no 4× mode; it returns to native outside combat so 2D stays clean."
                ),
                11f,
                MUTED,
                false
            ).apply { setPadding(0, dp(8), 0, 0) })
        }
        if (onReturnToStart != null) {
            content.addView(Space(context), LinearLayout.LayoutParams(1, dp(8)))
            content.addView(actionButton(
                pick(language, "Volver a la pantalla inicial", "Return to the start screen"),
                onReturnToStart,
                outlined = true
            ))
        }
        if (onCheckUpdate != null) {
            content.addView(Space(context), LinearLayout.LayoutParams(1, dp(8)))
            content.addView(actionButton(
                pick(language, "Buscar actualización", "Check for update"),
                onCheckUpdate,
                outlined = true
            ))
        }
        if (onViewCrashLog != null) {
            content.addView(Space(context), LinearLayout.LayoutParams(1, dp(8)))
            content.addView(actionButton(
                pick(
                    language,
                    if (hasCrashLog) "Ver último crash" else "Registro de crash (vacío)",
                    if (hasCrashLog) "View last crash" else "Crash log (empty)"
                ),
                onViewCrashLog,
                outlined = true,
                enabled = hasCrashLog
            ))
            content.addView(label(
                pick(
                    language,
                    "Si la app se cierra, guarda un registro con hilo, traza y la última acción del panel.",
                    "If the app crashes, it keeps a log with the thread, stack and the last companion action."
                ),
                11f,
                MUTED,
                false
            ).apply { setPadding(0, dp(8), 0, 0) })
        }
        if (onClose != null) {
            content.addView(Space(context), LinearLayout.LayoutParams(1, dp(16)))
            content.addView(actionButton(pick(language, "Volver al juego", "Return to the game"), onClose, outlined = true))
        }
        content.addView(Space(context), LinearLayout.LayoutParams(1, dp(20)))
        content.addView(label(
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
            content.addView(label(
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
    }

    private fun label(text: String, sp: Float, color: Int, bold: Boolean) = TextView(context).apply {
        this.text = text
        textSize = sp
        setTextColor(color)
        gravity = Gravity.CENTER
        if (bold) setTypeface(typeface, android.graphics.Typeface.BOLD)
    }

    private fun actionButton(text: String, action: () -> Unit, outlined: Boolean = false, enabled: Boolean = true) = Button(context).apply {
        this.text = text
        isAllCaps = true
        isEnabled = enabled
        alpha = if (enabled) 1f else .45f
        setTextColor(if (outlined) CYAN else Color.rgb(2, 16, 22))
        setTypeface(typeface, android.graphics.Typeface.BOLD)
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
        }.getOrNull().orEmpty().ifBlank { "1.0.8" }

    private fun pick(language: CompanionLanguage, spanish: String, english: String) =
        CompanionUiText.pick(language, spanish, english)

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    companion object {
        private val CYAN = Color.rgb(31, 213, 242)
        private val MUTED = Color.rgb(112, 159, 177)
    }
}
