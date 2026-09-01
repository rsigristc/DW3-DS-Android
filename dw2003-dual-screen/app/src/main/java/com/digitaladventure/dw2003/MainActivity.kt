package com.digitaladventure.dw2003

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.util.Consumer
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.window.java.layout.WindowInfoTrackerCallbackAdapter
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import com.digitaladventure.dw2003.data.AreaCatalog
import com.digitaladventure.dw2003.data.CheatCatalog
import com.digitaladventure.dw2003.data.GameStateRepository
import com.digitaladventure.dw2003.model.GameMode
import com.digitaladventure.dw2003.emulation.BiosManager
import com.digitaladventure.dw2003.emulation.FastTravelNavigator
import com.digitaladventure.dw2003.emulation.GameMemoryController
import com.digitaladventure.dw2003.emulation.MemoryPoller
import com.digitaladventure.dw2003.emulation.PadStep
import com.digitaladventure.dw2003.emulation.RetroPadButton
import com.digitaladventure.dw2003.emulation.RomVerifier
import com.digitaladventure.dw2003.emulation.SaveManager
import com.digitaladventure.dw2003.emulation.QuickStateManager
import com.digitaladventure.dw2003.ui.AdaptiveDualPaneLayout
import com.digitaladventure.dw2003.ui.CompanionPresentation
import com.digitaladventure.dw2003.ui.DashboardActions
import com.digitaladventure.dw2003.ui.DigiviceDashboardView
import com.digitaladventure.dw2003.ui.GamePlaceholderView
import com.digitaladventure.dw2003.ui.GameSetupView
import com.digitaladventure.dw2003.ui.QuickAction
import com.digitaladventure.dw2003.ui.VirtualControllerView
import com.swordfish.libretrodroid.GLRetroView
import com.swordfish.libretrodroid.GLRetroViewData
import com.swordfish.libretrodroid.ShaderConfig
import com.swordfish.libretrodroid.Variable
import com.swordfish.libretrodroid.VirtualFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity(), DisplayManager.DisplayListener {
    private val repository = GameStateRepository()
    private lateinit var displayManager: DisplayManager
    private lateinit var saveManager: SaveManager
    private lateinit var biosManager: BiosManager
    private lateinit var windowInfoAdapter: WindowInfoTrackerCallbackAdapter
    private var windowInfoConsumer: Consumer<androidx.window.layout.WindowLayoutInfo>? = null

    private var dualLayout: AdaptiveDualPaneLayout? = null
    private var localDashboard: DigiviceDashboardView? = null
    private var presentation: CompanionPresentation? = null
    private var retroView: GLRetroView? = null
    private var virtualController: VirtualControllerView? = null
    private var quickStateManager: QuickStateManager? = null
    private var memoryPoller: MemoryPoller? = null
    private var memoryController: GameMemoryController? = null
    private var emulatorEventsJob: Job? = null
    private var settingsDialog: Dialog? = null
    private var dualContentActive = false
    private var skipNextAutoSave = false
    private var fastForward = false
    private var muted = false
    private var modsEnabled = false
    private var enabledCheats = linkedSetOf<String>()
    private var visitedMaps = linkedSetOf<Int>()
    private var wasOnSaveScreen = false
    private var travelJob: Job? = null

    private val openRom = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let(::acceptRom)
    }

    private val openBios = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let(::acceptBios)
    }

    private val openMemoryCard = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let(::acceptMemoryCard)
    }

    private val createMemoryCard = registerForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
        uri?.let(::exportMemoryCard)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

        displayManager = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        saveManager = SaveManager(this)
        biosManager = BiosManager(this)
        muted = getPreferences(MODE_PRIVATE).getBoolean(PREF_MUTED, false)
        modsEnabled = getPreferences(MODE_PRIVATE).getBoolean(PREF_MODS_ENABLED, false)
        enabledCheats = getPreferences(MODE_PRIVATE).getString(PREF_ENABLED_CHEATS, "")
            ?.split(',')
            ?.filter { it.isNotBlank() }
            ?.toCollection(LinkedHashSet())
            ?: linkedSetOf()
        visitedMaps = getPreferences(MODE_PRIVATE).getString(PREF_VISITED_MAPS, "")
            ?.split(',')
            ?.mapNotNull { it.toIntOrNull(16) }
            ?.toCollection(LinkedHashSet())
            ?: linkedSetOf()
        windowInfoAdapter = WindowInfoTrackerCallbackAdapter(WindowInfoTracker.getOrCreate(this))
        displayManager.registerDisplayListener(this, null)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (dualContentActive) {
                    showAppMenu()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                    isEnabled = true
                }
            }
        })

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                repository.snapshot.collectLatest { snapshot ->
                    rememberVisited(snapshot.areaId, snapshot.mapId)
                    persistMemoryCardAfterSave(snapshot.areaId, snapshot.mapId)
                    localDashboard?.submitSnapshot(snapshot)
                    presentation?.submitSnapshot(snapshot)
                    syncDashboardExtras()
                }
            }
        }

        val stored = getPreferences(MODE_PRIVATE).getString(PREF_ROM_URI, null)
        if (stored != null) bootRom(Uri.parse(stored)) else showSetup()
    }

    override fun onStart() {
        super.onStart()
        val consumer = Consumer<androidx.window.layout.WindowLayoutInfo> { layout ->
            val feature = layout.displayFeatures.filterIsInstance<FoldingFeature>().firstOrNull()
            dualLayout?.setFold(feature)
        }
        windowInfoConsumer = consumer
        windowInfoAdapter.addWindowLayoutInfoListener(this, ContextCompat.getMainExecutor(this), consumer)
        updatePresentation()
    }

    override fun onStop() {
        windowInfoConsumer?.let(windowInfoAdapter::removeWindowLayoutInfoListener)
        windowInfoConsumer = null
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        applyAudioAndSpeed()
    }

    override fun onPause() {
        if (!skipNextAutoSave) persistMemoryCardIfSafe()
        skipNextAutoSave = false
        super.onPause()
    }

    override fun onDestroy() {
        memoryPoller?.stop()
        emulatorEventsJob?.cancel()
        travelJob?.cancel()
        settingsDialog?.dismiss()
        presentation?.dismiss()
        displayManager.unregisterDisplayListener(this)
        super.onDestroy()
    }

    private fun showSetup() {
        dualContentActive = false
        presentation?.dismiss()
        presentation = null
        setContentView(createSetupView())
    }

    private fun showDemo() {
        repository.showDemo()
        attachDualContent(GamePlaceholderView(this))
    }

    private fun acceptBios(uri: Uri) {
        Toast.makeText(this, "Importando BIOS…", Toast.LENGTH_SHORT).show()
        lifecycleScope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) { biosManager.importBios(contentResolver, uri) }
            }.getOrElse {
                Toast.makeText(this@MainActivity, "BIOS rechazado: ${it.message}", Toast.LENGTH_LONG).show()
                return@launch
            }
            Toast.makeText(this@MainActivity, "BIOS europeo instalado · SHA-1 ${result.sha1.take(10)}…", Toast.LENGTH_LONG).show()
            recreate()
        }
    }

    private fun acceptMemoryCard(uri: Uri) {
        retroView?.let(saveManager::save)
        Toast.makeText(this, "Importando Memory Card…", Toast.LENGTH_SHORT).show()
        lifecycleScope.launch {
            val size = runCatching {
                withContext(Dispatchers.IO) { saveManager.importCard(contentResolver, uri) }
            }.getOrElse {
                Toast.makeText(this@MainActivity, "Memory Card rechazada: ${it.message}", Toast.LENGTH_LONG).show()
                return@launch
            }
            skipNextAutoSave = true
            Toast.makeText(this@MainActivity, "Memory Card importada (${size / 1024} KiB)", Toast.LENGTH_LONG).show()
            recreate()
        }
    }

    private fun requestMemoryCardExport() {
        retroView?.let(saveManager::save)
        if (!saveManager.hasSave) {
            Toast.makeText(this, "Todavía no existe una Memory Card para exportar", Toast.LENGTH_LONG).show()
            return
        }
        createMemoryCard.launch("DW2003-memory-card.srm")
    }

    private fun exportMemoryCard(uri: Uri) {
        lifecycleScope.launch {
            val size = runCatching {
                withContext(Dispatchers.IO) { saveManager.exportCard(contentResolver, uri) }
            }.getOrElse {
                Toast.makeText(this@MainActivity, "No se pudo exportar: ${it.message}", Toast.LENGTH_LONG).show()
                return@launch
            }
            Toast.makeText(this@MainActivity, "Memory Card exportada (${size / 1024} KiB)", Toast.LENGTH_LONG).show()
        }
    }

    private fun showAppMenu() = showAppSettings()

    private fun createSetupView(onClose: (() -> Unit)? = null) = GameSetupView(
        this,
        onSelectRom = { openRom.launch(arrayOf("application/octet-stream", "*/*")) },
        onDemo = { showDemo() },
        onImportBios = { openBios.launch(arrayOf("application/octet-stream", "*/*")) },
        onImportSave = { openMemoryCard.launch(arrayOf("application/octet-stream", "*/*")) },
        onExportSave = { requestMemoryCardExport() },
        biosInstalled = biosManager.isInstalled,
        hasSave = saveManager.hasSave,
        modsEnabled = modsEnabled,
        onModsChanged = ::setModsEnabled,
        onClose = onClose,
        hasBackup = saveManager.hasBackup,
        onRestoreBackup = if (onClose != null) ::restoreAutomaticBackup else null,
        onReturnToStart = if (onClose != null) ::returnToStartScreen else null
    )

    private fun showAppSettings() {
        if (isFinishing || isDestroyed) return
        if (!dualContentActive) {
            showSetup()
            return
        }
        settingsDialog?.dismiss()
        val dialog = Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.setContentView(createSetupView { dialog.dismiss() })
        dialog.setOnDismissListener { settingsDialog = null }
        settingsDialog = dialog
        dialog.show()
    }

    private fun acceptRom(uri: Uri) {
        runCatching {
            contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        Toast.makeText(this, "Verificando la imagen de disco…", Toast.LENGTH_LONG).show()
        lifecycleScope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) { RomVerifier.verify(contentResolver, uri) }
            }.getOrElse {
                Toast.makeText(this@MainActivity, "No se pudo leer el BIN: ${it.message}", Toast.LENGTH_LONG).show()
                return@launch
            }
            if (result.variant == RomVerifier.Variant.UNKNOWN) {
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("ROM no verificada")
                    .setMessage("SHA-1: ${result.sha1}\n\nLa lectura de RAM fue diseñada para SLES-03936 original y Flawe's Mod 2.0. Puedes continuar, pero los datos de la segunda pantalla podrían ser incorrectos.")
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Continuar") { _, _ -> rememberAndRestart(uri, result) }
                    .show()
            } else {
                Toast.makeText(this@MainActivity, "Detectada: ${result.variant.label}", Toast.LENGTH_LONG).show()
                rememberAndRestart(uri, result)
            }
        }
    }

    private fun rememberAndRestart(uri: Uri, result: RomVerifier.Result) {
        getPreferences(MODE_PRIVATE).edit()
            .putString(PREF_ROM_URI, uri.toString())
            .putString(PREF_ROM_NAME, displayName(uri))
            .putString(PREF_ROM_VARIANT, result.variant.label)
            .putString(PREF_ROM_SHA1, result.sha1)
            .apply()
        recreate()
    }

    private fun bootRom(uri: Uri) {
        val descriptor = runCatching { contentResolver.openFileDescriptor(uri, "r") }.getOrNull()
        if (descriptor == null) {
            getPreferences(MODE_PRIVATE).edit().remove(PREF_ROM_URI).apply()
            Toast.makeText(this, "No se conserva acceso al BIN. Selecciónalo de nuevo.", Toast.LENGTH_LONG).show()
            showSetup()
            return
        }

        val data = GLRetroViewData(this).apply {
            coreFilePath = "${applicationInfo.nativeLibraryDir}/libretro.so"
            gameVirtualFiles = listOf(VirtualFile(displayName(uri), descriptor))
            systemDirectory = biosManager.systemDirectory.absolutePath
            savesDirectory = getDir("core-saves", MODE_PRIVATE).absolutePath
            saveRAMState = saveManager.load()
            shader = ShaderConfig.Sharp
            preferLowLatencyAudio = true
            skipDuplicateFrames = false
            variables = arrayOf(
                Variable("pcsx_rearmed_region", "PAL"),
                Variable("pcsx_rearmed_bios", "auto"),
                Variable("pcsx_rearmed_memcard1", "libretro"),
                Variable("pcsx_rearmed_memcard2", "none"),
                Variable("pcsx_rearmed_drc", "enabled"),
                Variable("pcsx_rearmed_drc_thread", "auto"),
                Variable("pcsx_rearmed_gpu_thread_rendering", "disabled"),
                Variable("pcsx_rearmed_spu_thread", "disabled"),
                Variable("pcsx_rearmed_frameskip_type", "disabled"),
                Variable("pcsx_rearmed_dithering", "enabled")
            )
        }
        val view = GLRetroView(this, data).apply {
            isFocusable = true
            isFocusableInTouchMode = true
            requestFocus()
        }
        retroView = view
        memoryController = GameMemoryController(view)
        if (enabledCheats.isNotEmpty()) applyEnabledCheats()
        if (!biosManager.isInstalled) {
            Toast.makeText(
                this,
                "BIOS HLE activo: el guardado dentro del juego puede congelarse. Importa un BIOS europeo desde ⚙ APP.",
                Toast.LENGTH_LONG
            ).show()
        }
        view.frameSpeed = if (fastForward) 2 else 1
        view.audioEnabled = !muted
        lifecycle.addObserver(view)
        view.applyRuntimeOptions()
        val romKey = getPreferences(MODE_PRIVATE).getString(PREF_ROM_SHA1, null) ?: uri.toString()
        val states = QuickStateManager(this, romKey)
        quickStateManager = states
        val controller = VirtualControllerView(
            this,
            keySink = { action, keyCode -> view.sendKeyEvent(action, keyCode, 0) },
            quickActionSink = ::handleQuickAction
        ).apply {
            gamepadVisible = virtualControlsVisible()
            muted = this@MainActivity.muted
            fastForward = this@MainActivity.fastForward
            stateAvailable = states.hasState || states.hasLegacyState
        }
        virtualController = controller
        val gameSurface = FrameLayout(this).apply {
            addView(view, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
            addView(controller, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
        }
        attachDualContent(gameSurface)

        memoryPoller = MemoryPoller(view, repository, lifecycleScope)
        emulatorEventsJob?.cancel()
        emulatorEventsJob = lifecycleScope.launch {
            launch {
                view.getGLRetroEvents().collect { event ->
                    if (event is GLRetroView.GLRetroEvents.FrameRendered) memoryPoller?.start()
                }
            }
            launch {
                view.getGLRetroErrors().collect { code ->
                    Toast.makeText(this@MainActivity, "Error del núcleo de emulación ($code)", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun handleQuickAction(action: QuickAction) {
        val view = retroView ?: return
        when (action) {
            QuickAction.SAVE_STATE -> lifecycleScope.launch {
                val snapshot = repository.snapshot.value
                if (snapshot.areaId == 0x0C01 || snapshot.mapId == 0x0C01) {
                    Toast.makeText(this@MainActivity, "Espera a que termine el guardado del juego antes de crear un estado rápido", Toast.LENGTH_LONG).show()
                    return@launch
                }
                Toast.makeText(this@MainActivity, "Guardando estado rápido…", Toast.LENGTH_SHORT).show()
                val result = runCatching {
                    withContext(Dispatchers.IO) {
                        val saved = quickStateManager?.save(view) ?: error("Gestor no disponible")
                        saveManager.persistSnapshot(saved.memoryCard)
                        saved
                    }
                }
                result.onSuccess { saved ->
                    virtualController?.stateAvailable = true
                    Toast.makeText(this@MainActivity, "Estado + Memory Card guardados (${saved.stateBytes / 1024} KiB)", Toast.LENGTH_SHORT).show()
                }.onFailure {
                    Toast.makeText(this@MainActivity, "No se pudo guardar: ${it.message}", Toast.LENGTH_LONG).show()
                }
            }
            QuickAction.LOAD_STATE -> lifecycleScope.launch {
                Toast.makeText(this@MainActivity, "Cargando estado rápido…", Toast.LENGTH_SHORT).show()
                val result = runCatching {
                    withContext(Dispatchers.IO) {
                        quickStateManager?.load(view)?.also { saveManager.persistSnapshot(it.memoryCard) }
                    }
                }
                val loaded = result.getOrNull()
                if (loaded != null) {
                    Toast.makeText(this@MainActivity, "Estado y Memory Card cargados", Toast.LENGTH_SHORT).show()
                } else {
                    val message = result.exceptionOrNull()?.message
                        ?: "No hay un estado compatible o no pudo cargarse"
                    Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
                }
            }
            QuickAction.TOGGLE_SPEED -> {
                fastForward = !fastForward
                view.frameSpeed = if (fastForward) 2 else 1
                view.applyRuntimeOptions()
                virtualController?.fastForward = fastForward
                Toast.makeText(this, if (fastForward) "Velocidad 2×" else "Velocidad normal", Toast.LENGTH_SHORT).show()
            }
            QuickAction.TOGGLE_MUTE -> {
                muted = !muted
                view.audioEnabled = !muted
                view.applyRuntimeOptions()
                getPreferences(MODE_PRIVATE).edit().putBoolean(PREF_MUTED, muted).apply()
                virtualController?.muted = muted
                Toast.makeText(this, if (muted) "Sonido desactivado" else "Sonido activado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun attachDualContent(gameView: View) {
        val layout = AdaptiveDualPaneLayout(this)
        val dashboard = DigiviceDashboardView(this, dashboardActions()).apply {
            controlsVisible = virtualControlsVisible()
        }
        dashboard.submitSnapshot(repository.snapshot.value)
        syncDashboardExtras()
        layout.addView(gameView)
        layout.addView(dashboard)
        dualLayout = layout
        localDashboard = dashboard
        dualContentActive = true
        setContentView(layout)
        updatePresentation()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        val view = retroView
        if (view != null && keyCode in GAME_KEYS) {
            view.sendKeyEvent(KeyEvent.ACTION_DOWN, mapPhysicalButton(keyCode), 0)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        val view = retroView
        if (view != null && keyCode in GAME_KEYS) {
            view.sendKeyEvent(KeyEvent.ACTION_UP, mapPhysicalButton(keyCode), 0)
            return true
        }
        return super.onKeyUp(keyCode, event)
    }

    override fun dispatchGenericMotionEvent(event: MotionEvent): Boolean {
        val view = retroView
        if (view != null && event.source and InputDevice.SOURCE_JOYSTICK == InputDevice.SOURCE_JOYSTICK && event.action == MotionEvent.ACTION_MOVE) {
            view.sendMotionEvent(0, event.getAxisValue(MotionEvent.AXIS_HAT_X), event.getAxisValue(MotionEvent.AXIS_HAT_Y), 0)
            view.sendMotionEvent(1, event.getAxisValue(MotionEvent.AXIS_X), event.getAxisValue(MotionEvent.AXIS_Y), 0)
            view.sendMotionEvent(2, event.getAxisValue(MotionEvent.AXIS_Z), event.getAxisValue(MotionEvent.AXIS_RZ), 0)
            return true
        }
        return super.dispatchGenericMotionEvent(event)
    }

    private fun updatePresentation() {
        if (!dualContentActive) return
        val activityDisplayId = window.decorView.display?.displayId
        val secondary = displayManager.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION)
            .firstOrNull { it.displayId != activityDisplayId }
        if (secondary == null) {
            presentation?.dismiss()
            presentation = null
            dualLayout?.setGameOnly(false)
            return
        }
        if (presentation?.display?.displayId == secondary.displayId) return
        presentation?.dismiss()
        presentation = CompanionPresentation(this, secondary, dashboardActions()).also {
            it.setOnDismissListener {
                presentation = null
                dualLayout?.setGameOnly(false)
            }
            it.show()
            it.submitSnapshot(repository.snapshot.value)
            it.setControlsVisible(virtualControlsVisible())
            syncDashboardExtras()
        }
        dualLayout?.setGameOnly(true)
    }

    override fun onDisplayAdded(displayId: Int) = updatePresentation()
    override fun onDisplayRemoved(displayId: Int) = updatePresentation()
    override fun onDisplayChanged(displayId: Int) = Unit

    private fun displayName(uri: Uri): String {
        contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) return cursor.getString(0).let { if (it.endsWith(".bin", true)) it else "$it.bin" }
        }
        return "Digimon World 2003.bin"
    }

    private fun mapPhysicalButton(keyCode: Int) = when (keyCode) {
        KeyEvent.KEYCODE_BUTTON_A -> KeyEvent.KEYCODE_BUTTON_B
        KeyEvent.KEYCODE_BUTTON_B -> KeyEvent.KEYCODE_BUTTON_A
        KeyEvent.KEYCODE_BUTTON_X -> KeyEvent.KEYCODE_BUTTON_Y
        KeyEvent.KEYCODE_BUTTON_Y -> KeyEvent.KEYCODE_BUTTON_X
        else -> keyCode
    }

    private fun virtualControlsVisible(): Boolean =
        getPreferences(MODE_PRIVATE).getBoolean(PREF_VIRTUAL_GAMEPAD, true)

    private fun dashboardActions() = DashboardActions(
        onAppSettings = ::showAppSettings,
        onToggleControls = ::toggleVirtualControls,
        onFastTravel = ::requestFastTravel,
        onOpenGameMap = ::openInGameMap,
        onPartyMove = ::movePartyMember,
        onCheatToggle = ::toggleCheat
    )

    private fun applyAudioAndSpeed() {
        val view = retroView ?: return
        view.audioEnabled = !muted
        view.frameSpeed = if (fastForward) 2 else 1
        view.applyRuntimeOptions()
        virtualController?.muted = muted
        virtualController?.fastForward = fastForward
    }

    private fun syncDashboardExtras() {
        localDashboard?.modsEnabled = modsEnabled
        localDashboard?.enabledCheats = enabledCheats
        localDashboard?.visitedMaps = visitedMaps
        presentation?.setModsEnabled(modsEnabled)
        presentation?.setEnabledCheats(enabledCheats)
        presentation?.setVisitedMaps(visitedMaps)
    }

    private fun restoreAutomaticBackup() {
        if (saveManager.restoreBackup()) {
            skipNextAutoSave = true
            Toast.makeText(this, "Respaldo restaurado", Toast.LENGTH_LONG).show()
            recreate()
        }
    }

    private fun returnToStartScreen() {
        getPreferences(MODE_PRIVATE).edit()
            .remove(PREF_ROM_URI)
            .remove(PREF_ROM_NAME)
            .remove(PREF_ROM_VARIANT)
            .remove(PREF_ROM_SHA1)
            .apply()
        recreate()
    }

    private fun setModsEnabled(enabled: Boolean) {
        modsEnabled = enabled
        getPreferences(MODE_PRIVATE).edit().putBoolean(PREF_MODS_ENABLED, enabled).apply()
        if (!enabled) {
            enabledCheats.clear()
            persistEnabledCheats()
            memoryController?.applyCheats(emptyList())
        }
        syncDashboardExtras()
        Toast.makeText(this, if (enabled) "Pestaña Mods visible" else "Pestaña Mods oculta", Toast.LENGTH_SHORT).show()
    }

    private fun rememberVisited(areaId: Int, mapId: Int) {
        var changed = false
        listOf(areaId, mapId).forEach { id ->
            if (AreaCatalog.isField(id) && visitedMaps.add(id)) changed = true
        }
        if (changed) {
            getPreferences(MODE_PRIVATE).edit()
                .putString(PREF_VISITED_MAPS, visitedMaps.joinToString(",") { AreaCatalog.hex(it) })
                .apply()
        }
    }

    private fun persistMemoryCardIfSafe() {
        val snapshot = repository.snapshot.value
        if (snapshot.areaId == 0x0C01 || snapshot.mapId == 0x0C01) return
        retroView?.let(saveManager::save)
    }

    private fun persistMemoryCardAfterSave(areaId: Int, mapId: Int) {
        val onSave = areaId == 0x0C01 || mapId == 0x0C01
        if (wasOnSaveScreen && !onSave) persistMemoryCardIfSafe()
        wasOnSaveScreen = onSave
    }

    private fun requestFastTravel(areaId: Int) {
        val snapshot = repository.snapshot.value
        if (!snapshot.canFastTravel) {
            Toast.makeText(this, "Viaje rápido bloqueado durante batalla o eventos", Toast.LENGTH_LONG).show()
            return
        }
        val controller = memoryController
        if (controller == null || retroView == null) {
            Toast.makeText(this, "El viaje rápido requiere una partida en emulación", Toast.LENGTH_LONG).show()
            return
        }
        runTravelSequence {
            openMapTab()
            val origin = controller.readAreaMap().first
            runCatching { controller.requestFastTravel(areaId) }
                .onFailure {
                    Toast.makeText(this@MainActivity, "No se pudo escribir el destino: ${it.message}", Toast.LENGTH_LONG).show()
                    return@runTravelSequence
                }
            delay(280)
            playPadSteps(FastTravelNavigator.confirmMapDestination())
            waitUntil(2500) {
                val now = memoryController?.readAreaMap()?.first ?: return@waitUntil false
                now != origin && !menuIsOpen()
            }
            if (menuIsOpen()) {
                playPadSteps(FastTravelNavigator.closeMenu())
                waitUntil(900) { !menuIsOpen() }
            }
            Toast.makeText(
                this@MainActivity,
                "Destino ${AreaCatalog.name(areaId)} confirmado en la pestaña Mapa. Flawe carga al salir, en el spawn del icono.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun openInGameMap() {
        val snapshot = repository.snapshot.value
        if (!snapshot.gameStarted) {
            Toast.makeText(this, "Inicia una partida para abrir el mapa", Toast.LENGTH_LONG).show()
            return
        }
        if (retroView == null) {
            Toast.makeText(this, "El mapa del juego requiere una partida en emulación", Toast.LENGTH_LONG).show()
            return
        }
        runTravelSequence {
            openMapTab()
            Toast.makeText(
                this@MainActivity,
                "Pestaña Mapa. En Flawe, □ cambia de servidor y × elige el icono; al salir del menú cargas en el spawn de ese icono.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun runTravelSequence(block: suspend () -> Unit) {
        if (travelJob?.isActive == true) {
            Toast.makeText(this, "Espera a que termine la secuencia del mapa", Toast.LENGTH_SHORT).show()
            return
        }
        travelJob = lifecycleScope.launch {
            val view = retroView ?: return@launch
            view.frameSpeed = 1
            view.applyRuntimeOptions()
            try {
                block()
            } finally {
                view.frameSpeed = if (fastForward) 2 else 1
                view.applyRuntimeOptions()
            }
        }
    }

    private suspend fun openMapTab() {
        if (menuIsOpen()) {
            playPadSteps(FastTravelNavigator.dismissMenu())
            waitUntil(900) { !menuIsOpen() }
        }
        playPadSteps(FastTravelNavigator.pressStart())
        if (!waitUntil(1800) { menuIsOpen() }) {
            delay(500)
        }
        delay(FastTravelNavigator.MENU_SETTLE_MS)
        val controller = memoryController
        val before = controller?.readTabScan()
        playPadSteps(FastTravelNavigator.probeNextTab())
        val after = controller?.readTabScan()
        val cursor = if (before != null && after != null) {
            FastTravelNavigator.findTabCursor(before, after)
        } else {
            null
        }
        if (cursor != null) {
            playPadSteps(FastTravelNavigator.stepsTowardMap(cursor.index, cursor.r1Increases))
        } else {
            playPadSteps(FastTravelNavigator.fallbackAfterProbe())
        }
        delay(500)
    }

    private fun menuIsOpen(): Boolean {
        val overlay = memoryController?.readOverlaySignature() ?: 0L
        val snapshot = repository.snapshot.value
        if (FastTravelNavigator.isStatusMenu(overlay, snapshot.areaId, snapshot.mapId)) return true
        val ram = memoryController?.readAreaMap() ?: return snapshot.mode == GameMode.MANAGEMENT
        return FastTravelNavigator.isStatusMenu(overlay, ram.first, ram.second) ||
            snapshot.mode == GameMode.MANAGEMENT
    }

    private suspend fun waitUntil(timeoutMs: Long, condition: () -> Boolean): Boolean {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            if (condition()) return true
            delay(40)
        }
        return condition()
    }

    private suspend fun playPadSteps(steps: List<PadStep>) {
        val view = retroView ?: return
        steps.forEach { step ->
            val keyCode = retroPadKeyCode(step.button)
            view.sendKeyEvent(KeyEvent.ACTION_DOWN, keyCode, 0)
            delay(step.holdMs)
            view.sendKeyEvent(KeyEvent.ACTION_UP, keyCode, 0)
            delay(step.afterMs)
        }
    }

    private fun retroPadKeyCode(button: RetroPadButton): Int = when (button) {
        RetroPadButton.START -> KeyEvent.KEYCODE_BUTTON_START
        RetroPadButton.L1 -> KeyEvent.KEYCODE_BUTTON_L1
        RetroPadButton.R1 -> KeyEvent.KEYCODE_BUTTON_R1
        RetroPadButton.CROSS -> KeyEvent.KEYCODE_BUTTON_B
        RetroPadButton.TRIANGLE -> KeyEvent.KEYCODE_BUTTON_X
    }

    private fun movePartyMember(fromIndex: Int, toIndex: Int) {
        val snapshot = repository.snapshot.value
        if (!snapshot.canReorderParty) {
            Toast.makeText(this, "El orden del equipo solo se cambia fuera de batalla o eventos", Toast.LENGTH_LONG).show()
            return
        }
        val party = snapshot.party.map { it.profileId }.toMutableList()
        if (fromIndex !in party.indices || toIndex !in party.indices) return
        val moved = party.removeAt(fromIndex)
        party.add(toIndex, moved)
        val controller = memoryController
        if (controller == null) {
            Toast.makeText(this, "Reordenar requiere una partida en emulación", Toast.LENGTH_LONG).show()
            return
        }
        runCatching { controller.reorderParty(party) }
            .onSuccess { Toast.makeText(this, "Orden de salida actualizado", Toast.LENGTH_SHORT).show() }
            .onFailure { Toast.makeText(this, "No se pudo reordenar: ${it.message}", Toast.LENGTH_LONG).show() }
    }

    private fun toggleCheat(id: String, enabled: Boolean) {
        if (!modsEnabled) return
        if (enabled) enabledCheats += id else enabledCheats -= id
        persistEnabledCheats()
        applyEnabledCheats()
        syncDashboardExtras()
    }

    private fun persistEnabledCheats() {
        getPreferences(MODE_PRIVATE).edit()
            .putString(PREF_ENABLED_CHEATS, enabledCheats.joinToString(","))
            .apply()
    }

    private fun applyEnabledCheats() {
        val controller = memoryController ?: return
        val selected = enabledCheats.mapNotNull(CheatCatalog::byId)
        runCatching { controller.applyCheats(selected) }
            .onFailure { Toast.makeText(this, "No se pudieron aplicar los mods: ${it.message}", Toast.LENGTH_LONG).show() }
    }

    private fun toggleVirtualControls() {
        val next = !virtualControlsVisible()
        getPreferences(MODE_PRIVATE).edit().putBoolean(PREF_VIRTUAL_GAMEPAD, next).apply()
        virtualController?.gamepadVisible = next
        localDashboard?.controlsVisible = next
        presentation?.setControlsVisible(next)
        Toast.makeText(this, if (next) "Controles virtuales visibles" else "Controles virtuales ocultos", Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val PREF_ROM_URI = "rom_uri"
        private const val PREF_ROM_NAME = "rom_name"
        private const val PREF_ROM_VARIANT = "rom_variant"
        private const val PREF_ROM_SHA1 = "rom_sha1"
        private const val PREF_MUTED = "muted"
        private const val PREF_VIRTUAL_GAMEPAD = "virtual_gamepad"
        private const val PREF_MODS_ENABLED = "mods_enabled"
        private const val PREF_ENABLED_CHEATS = "enabled_cheats"
        private const val PREF_VISITED_MAPS = "visited_maps"
        private val GAME_KEYS = setOf(
            KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_RIGHT,
            KeyEvent.KEYCODE_BUTTON_A, KeyEvent.KEYCODE_BUTTON_B, KeyEvent.KEYCODE_BUTTON_X, KeyEvent.KEYCODE_BUTTON_Y,
            KeyEvent.KEYCODE_BUTTON_L1, KeyEvent.KEYCODE_BUTTON_L2, KeyEvent.KEYCODE_BUTTON_R1, KeyEvent.KEYCODE_BUTTON_R2,
            KeyEvent.KEYCODE_BUTTON_START, KeyEvent.KEYCODE_BUTTON_SELECT, KeyEvent.KEYCODE_BUTTON_THUMBL, KeyEvent.KEYCODE_BUTTON_THUMBR
        )
    }
}
