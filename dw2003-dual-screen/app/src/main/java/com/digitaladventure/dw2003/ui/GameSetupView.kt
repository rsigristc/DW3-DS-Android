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
    onClose: (() -> Unit)? = null,
    allowDemo: Boolean = onClose == null,
    hasBackup: Boolean = false,
    onRestoreBackup: (() -> Unit)? = null,
    onReturnToStart: (() -> Unit)? = null
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

        content.addView(label(context.getString(R.string.setup_title), 26f, Color.WHITE, true))
        content.addView(label(context.getString(R.string.setup_subtitle), 15f, CYAN, true).apply {
            letterSpacing = .12f
        })
        content.addView(Space(context), LinearLayout.LayoutParams(1, dp(24)))
        content.addView(label("POC NATIVO PARA AYN THOR + GALAXY Z FOLD", 12f, MUTED, true))
        content.addView(label(context.getString(R.string.setup_legal), 13f, Color.rgb(189, 210, 219), false).apply {
            gravity = Gravity.CENTER
            maxWidth = dp(620)
            setPadding(0, dp(14), 0, dp(22))
        })
        content.addView(actionButton(context.getString(R.string.select_rom), onSelectRom))
        if (allowDemo) {
            content.addView(Space(context), LinearLayout.LayoutParams(1, dp(9)))
            content.addView(actionButton(context.getString(R.string.demo_mode), onDemo, outlined = true))
        }
        content.addView(Space(context), LinearLayout.LayoutParams(1, dp(19)))
        content.addView(label("ARCHIVOS DEL USUARIO", 10f, CYAN, true))
        content.addView(label(
            "BIOS: ${if (biosInstalled) "INSTALADO" else "HLE / NO IMPORTADO"}  ·  MEMORY CARD: ${if (hasSave) "128 KiB" else "SIN PARTIDA"}",
            11f,
            MUTED,
            false
        ).apply { setPadding(0, dp(7), 0, dp(10)) })
        content.addView(actionButton(context.getString(R.string.import_bios), onImportBios, outlined = true))
        content.addView(Space(context), LinearLayout.LayoutParams(1, dp(8)))
        content.addView(actionButton(context.getString(R.string.import_save), onImportSave, outlined = true))
        content.addView(Space(context), LinearLayout.LayoutParams(1, dp(8)))
        content.addView(actionButton(context.getString(R.string.export_save), onExportSave, outlined = true, enabled = hasSave))
        content.addView(Space(context), LinearLayout.LayoutParams(1, dp(19)))
        content.addView(label("OPCIONES DE LA APP", 10f, CYAN, true))
        var modsOn = modsEnabled
        val modsButton = actionButton(
            if (modsOn) "Pestaña de mods: activa" else "Pestaña de mods: oculta",
            {},
            outlined = !modsOn
        )
        modsButton.setOnClickListener {
            modsOn = !modsOn
            modsButton.text = if (modsOn) "Pestaña de mods: activa" else "Pestaña de mods: oculta"
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
            "Si la activas, la segunda pantalla muestra una pestaña Mods con códigos PAL opcionales.",
            11f,
            MUTED,
            false
        ).apply { setPadding(0, dp(8), 0, 0) })
        if (onPaneArrangement != null) {
            content.addView(Space(context), LinearLayout.LayoutParams(1, dp(8)))
            content.addView(
                actionButton(
                    "Distribución de pantallas: $paneArrangementLabel",
                    onPaneArrangement,
                    outlined = true
                )
            )
        }
        if (onRestoreBackup != null) {
            content.addView(Space(context), LinearLayout.LayoutParams(1, dp(8)))
            content.addView(actionButton("Restaurar respaldo automático", onRestoreBackup, outlined = true, enabled = hasBackup))
        }
        if (onReturnToStart != null) {
            content.addView(Space(context), LinearLayout.LayoutParams(1, dp(8)))
            content.addView(actionButton("Volver a la pantalla inicial", onReturnToStart, outlined = true))
        }
        if (onClose != null) {
            content.addView(Space(context), LinearLayout.LayoutParams(1, dp(16)))
            content.addView(actionButton("Volver al juego", onClose, outlined = true))
        }
        content.addView(Space(context), LinearLayout.LayoutParams(1, dp(20)))
        content.addView(label("ROM compatibles verificadas: SLES-03936 original y Flawe's Mod 2.0 combinado", 11f, MUTED, false))
        if (!biosInstalled) {
            content.addView(label(
                "Sin BIOS europeo el guardado dentro del juego puede quedarse en «Comprobando la Tarjeta de Memoria». Importa scph5502 o un BIOS PAL de 512 KiB.",
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

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    companion object {
        private val CYAN = Color.rgb(31, 213, 242)
        private val MUTED = Color.rgb(112, 159, 177)
    }
}
