package com.digitaladventure.dw2003.remote

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.digitaladventure.dw2003.remote.protocol.RemoteButton
import com.digitaladventure.dw2003.remote.protocol.RemoteCommand
import com.digitaladventure.dw2003.remote.protocol.RemoteTelemetryFrame
import kotlin.math.abs

class RemoteControllerActivity : ComponentActivity() {
    private lateinit var mappingStore: ButtonMappingStore
    private lateinit var client: RemoteControllerClient
    private lateinit var hostInput: EditText
    private lateinit var codeInput: EditText
    private lateinit var connectionForm: LinearLayout
    private lateinit var connectedBar: LinearLayout
    private lateinit var disconnectedStatus: TextView
    private lateinit var connectedStatus: TextView
    private lateinit var configureButton: Button
    private lateinit var hudBar: LinearLayout
    private lateinit var loadButton: Button
    private lateinit var speedButton: Button
    private lateinit var soundButton: Button
    private lateinit var imageButton: Button
    private lateinit var dashboard: RemoteDashboardView
    private val pressedKeys = mutableSetOf<Int>()
    private var axisButtons = 0
    private var leftX = 0f
    private var leftY = 0f
    private var rightX = 0f
    private var rightY = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        mappingStore = ButtonMappingStore(this)
        client = RemoteControllerClient(
            onTelemetry = { frame -> runOnUiThread { render(frame) } },
            onConnectionChanged = { connected -> runOnUiThread { setConnected(connected) } }
        )
        setContentView(buildContent())
        setConnected(false)
    }

    override fun onPause() {
        pressedKeys.clear()
        axisButtons = 0
        client.sendNeutral()
        super.onPause()
    }

    override fun onDestroy() {
        client.stop()
        super.onDestroy()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode !in ButtonMappingStore.CONFIGURABLE_KEYS) return super.onKeyDown(keyCode, event)
        if (event.repeatCount == 0 && pressedKeys.add(keyCode)) sendControllerState()
        return true
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode !in ButtonMappingStore.CONFIGURABLE_KEYS) return super.onKeyUp(keyCode, event)
        if (pressedKeys.remove(keyCode)) sendControllerState()
        return true
    }

    override fun dispatchGenericMotionEvent(event: MotionEvent): Boolean {
        val joystick = event.source and InputDevice.SOURCE_JOYSTICK == InputDevice.SOURCE_JOYSTICK
        if (!joystick || event.action != MotionEvent.ACTION_MOVE) return super.dispatchGenericMotionEvent(event)
        leftX = deadzone(event.getAxisValue(MotionEvent.AXIS_X))
        leftY = deadzone(event.getAxisValue(MotionEvent.AXIS_Y))
        rightX = deadzone(event.getAxisValue(MotionEvent.AXIS_Z))
        rightY = deadzone(event.getAxisValue(MotionEvent.AXIS_RZ))
        val hatX = event.getAxisValue(MotionEvent.AXIS_HAT_X)
        val hatY = event.getAxisValue(MotionEvent.AXIS_HAT_Y)
        axisButtons = 0
        if (hatX < -.5f) addAxisButton(KeyEvent.KEYCODE_DPAD_LEFT)
        if (hatX > .5f) addAxisButton(KeyEvent.KEYCODE_DPAD_RIGHT)
        if (hatY < -.5f) addAxisButton(KeyEvent.KEYCODE_DPAD_UP)
        if (hatY > .5f) addAxisButton(KeyEvent.KEYCODE_DPAD_DOWN)
        if (event.getAxisValue(MotionEvent.AXIS_LTRIGGER) > .25f) addAxisButton(KeyEvent.KEYCODE_BUTTON_L2)
        if (event.getAxisValue(MotionEvent.AXIS_RTRIGGER) > .25f) addAxisButton(KeyEvent.KEYCODE_BUTTON_R2)
        sendControllerState()
        return true
    }

    private fun addAxisButton(keyCode: Int) {
        axisButtons = axisButtons or (mappingStore.actionFor(keyCode)?.mask ?: 0)
    }

    private fun sendControllerState() {
        val mappedButtons = pressedKeys.fold(axisButtons) { buttons, keyCode ->
            buttons or (mappingStore.actionFor(keyCode)?.mask ?: 0)
        }
        client.update(RemoteControllerClient.ControllerState(mappedButtons, leftX, leftY, rightX, rightY))
    }

    private fun buildContent(): View {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(10), dp(14), dp(8))
            setBackgroundColor(BACKGROUND)
        }
        connectionForm = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(8), dp(5), dp(8), dp(5))
            background = panelBackground()
        }
        disconnectedStatus = label("SIN CONEXIÓN", 11f, MUTED, true)
        hostInput = input("IP DEL FOLD", getPreferences(MODE_PRIVATE).getString("host", "") ?: "")
        codeInput = input("CÓDIGO", getPreferences(MODE_PRIVATE).getString("code", "") ?: "").apply {
            inputType = InputType.TYPE_CLASS_NUMBER
        }
        val connectButton = actionButton("CONECTAR") { connect() }
        connectionForm.addView(disconnectedStatus, LinearLayout.LayoutParams(dp(118), dp(48)))
        connectionForm.addView(hostInput, LinearLayout.LayoutParams(0, dp(48), 2f).apply { marginEnd = dp(7) })
        connectionForm.addView(codeInput, LinearLayout.LayoutParams(0, dp(48), 1f).apply { marginEnd = dp(7) })
        connectionForm.addView(connectButton, LinearLayout.LayoutParams(dp(132), dp(43)))

        connectedBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(10), dp(5), dp(8), dp(5))
            background = panelBackground()
        }
        connectedStatus = label("● CONECTADO", 12f, CYAN, true)
        val disconnect = actionButton("DESCONECTAR") { client.stop() }
        configureButton = actionButton(profileButtonText()) { showControllerSettings() }
        connectedBar.addView(connectedStatus, LinearLayout.LayoutParams(0, dp(46), 1f))
        connectedBar.addView(disconnect, LinearLayout.LayoutParams(dp(150), dp(42)).apply { marginEnd = dp(7) })
        connectedBar.addView(configureButton, LinearLayout.LayoutParams(dp(190), dp(42)))

        hudBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(3), 0, dp(3))
        }
        val saveButton = actionButton("GUARDAR") { client.sendCommand(RemoteCommand.SAVE_STATE) }
        loadButton = actionButton("CARGAR") { client.sendCommand(RemoteCommand.LOAD_STATE) }
        speedButton = actionButton("VELOCIDAD 1×") { client.sendCommand(RemoteCommand.TOGGLE_SPEED) }
        soundButton = actionButton("SONIDO ON") { client.sendCommand(RemoteCommand.TOGGLE_MUTE) }
        imageButton = actionButton("IMAGEN AA+") { client.sendCommand(RemoteCommand.CYCLE_IMAGE) }
        listOf(saveButton, loadButton, speedButton, soundButton, imageButton).forEachIndexed { index, button ->
            hudBar.addView(button, LinearLayout.LayoutParams(0, dp(42), 1f).apply { if (index > 0) marginStart = dp(6) })
        }

        dashboard = RemoteDashboardView(this).apply {
            onCommand = { command, argument -> client.sendCommand(command, argument) }
            onAddCustomMod = ::showAddCustomMod
        }
        root.addView(connectionForm, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(58)))
        root.addView(connectedBar, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(58)))
        root.addView(hudBar, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(48)))
        root.addView(dashboard, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))
        return root
    }

    private fun setConnected(connected: Boolean) {
        if (!::connectionForm.isInitialized) return
        connectionForm.visibility = if (connected) View.GONE else View.VISIBLE
        connectedBar.visibility = if (connected) View.VISIBLE else View.GONE
        hudBar.visibility = if (connected) View.VISIBLE else View.GONE
        if (connected) connectedStatus.text = "● CONECTADO  ·  ${mappingStore.activeProfile.displayName.uppercase()}"
        else disconnectedStatus.text = "○ SIN CONEXIÓN"
    }

    private fun render(frame: RemoteTelemetryFrame) {
        dashboard.render(frame)
        loadButton.isEnabled = frame.stateAvailable
        loadButton.alpha = if (frame.stateAvailable) 1f else .45f
        speedButton.text = if (frame.fastForward) "VELOCIDAD 2×" else "VELOCIDAD 1×"
        soundButton.text = if (frame.muted) "SONIDO OFF" else "SONIDO ON"
        imageButton.text = "IMAGEN"
    }

    private fun connect() {
        val host = hostInput.text.toString().trim()
        val code = codeInput.text.toString().toIntOrNull()
        if (host.isBlank() || code == null || code !in 100000..999999) {
            Toast.makeText(this, "Escribe la IP y el código de 6 dígitos", Toast.LENGTH_SHORT).show()
            return
        }
        getPreferences(MODE_PRIVATE).edit().putString("host", host).putString("code", code.toString()).apply()
        disconnectedStatus.text = "◌ CONECTANDO…"
        runCatching { client.connect(host, code) }
            .onFailure {
                disconnectedStatus.text = "○ SIN CONEXIÓN"
                Toast.makeText(this, "No se pudo conectar: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun showAddCustomMod() {
        val name = input("NOMBRE DEL MOD", "")
        val code = input("800XXXXX YYYY", "").apply {
            isSingleLine = false
            minLines = 3
            gravity = Gravity.TOP
            setPadding(dp(12), dp(10), dp(12), dp(10))
        }
        val body = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(10), dp(20), 0)
            addView(name, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(48)))
            addView(code, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(94)).apply { topMargin = dp(8) })
        }
        AlertDialog.Builder(this)
            .setTitle("Añadir mod personalizado")
            .setView(body)
            .setPositiveButton("ENVIAR AL FOLD") { _, _ ->
                val label = name.text.toString().trim()
                val value = code.text.toString().trim()
                if (value.isBlank()) {
                    Toast.makeText(this, "Escribe un código PAL", Toast.LENGTH_SHORT).show()
                } else {
                    client.sendCommand(RemoteCommand.ADD_CUSTOM_MOD, text = label, detail = value)
                }
            }
            .setNegativeButton("CANCELAR", null)
            .show()
    }

    private fun showControllerSettings() {
        val options = arrayOf(
            "PERFIL · ${mappingStore.activeProfile.displayName}",
            "CONFIGURAR CADA BOTÓN",
            "RESTAURAR ESTE PERFIL"
        )
        AlertDialog.Builder(this)
            .setTitle("Controles")
            .setItems(options) { _, index ->
                when (index) {
                    0 -> chooseProfile()
                    1 -> chooseButtonToConfigure()
                    2 -> confirmResetProfile()
                }
            }
            .setNegativeButton("Cerrar", null)
            .show()
    }

    private fun chooseProfile() {
        val profiles = ControllerProfile.entries
        AlertDialog.Builder(this)
            .setTitle("Perfil de gamepad")
            .setSingleChoiceItems(profiles.map { "${it.displayName}\n${it.description}" }.toTypedArray(), mappingStore.activeProfile.ordinal) { dialog, index ->
                mappingStore.activeProfile = profiles[index]
                pressedKeys.clear()
                axisButtons = 0
                sendControllerState()
                configureButton.text = profileButtonText()
                connectedStatus.text = "● CONECTADO  ·  ${mappingStore.activeProfile.displayName.uppercase()}"
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun chooseButtonToConfigure() {
        val keys = if (mappingStore.activeProfile == ControllerProfile.GENERIC) ButtonMappingStore.CONFIGURABLE_KEYS
        else ButtonMappingStore.CONFIGURABLE_KEYS.take(16)
        val labels = keys.map { key ->
            val action = mappingStore.actionFor(key)?.name?.replace('_', ' ') ?: "DESHABILITADO"
            "${friendlyKeyName(key)}  →  $action"
        }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("${mappingStore.activeProfile.displayName} · botón físico")
            .setItems(labels) { _, index -> chooseAction(keys[index]) }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun chooseAction(keyCode: Int) {
        val actions = listOf<RemoteButton?>(null) + RemoteButton.entries
        val labels = actions.map { it?.name?.replace('_', ' ') ?: "DESHABILITADO" }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle(friendlyKeyName(keyCode))
            .setItems(labels) { _, index ->
                mappingStore.setAction(keyCode, actions[index])
                pressedKeys.clear()
                sendControllerState()
                Toast.makeText(this, "Asignación guardada en ${mappingStore.activeProfile.displayName}", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun confirmResetProfile() {
        AlertDialog.Builder(this)
            .setTitle("Restaurar ${mappingStore.activeProfile.displayName}")
            .setMessage("Se eliminarán solamente las asignaciones personalizadas de este perfil.")
            .setPositiveButton("Restaurar") { _, _ ->
                mappingStore.resetActiveProfile()
                pressedKeys.clear()
                sendControllerState()
                Toast.makeText(this, "Perfil restaurado", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun friendlyKeyName(keyCode: Int): String = when (keyCode) {
        KeyEvent.KEYCODE_BUTTON_A -> "A / Cruz"
        KeyEvent.KEYCODE_BUTTON_B -> "B / Círculo"
        KeyEvent.KEYCODE_BUTTON_X -> "X / Cuadrado"
        KeyEvent.KEYCODE_BUTTON_Y -> "Y / Triángulo"
        KeyEvent.KEYCODE_BUTTON_START -> "Start / Options"
        KeyEvent.KEYCODE_BUTTON_SELECT -> "Select / Share"
        KeyEvent.KEYCODE_BUTTON_THUMBL -> "Stick izquierdo"
        KeyEvent.KEYCODE_BUTTON_THUMBR -> "Stick derecho"
        else -> KeyEvent.keyCodeToString(keyCode).removePrefix("KEYCODE_").replace('_', ' ')
    }

    private fun input(hintValue: String, value: String) = EditText(this).apply {
        hint = hintValue
        setText(value)
        textSize = 14f
        setTextColor(WHITE)
        setHintTextColor(MUTED)
        isSingleLine = true
        setPadding(dp(12), 0, dp(12), 0)
        background = fieldBackground()
    }

    private fun label(value: String, size: Float, color: Int, bold: Boolean) = TextView(this).apply {
        text = value
        textSize = size
        setTextColor(color)
        gravity = Gravity.CENTER_VERTICAL
        typeface = Typeface.create(Typeface.DEFAULT, if (bold) Typeface.BOLD else Typeface.NORMAL)
    }

    private fun actionButton(value: String, action: () -> Unit) = Button(this).apply {
        text = value
        textSize = 11f
        setTextColor(WHITE)
        typeface = Typeface.DEFAULT_BOLD
        isAllCaps = false
        background = buttonBackground()
        setOnClickListener { action() }
    }

    private fun panelBackground() = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = dp(7).toFloat()
        setColor(PANEL)
        setStroke(dp(1), CYAN_DARK)
    }

    private fun fieldBackground() = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = dp(6).toFloat()
        setColor(PANEL_INNER)
        setStroke(dp(1), CYAN_DARK)
    }

    private fun buttonBackground() = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = dp(6).toFloat()
        setColor(Color.rgb(8, 72, 91))
        setStroke(dp(1), CYAN)
    }

    private fun profileButtonText() = "BOTONES · ${mappingStore.activeProfile.displayName.substringBefore(" /").uppercase()}"

    private fun deadzone(value: Float): Float {
        if (abs(value) < .08f) return 0f
        return ((abs(value) - .08f) / .92f).coerceIn(0f, 1f) * if (value < 0f) -1f else 1f
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    companion object {
        private val BACKGROUND = Color.rgb(2, 10, 16)
        private val PANEL = Color.rgb(4, 24, 33)
        private val PANEL_INNER = Color.rgb(6, 35, 46)
        private val CYAN = Color.rgb(31, 213, 242)
        private val CYAN_DARK = Color.rgb(10, 91, 112)
        private val WHITE = Color.rgb(228, 246, 250)
        private val MUTED = Color.rgb(111, 158, 175)
    }
}
