package com.digitaladventure.dw2003

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.provider.Settings
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.util.Consumer
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.window.java.layout.WindowInfoTrackerCallbackAdapter
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import com.digitaladventure.dw2003.data.AreaCatalog
import com.digitaladventure.dw2003.data.CheatCatalog
import com.digitaladventure.dw2003.data.AppRelease
import com.digitaladventure.dw2003.data.AppUpdateChecker
import com.digitaladventure.dw2003.data.AppUpdateStatus
import com.digitaladventure.dw2003.data.CustomCheatStore
import com.digitaladventure.dw2003.data.CompanionLanguage
import com.digitaladventure.dw2003.data.CompanionLanguageResolver
import com.digitaladventure.dw2003.data.CompanionLanguageSetting
import com.digitaladventure.dw2003.data.FastTravelCatalog
import com.digitaladventure.dw2003.data.MapRegionCatalog
import com.digitaladventure.dw2003.data.ServerRegion
import com.digitaladventure.dw2003.data.GameStateRepository
import com.digitaladventure.dw2003.model.GameMode
import com.digitaladventure.dw2003.ui.CompanionUiText
import com.digitaladventure.dw2003.emulation.BiosManager
import com.digitaladventure.dw2003.emulation.CrashLogStore
import com.digitaladventure.dw2003.emulation.FastTravelNavigator
import com.digitaladventure.dw2003.emulation.GameMemoryController
import com.digitaladventure.dw2003.emulation.MemoryPoller
import com.digitaladventure.dw2003.emulation.PadStep
import com.digitaladventure.dw2003.emulation.RetroPadButton
import com.digitaladventure.dw2003.emulation.RomVerifier
import com.digitaladventure.dw2003.emulation.SaveManager
import com.digitaladventure.dw2003.emulation.QuickStateManager
import com.digitaladventure.dw2003.ui.AdaptiveDualPaneLayout
import com.digitaladventure.dw2003.ui.AnalogStickMath
import com.digitaladventure.dw2003.ui.BattleScale
import com.digitaladventure.dw2003.ui.CompanionPresentation
import com.digitaladventure.dw2003.ui.DashboardActions
import com.digitaladventure.dw2003.ui.DigiviceDashboardView
import com.digitaladventure.dw2003.ui.GamePlaceholderView
import com.digitaladventure.dw2003.ui.GameSetupView
import com.digitaladventure.dw2003.ui.PadDirection
import com.digitaladventure.dw2003.ui.PaneArrangement
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
import java.io.File
import java.net.URL

class MainActivity : ComponentActivity(), DisplayManager.DisplayListener {
    private val repository = GameStateRepository()
    private lateinit var displayManager: DisplayManager
    private lateinit var saveManager: SaveManager
    private lateinit var biosManager: BiosManager
    private val updateChecker = AppUpdateChecker()
    private lateinit var customCheats: CustomCheatStore
    private lateinit var crashLog: CrashLogStore
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
    private var languageSetting = CompanionLanguageSetting.AUTO
    private var detectedLanguage: CompanionLanguage? = null
    private var battleScale = BattleScale.BATTLE_2X
    private var lastEnhancementEnabled: Boolean? = null
    private val analogDpadKeys = mutableSetOf<Int>()

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
        customCheats = CustomCheatStore(getPreferences(MODE_PRIVATE))
        crashLog = (application as? Dw2003App)?.crashLog ?: CrashLogStore(this).also { it.install() }
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
        battleScale = BattleScale.fromPreference(
            getPreferences(MODE_PRIVATE).getString(PREF_BATTLE_SCALE, null)
        )
        languageSetting = CompanionLanguageSetting.fromPreference(
            getPreferences(MODE_PRIVATE).getString(PREF_LANGUAGE, null)
        )
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
                    rememberVisited(snapshot.publicMapId, snapshot.publicMapId)
                    persistMemoryCardAfterSave(snapshot.areaId, snapshot.mapId)
                    localDashboard?.submitSnapshot(snapshot)
                    presentation?.submitSnapshot(snapshot)
                    syncDashboardExtras()
                    applyBattleEnhancement(snapshot.mode)
                }
            }
        }

        crashLog.note("activity-create")
        val stored = getPreferences(MODE_PRIVATE).getString(PREF_ROM_URI, null)
        if (stored != null) bootRom(Uri.parse(stored)) else showSetup()
        checkForAppUpdate(manual = false)
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
        memoryPoller?.stop()
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        applyAudioAndSpeed()
        memoryPoller?.start()
    }

    override fun onPause() {
        if (!skipNextAutoSave) persistMemoryCardIfSafe()
        skipNextAutoSave = false
        super.onPause()
    }

    override fun onDestroy() {
        crashLog.note("activity-destroy")
        memoryPoller?.stop()
        memoryPoller = null
        emulatorEventsJob?.cancel()
        emulatorEventsJob = null
        travelJob?.cancel()
        settingsDialog?.dismiss()
        presentation?.dismiss()
        presentation = null
        retroView?.let { view ->
            lifecycle.removeObserver(view)
            runCatching { view.onDestroy() }
        }
        retroView = null
        memoryController = null
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
        toast("Importando BIOS…", "Importing BIOS…", Toast.LENGTH_SHORT)
        lifecycleScope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) { biosManager.importBios(contentResolver, uri) }
            }.getOrElse {
                toast("BIOS rechazado: ${it.message}", "BIOS rejected: ${it.message}")
                return@launch
            }
            toast(
                "BIOS europeo instalado · SHA-1 ${result.sha1.take(10)}…",
                "European BIOS installed · SHA-1 ${result.sha1.take(10)}…"
            )
            recreate()
        }
    }

    private fun acceptMemoryCard(uri: Uri) {
        retroView?.let(saveManager::save)
        toast("Importando Memory Card…", "Importing Memory Card…", Toast.LENGTH_SHORT)
        lifecycleScope.launch {
            val size = runCatching {
                withContext(Dispatchers.IO) { saveManager.importCard(contentResolver, uri) }
            }.getOrElse {
                toast("Memory Card rechazada: ${it.message}", "Memory Card rejected: ${it.message}")
                return@launch
            }
            skipNextAutoSave = true
            toast("Memory Card importada (${size / 1024} KiB)", "Memory Card imported (${size / 1024} KiB)")
            recreate()
        }
    }

    private fun requestMemoryCardExport() {
        retroView?.let(saveManager::save)
        if (!saveManager.hasSave) {
            toast("Todavía no existe una Memory Card para exportar", "There is no Memory Card to export yet")
            return
        }
        createMemoryCard.launch("DW2003-memory-card.srm")
    }

    private fun exportMemoryCard(uri: Uri) {
        lifecycleScope.launch {
            val size = runCatching {
                withContext(Dispatchers.IO) { saveManager.exportCard(contentResolver, uri) }
            }.getOrElse {
                toast("No se pudo exportar: ${it.message}", "Could not export: ${it.message}")
                return@launch
            }
            toast("Memory Card exportada (${size / 1024} KiB)", "Memory Card exported (${size / 1024} KiB)")
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
        paneArrangementLabel = CompanionUiText.paneArrangement(resolvedLanguage(), paneArrangement()),
        onPaneArrangement = ::showPaneArrangementMenu,
        language = resolvedLanguage(),
        languageLabel = CompanionUiText.languageSetting(resolvedLanguage(), languageSetting),
        onLanguage = ::showLanguageMenu,
        gameHudLabel = if (gameHudVisible()) {
            CompanionUiText.pick(resolvedLanguage(), "HUD en el juego: visible", "Game HUD: visible")
        } else {
            CompanionUiText.pick(resolvedLanguage(), "HUD en el juego: oculto", "Game HUD: hidden")
        },
        onGameHud = ::toggleGameHud,
        battleScaleLabel = CompanionUiText.battleScale(resolvedLanguage(), battleScale),
        onBattleScale = ::showBattleScaleMenu,
        onClose = onClose,
        onReturnToStart = if (onClose != null) ::returnToStartScreen else null,
        hasCrashLog = crashLog.hasLog(),
        onViewCrashLog = ::showCrashLog,
        onCheckUpdate = { checkForAppUpdate(manual = true) }
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

    private fun showPaneArrangementMenu() {
        val arrangements = PaneArrangement.entries
        val current = paneArrangement()
        val language = resolvedLanguage()
        AlertDialog.Builder(this)
            .setTitle(CompanionUiText.pick(language, "Distribución de pantallas", "Screen layout"))
            .setSingleChoiceItems(
                arrangements.map { CompanionUiText.paneArrangement(language, it) }.toTypedArray(),
                arrangements.indexOf(current)
            ) { dialog, index ->
                val selected = arrangements[index]
                getPreferences(MODE_PRIVATE).edit()
                    .putString(PREF_PANE_ARRANGEMENT, selected.name)
                    .apply()
                dualLayout?.setArrangement(selected)
                dialog.dismiss()
                toast(
                    CompanionUiText.paneArrangement(CompanionLanguage.SPANISH, selected),
                    CompanionUiText.paneArrangement(CompanionLanguage.ENGLISH, selected),
                    Toast.LENGTH_SHORT
                )
            }
            .setNegativeButton(CompanionUiText.pick(language, "Cancelar", "Cancel"), null)
            .show()
    }

    private fun showLanguageMenu() {
        val options = CompanionLanguageSetting.entries
        val language = resolvedLanguage()
        AlertDialog.Builder(this)
            .setTitle(CompanionUiText.pick(language, "Idioma del panel", "Companion language"))
            .setSingleChoiceItems(
                options.map { CompanionUiText.languageSetting(language, it) }.toTypedArray(),
                options.indexOf(languageSetting)
            ) { dialog, index ->
                languageSetting = options[index]
                getPreferences(MODE_PRIVATE).edit()
                    .putString(PREF_LANGUAGE, languageSetting.name)
                    .apply()
                applyCompanionLanguage()
                dialog.dismiss()
                if (settingsDialog != null) {
                    showAppSettings()
                } else if (!dualContentActive) {
                    showSetup()
                }
            }
            .setNegativeButton(CompanionUiText.pick(language, "Cancelar", "Cancel"), null)
            .show()
    }

    private fun resolvedLanguage(): CompanionLanguage =
        CompanionLanguageResolver.resolve(languageSetting, detectedLanguage)

    private fun storedRomVariant(): RomVerifier.Variant =
        RomVerifier.Variant.fromStored(
            getPreferences(MODE_PRIVATE).getString(PREF_ROM_VARIANT, null),
            getPreferences(MODE_PRIVATE).getString(PREF_ROM_SHA1, null)
        )

    private fun applyCompanionLanguage() {
        val language = resolvedLanguage()
        localDashboard?.language = language
        presentation?.setLanguage(language)
        virtualController?.language = language
    }

    private fun toast(spanish: String, english: String, duration: Int = Toast.LENGTH_LONG) {
        Toast.makeText(this, CompanionUiText.pick(resolvedLanguage(), spanish, english), duration).show()
    }

    private fun paneArrangement(): PaneArrangement =
        PaneArrangement.fromPreference(
            getPreferences(MODE_PRIVATE).getString(PREF_PANE_ARRANGEMENT, null)
        )

    private fun acceptRom(uri: Uri) {
        runCatching {
            contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        toast("Verificando la imagen de disco…", "Verifying the disc image…")
        lifecycleScope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) { RomVerifier.verify(contentResolver, uri) }
            }.getOrElse {
                toast("No se pudo leer el BIN: ${it.message}", "Could not read the BIN: ${it.message}")
                return@launch
            }
            if (result.variant == RomVerifier.Variant.USA) {
                toast(
                    "Detectada: ${result.variant.label}. Viaje rápido y guía de Flawe desactivados.",
                    "Detected: ${result.variant.label}. Fast travel and Flawe walkthrough are off."
                )
                rememberAndRestart(uri, result)
            } else if (result.variant == RomVerifier.Variant.UNKNOWN) {
                val language = resolvedLanguage()
                AlertDialog.Builder(this@MainActivity)
                    .setTitle(CompanionUiText.pick(language, "ROM no verificada", "Unverified ROM"))
                    .setMessage(
                        CompanionUiText.pick(
                            language,
                            "SHA-1: ${result.sha1}\n\nLa lectura de RAM fue diseñada para SLES-03936, Flawe's Mod 2.0 y SLUS-01436 USA. Puedes continuar, pero los datos de la segunda pantalla podrían ser incorrectos.",
                            "SHA-1: ${result.sha1}\n\nRAM reading was designed for SLES-03936, Flawe's Mod 2.0 and SLUS-01436 USA. You can continue, but the second screen may be wrong."
                        )
                    )
                    .setNegativeButton(CompanionUiText.pick(language, "Cancelar", "Cancel"), null)
                    .setPositiveButton(CompanionUiText.pick(language, "Continuar", "Continue")) { _, _ ->
                        rememberAndRestart(uri, result)
                    }
                    .show()
            } else {
                toast("Detectada: ${result.variant.label}", "Detected: ${result.variant.label}")
                rememberAndRestart(uri, result)
            }
        }
    }

    private fun rememberAndRestart(uri: Uri, result: RomVerifier.Result) {
        getPreferences(MODE_PRIVATE).edit()
            .putString(PREF_ROM_URI, uri.toString())
            .putString(PREF_ROM_NAME, displayName(uri))
                            .putString(PREF_ROM_VARIANT, result.variant.name)
            .putString(PREF_ROM_SHA1, result.sha1)
            .apply()
        recreate()
    }

    private fun bootRom(uri: Uri) {
        if (retroView != null) return
        crashLog.note("boot-rom")
        val descriptor = runCatching { contentResolver.openFileDescriptor(uri, "r") }.getOrNull()
        if (descriptor == null) {
            getPreferences(MODE_PRIVATE).edit().remove(PREF_ROM_URI).apply()
            toast("No se conserva acceso al BIN. Selecciónalo de nuevo.", "BIN access was lost. Select it again.")
            showSetup()
            return
        }

        try {
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
                Variable("pcsx_rearmed_region", storedRomVariant().emulatorRegion),
                Variable("pcsx_rearmed_bios", "auto"),
                Variable("pcsx_rearmed_memcard1", "libretro"),
                Variable("pcsx_rearmed_memcard2", "none"),
                Variable("pcsx_rearmed_drc", "enabled"),
                Variable("pcsx_rearmed_drc_thread", "auto"),
                Variable("pcsx_rearmed_gpu_thread_rendering", "disabled"),
                Variable("pcsx_rearmed_spu_thread", "disabled"),
                Variable("pcsx_rearmed_frameskip_type", "disabled"),
                Variable("pcsx_rearmed_dithering", "enabled"),
                Variable(
                    "pcsx_rearmed_neon_enhancement_enable",
                    if (battleScale.enhancementEnabled(GameMode.EXPLORATION)) "enabled" else "disabled"
                ),
                Variable("pcsx_rearmed_neon_enhancement_tex_adj_v2", "enabled")
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
            toast(
                "BIOS HLE activo: el guardado dentro del juego puede congelarse. Importa un BIOS europeo desde ⚙ APP.",
                "HLE BIOS is active: in-game saving can freeze. Import a European BIOS from ⚙ APP."
            )
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
            quickBarVisible = gameHudVisible()
            gameHudVisible = gameHudVisible()
            language = resolvedLanguage()
            muted = this@MainActivity.muted
            fastForward = this@MainActivity.fastForward
            battleScale = this@MainActivity.battleScale
            stateAvailable = states.hasState || states.hasLegacyState
        }
        virtualController = controller
        val gameSurface = FrameLayout(this).apply {
            addView(view, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
            addView(controller, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
        }
        attachDualContent(gameSurface)

        val romVariant = storedRomVariant()
        if (romVariant == RomVerifier.Variant.USA) {
            detectedLanguage = CompanionLanguage.ENGLISH
        }
        memoryPoller = MemoryPoller(
            view,
            repository,
            lifecycleScope,
            romVariant.features,
            objectiveLanguageOverride = {
                when (languageSetting) {
                    CompanionLanguageSetting.ENGLISH -> 2
                    CompanionLanguageSetting.SPANISH -> 6
                    CompanionLanguageSetting.AUTO -> null
                }
            },
            onLanguageDetected = { detected ->
                if (detectedLanguage != detected) {
                    detectedLanguage = detected
                    if (languageSetting == CompanionLanguageSetting.AUTO) {
                        runOnUiThread { applyCompanionLanguage() }
                    }
                }
            }
        )
        memoryPoller?.start()
        emulatorEventsJob?.cancel()
        emulatorEventsJob = lifecycleScope.launch {
            launch {
                view.getGLRetroEvents().collect { event ->
                    if (event is GLRetroView.GLRetroEvents.FrameRendered ||
                        event is GLRetroView.GLRetroEvents.SurfaceCreated
                    ) {
                        view.setControllerType(0, RETRO_DEVICE_PSE_DUALSHOCK)
                        applyBattleEnhancement(repository.snapshot.value.mode)
                        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) memoryPoller?.start()
                    }
                }
            }
            launch {
                view.getGLRetroErrors().collect { code ->
                    toast("Error del núcleo de emulación ($code)", "Emulation core error ($code)")
                }
            }
        }
        } catch (error: Exception) {
            crashLog.note("boot-rom-failed ${error.javaClass.simpleName}: ${error.message}")
            retroView?.let { runCatching { lifecycle.removeObserver(it) } }
            retroView = null
            memoryController = null
            toast(
                "No se pudo reabrir la emulación: ${error.message}",
                "Could not reopen emulation: ${error.message}"
            )
            showSetup()
        }
    }

    private fun handleQuickAction(action: QuickAction) {
        val view = retroView ?: return
        when (action) {
            QuickAction.SAVE_STATE -> lifecycleScope.launch {
                val snapshot = repository.snapshot.value
                if (snapshot.areaId == 0x0C01 || snapshot.mapId == 0x0C01) {
                    toast(
                        "Espera a que termine el guardado del juego antes de crear un estado rápido",
                        "Wait for the in-game save to finish before creating a quick state"
                    )
                    return@launch
                }
                toast("Guardando estado rápido…", "Saving quick state…", Toast.LENGTH_SHORT)
                val result = runCatching {
                    withContext(Dispatchers.IO) {
                        val saved = quickStateManager?.save(view) ?: error("Gestor no disponible")
                        saveManager.persistSnapshot(saved.memoryCard)
                        saved
                    }
                }
                result.onSuccess { saved ->
                    virtualController?.stateAvailable = true
                    syncDashboardExtras()
                    toast(
                        "Estado + Memory Card guardados (${saved.stateBytes / 1024} KiB)",
                        "State + Memory Card saved (${saved.stateBytes / 1024} KiB)",
                        Toast.LENGTH_SHORT
                    )
                }.onFailure {
                    toast("No se pudo guardar: ${it.message}", "Could not save: ${it.message}")
                }
            }
            QuickAction.LOAD_STATE -> lifecycleScope.launch {
                toast("Cargando estado rápido…", "Loading quick state…", Toast.LENGTH_SHORT)
                val result = runCatching {
                    withContext(Dispatchers.IO) {
                        quickStateManager?.load(view)?.also { saveManager.persistSnapshot(it.memoryCard) }
                    }
                }
                val loaded = result.getOrNull()
                if (loaded != null) {
                    toast("Estado y Memory Card cargados", "State and Memory Card loaded", Toast.LENGTH_SHORT)
                } else {
                    val spanish = result.exceptionOrNull()?.message
                        ?: "No hay un estado compatible o no pudo cargarse"
                    val english = result.exceptionOrNull()?.message
                        ?: "No compatible state is available or it could not be loaded"
                    toast(spanish, english)
                }
            }
            QuickAction.TOGGLE_SPEED -> {
                fastForward = !fastForward
                view.frameSpeed = if (fastForward) 2 else 1
                view.applyRuntimeOptions()
                virtualController?.fastForward = fastForward
                syncDashboardExtras()
                toast(
                    if (fastForward) "Velocidad 2×" else "Velocidad normal",
                    if (fastForward) "Speed 2×" else "Normal speed",
                    Toast.LENGTH_SHORT
                )
            }
            QuickAction.TOGGLE_MUTE -> {
                muted = !muted
                view.audioEnabled = !muted
                view.applyRuntimeOptions()
                getPreferences(MODE_PRIVATE).edit().putBoolean(PREF_MUTED, muted).apply()
                virtualController?.muted = muted
                syncDashboardExtras()
                toast(
                    if (muted) "Sonido desactivado" else "Sonido activado",
                    if (muted) "Sound off" else "Sound on",
                    Toast.LENGTH_SHORT
                )
            }
            QuickAction.PICK_SCALE -> showBattleScaleMenu()
            QuickAction.TOGGLE_HUD -> toggleGameHud()
        }
    }

    private fun attachDualContent(gameView: View) {
        val layout = AdaptiveDualPaneLayout(this).apply {
            setArrangement(paneArrangement())
        }
        val dashboard = DigiviceDashboardView(this, dashboardActions()).apply {
            controlsVisible = virtualControlsVisible()
            gameHudVisible = gameHudVisible()
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
            val hatX = event.getAxisValue(MotionEvent.AXIS_HAT_X)
            val hatY = event.getAxisValue(MotionEvent.AXIS_HAT_Y)
            val leftX = event.getAxisValue(MotionEvent.AXIS_X)
            val leftY = event.getAxisValue(MotionEvent.AXIS_Y)
            view.sendMotionEvent(0, hatX, hatY, 0)
            view.sendMotionEvent(1, leftX, leftY, 0)
            view.sendMotionEvent(2, event.getAxisValue(MotionEvent.AXIS_Z), event.getAxisValue(MotionEvent.AXIS_RZ), 0)
            applyAnalogToDpad(view, hatX, hatY, leftX, leftY)
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
            it.setGameHudVisible(gameHudVisible())
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

    private fun gameHudVisible(): Boolean =
        getPreferences(MODE_PRIVATE).getBoolean(PREF_GAME_HUD, false)

    private fun dashboardActions() = DashboardActions(
        onAppSettings = ::showAppSettings,
        onToggleControls = ::toggleVirtualControls,
        onToggleGameHud = ::toggleGameHud,
        onQuickAction = ::handleQuickAction,
        onFastTravel = ::requestFastTravel,
        onOpenGameMap = ::openInGameMap,
        onPartyMove = ::movePartyMember,
        onCheatToggle = ::toggleCheat,
        onAddCustomCheat = ::showAddCustomCheat,
        onRemoveCustomCheat = ::removeCustomCheat
    )

    private fun applyAudioAndSpeed() {
        val view = retroView ?: return
        view.audioEnabled = !muted
        view.frameSpeed = if (fastForward) 2 else 1
        view.applyRuntimeOptions()
        virtualController?.muted = muted
        virtualController?.fastForward = fastForward
    }

    private fun applyAnalogToDpad(view: GLRetroView, hatX: Float, hatY: Float, leftX: Float, leftY: Float) {
        val fromHat = AnalogStickMath.dpadFromStick(hatX, hatY, 0.5f)
        val next = if (fromHat.isNotEmpty()) {
            fromHat
        } else {
            AnalogStickMath.dpadFromStick(leftX, leftY)
        }.map {
            when (it) {
                PadDirection.UP -> KeyEvent.KEYCODE_DPAD_UP
                PadDirection.DOWN -> KeyEvent.KEYCODE_DPAD_DOWN
                PadDirection.LEFT -> KeyEvent.KEYCODE_DPAD_LEFT
                PadDirection.RIGHT -> KeyEvent.KEYCODE_DPAD_RIGHT
            }
        }.toSet()
        (analogDpadKeys - next).forEach { view.sendKeyEvent(KeyEvent.ACTION_UP, it, 0) }
        (next - analogDpadKeys).forEach { view.sendKeyEvent(KeyEvent.ACTION_DOWN, it, 0) }
        analogDpadKeys.clear()
        analogDpadKeys += next
    }

    private fun applyBattleEnhancement(mode: GameMode, announce: Boolean = false) {
        val view = retroView ?: return
        val enabled = battleScale.enhancementEnabled(mode)
        if (lastEnhancementEnabled == enabled) return
        lastEnhancementEnabled = enabled
        view.updateVariables(
            Variable("pcsx_rearmed_neon_enhancement_enable", if (enabled) "enabled" else "disabled"),
            Variable("pcsx_rearmed_neon_enhancement_tex_adj_v2", "enabled")
        )
        if (announce) {
            toast(
                if (enabled) "Resolución interna 2× activa" else "Resolución nativa",
                if (enabled) "Internal 2× resolution on" else "Native resolution",
                Toast.LENGTH_SHORT
            )
        }
    }

    private fun syncDashboardExtras() {
        val stateAvailable = virtualController?.stateAvailable ?: (quickStateManager?.hasState == true)
        localDashboard?.modsEnabled = modsEnabled
        localDashboard?.enabledCheats = enabledCheats
        localDashboard?.customCheats = customCheats.all()
        localDashboard?.visitedMaps = visitedMaps
        localDashboard?.gameHudVisible = gameHudVisible()
        localDashboard?.quickMuted = muted
        localDashboard?.quickFastForward = fastForward
        localDashboard?.quickStateAvailable = stateAvailable
        localDashboard?.battleScale = battleScale
        virtualController?.quickBarVisible = gameHudVisible()
        virtualController?.gameHudVisible = gameHudVisible()
        virtualController?.battleScale = battleScale
        applyCompanionLanguage()
        presentation?.setModsEnabled(modsEnabled)
        presentation?.setEnabledCheats(enabledCheats)
        presentation?.setCustomCheats(customCheats.all())
        presentation?.setVisitedMaps(visitedMaps)
        presentation?.setGameHudVisible(gameHudVisible())
        presentation?.setQuickBar(muted, fastForward, stateAvailable, battleScale)
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
        toast(
            if (enabled) "Pestaña Mods visible" else "Pestaña Mods oculta",
            if (enabled) "Mods tab visible" else "Mods tab hidden",
            Toast.LENGTH_SHORT
        )
    }

    private fun rememberVisited(areaId: Int, mapId: Int) {
        var changed = false
        listOf(areaId, mapId, FastTravelCatalog.iconId(areaId, mapId)).forEach { id ->
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
        if (!snapshot.supportsFastTravel) {
            toast(
                "El viaje rápido de Flawe no está disponible en Digimon World 3 USA.",
                "Flawe fast travel is not available on Digimon World 3 USA."
            )
            return
        }
        if (!snapshot.canFastTravel) {
            toast("Viaje rápido bloqueado durante batalla o eventos", "Fast travel is blocked during battle or events")
            return
        }
        val controller = memoryController
        if (controller == null || retroView == null) {
            toast("El viaje rápido requiere una partida en emulación", "Fast travel needs an emulated game session")
            return
        }
        val currentIcon = FastTravelCatalog.iconId(snapshot.publicMapId)
        if (areaId !in FastTravelCatalog.rememberedIcons(visitedMaps, snapshot.publicMapId)) {
            toast("Ese destino aún no está registrado como visitado", "That destination has not been recorded as visited")
            return
        }
        if (areaId == currentIcon) return
        val destinationServer = MapRegionCatalog.resolve(areaId).server
        val currentServer = MapRegionCatalog.resolve(currentIcon).server
        crashLog.note("fast-travel dest=0x${areaId.toString(16)} from=0x${currentIcon.toString(16)}")
        runTravelSequence {
            // Dispatcher code persists after the map closes; its presence is not
            // evidence that the map UI is open. Establish a known menu state.
            openMapTab()
            delay(500)
            if (currentServer != ServerRegion.UNKNOWN &&
                destinationServer != ServerRegion.UNKNOWN &&
                currentServer != destinationServer
            ) {
                playPadSteps(FastTravelNavigator.switchServer())
                delay(450)
            }
            waitUntil(1800) { controller.hasFlaweDispatcher() }
            var directToken = if (destinationServer == ServerRegion.ASUKA) {
                withContext(Dispatchers.Default) { controller.beginDirectFlaweWarp(areaId) }
            } else {
                null
            }
            if (directToken == null && destinationServer == ServerRegion.ASUKA) {
                waitUntil(1200) { controller.hasFlaweDispatcher() }
                directToken = withContext(Dispatchers.Default) { controller.beginDirectFlaweWarp(areaId) }
            }
            if (directToken != null) {
                try {
                    playPadSteps(FastTravelNavigator.selectMapDestination())
                } finally {
                    controller.restoreDirectFlaweWarp(directToken)
                }
                playPadSteps(FastTravelNavigator.exitMapMenu())
            } else if (destinationServer == ServerRegion.ASUKA) {
                val walk = FastTravelNavigator.stepsToFlaweIcon(
                    currentIcon,
                    areaId,
                    FastTravelCatalog.cycleOrder(areaId)
                )
                if (walk.isEmpty()) {
                    crashLog.note("fast-travel left map open dest=0x${areaId.toString(16)}")
                    toast(
                        "El mapa está abierto, pero no se detectó una función compatible de viaje rápido. Comprueba que el mod de Flawe esté activo en el idioma del juego.",
                        "The map is open, but no compatible fast-travel function was detected. Check that Flawe's mod is active in the game's language."
                    )
                    return@runTravelSequence
                }
                playPadSteps(walk)
                playPadSteps(FastTravelNavigator.confirmMapDestination())
            } else {
                toast(
                    "Mapa de Amaterasu abierto. El IPS de Flawe no publica esos iconos; elige el destino con la cruceta y ×.",
                    "Amaterasu map opened. Flawe's IPS does not publish those icons; pick the destination with the D-pad and ×."
                )
                return@runTravelSequence
            }
            waitUntil(2500) { !menuIsOpen() }
            if (menuIsOpen()) {
                playPadSteps(FastTravelNavigator.closeMenu())
                waitUntil(900) { !menuIsOpen() }
            }
            val arrived = waitUntil(6000) {
                val latest = repository.snapshot.value
                latest.mode == GameMode.EXPLORATION &&
                    FastTravelCatalog.iconId(latest.publicMapId) == areaId
            }
            val destination = CompanionUiText.area(resolvedLanguage(), areaId)
            toast(
                if (arrived) "Llegaste a $destination" else "No se pudo confirmar el viaje a $destination. Revisa el mapa del juego.",
                if (arrived) "Arrived at $destination" else "Could not confirm travel to $destination. Check the in-game map."
            )
        }
    }

    private fun openInGameMap() {
        val snapshot = repository.snapshot.value
        if (!snapshot.supportsFastTravel) {
            toast(
                "El mapa de Flawe no está disponible en Digimon World 3 USA.",
                "Flawe's map is not available on Digimon World 3 USA."
            )
            return
        }
        if (!snapshot.canFastTravel) {
            toast("El mapa no está disponible durante batallas o eventos", "The map is unavailable during battles or events")
            return
        }
        if (retroView == null) {
            toast("El mapa del juego requiere una partida en emulación", "The in-game map needs an emulated session")
            return
        }
        crashLog.note("open-map area=0x${snapshot.areaId.toString(16)} map=0x${snapshot.mapId.toString(16)}")
        runTravelSequence {
            openMapTab()
            toast("Mapa abierto", "Map opened")
        }
    }

    private fun runTravelSequence(block: suspend () -> Unit) {
        if (travelJob?.isActive == true) {
            toast("Espera a que termine la secuencia del mapa", "Wait for the map sequence to finish", Toast.LENGTH_SHORT)
            return
        }
        travelJob = lifecycleScope.launch {
            val view = retroView ?: return@launch
            view.frameSpeed = 1
            view.applyRuntimeOptions()
            try {
                block()
            } catch (cancelled: kotlinx.coroutines.CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                crashLog.note("fast-travel-failed ${error.javaClass.simpleName}: ${error.message}")
                android.util.Log.e("DW2003Travel", "Map/travel sequence failed", error)
                toast("No se pudo completar el viaje rápido", "Could not complete fast travel")
            } finally {
                view.frameSpeed = if (fastForward) 2 else 1
                view.applyRuntimeOptions()
            }
        }
    }

    private suspend fun openMapTab() {
        // Neither the field START list nor its Map page loads STSTATUS.
        // Cancel unwinds both pages and is a no-op in exploration. This also
        // works when a localized ROM has no Flawe walkthrough widget to read.
        repeat(3) {
            playPadSteps(FastTravelNavigator.dismissMenu())
        }
        delay(350)
        playPadSteps(FastTravelNavigator.pressStart())
        delay(800)
        playPadSteps(FastTravelNavigator.stepsToMapFromUnknown())
        delay(800)
    }

    private fun menuIsOpen(): Boolean {
        if (memoryController?.isFieldMenuVisible() == true) return true
        val overlay = memoryController?.readOverlaySignature() ?: 0L
        val ram = memoryController?.readAreaMap() ?: return false
        return FastTravelNavigator.isStatusMenu(overlay, ram.first, ram.second)
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
            try {
                delay(step.holdMs)
            } finally {
                view.sendKeyEvent(KeyEvent.ACTION_UP, keyCode, 0)
            }
            delay(step.afterMs)
        }
    }

    private fun retroPadKeyCode(button: RetroPadButton): Int = when (button) {
        RetroPadButton.START -> KeyEvent.KEYCODE_BUTTON_START
        RetroPadButton.L1 -> KeyEvent.KEYCODE_BUTTON_L1
        RetroPadButton.R1 -> KeyEvent.KEYCODE_BUTTON_R1
        RetroPadButton.CROSS -> KeyEvent.KEYCODE_BUTTON_B
        RetroPadButton.TRIANGLE -> KeyEvent.KEYCODE_BUTTON_X
        RetroPadButton.SQUARE -> KeyEvent.KEYCODE_BUTTON_Y
        RetroPadButton.DPAD_UP -> KeyEvent.KEYCODE_DPAD_UP
        RetroPadButton.DPAD_DOWN -> KeyEvent.KEYCODE_DPAD_DOWN
    }

    private fun movePartyMember(fromIndex: Int, toIndex: Int) {
        val snapshot = repository.snapshot.value
        if (!snapshot.canReorderParty) {
            toast(
                "El orden del equipo solo se cambia fuera de batalla o eventos",
                "Party order can only be changed outside battle or events"
            )
            return
        }
        val party = snapshot.party.map { it.profileId }.toMutableList()
        if (fromIndex !in party.indices || toIndex !in party.indices) return
        val moved = party.removeAt(fromIndex)
        party.add(toIndex, moved)
        val controller = memoryController
        if (controller == null) {
            toast("Reordenar requiere una partida en emulación", "Reordering needs an emulated game session")
            return
        }
        runCatching { controller.reorderParty(party) }
            .onSuccess { toast("Orden de salida actualizado", "Battle order updated", Toast.LENGTH_SHORT) }
            .onFailure { toast("No se pudo reordenar: ${it.message}", "Could not reorder: ${it.message}") }
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
        val selected = enabledCheats.mapNotNull { id -> CheatCatalog.byId(id) ?: customCheats.byId(id) }
        runCatching { controller.applyCheats(selected) }
            .onFailure { toast("No se pudieron aplicar los mods: ${it.message}", "Could not apply mods: ${it.message}") }
    }

    private fun showAddCustomCheat() {
        val language = resolvedLanguage()
        val name = EditText(this).apply {
            hint = CompanionUiText.pick(language, "Nombre del mod", "Mod name")
        }
        val code = EditText(this).apply {
            hint = "800XXXXX YYYY"
            minLines = 2
        }
        val body = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 0)
            addView(name)
            addView(code)
        }
        AlertDialog.Builder(this)
            .setTitle(CompanionUiText.pick(language, "Añadir mod personalizado", "Add custom mod"))
            .setView(body)
            .setPositiveButton(CompanionUiText.pick(language, "Guardar", "Save")) { _, _ ->
                val spec = customCheats.add(name.text.toString(), code.text.toString())
                crashLog.note("add-custom-cheat")
                if (spec == null) {
                    toast(
                        "El código debe ser pares PAL 800XXXXX YYYY",
                        "The code must be PAL 800XXXXX YYYY pairs"
                    )
                    return@setPositiveButton
                }
                syncDashboardExtras()
                toast("Mod añadido", "Mod added", Toast.LENGTH_SHORT)
            }
            .setNegativeButton(CompanionUiText.pick(language, "Cancelar", "Cancel"), null)
            .show()
    }

    private fun removeCustomCheat(id: String) {
        crashLog.note("remove-custom-cheat id=$id")
        enabledCheats -= id
        customCheats.remove(id)
        persistEnabledCheats()
        applyEnabledCheats()
        syncDashboardExtras()
        toast("Mod personalizado eliminado", "Custom mod removed", Toast.LENGTH_SHORT)
    }

    private fun installedVersionName(): String =
        packageManager.getPackageInfo(packageName, 0).versionName.orEmpty()

    private fun checkForAppUpdate(manual: Boolean) {
        lifecycleScope.launch {
            val installed = installedVersionName()
            val status = withContext(Dispatchers.IO) {
                runCatching { updateChecker.check(installed) }.getOrElse { error ->
                    AppUpdateStatus.Unavailable(error.message ?: error.javaClass.simpleName)
                }
            }
            if (isFinishing || isDestroyed) return@launch
            when (status) {
                is AppUpdateStatus.Available -> {
                    val release = status.release
                    AlertDialog.Builder(this@MainActivity)
                        .setTitle(CompanionUiText.pick(resolvedLanguage(), "Actualización disponible", "Update available"))
                        .setMessage(
                            CompanionUiText.pick(
                                resolvedLanguage(),
                                "Hay ${release.tag} en GitHub. Esta instalación es $installed. ¿Descargar e instalar el APK?",
                                "${release.tag} is on GitHub. This install is $installed. Download and install the APK?"
                            ) + "\n\n" + CompanionUiText.pick(resolvedLanguage(), "NOVEDADES", "WHAT'S NEW") +
                                "\n\n" + release.changelog.ifBlank {
                                    CompanionUiText.pick(resolvedLanguage(),
                                        "Esta versión no incluye notas de cambios.",
                                        "No release notes were provided for this version.")
                                }
                        )
                        .setPositiveButton(CompanionUiText.pick(resolvedLanguage(), "Actualizar", "Update")) { _, _ ->
                            downloadAndInstall(release)
                        }
                        .setNegativeButton(CompanionUiText.pick(resolvedLanguage(), "Después", "Later"), null)
                        .show()
                }
                is AppUpdateStatus.Current -> {
                    if (manual) {
                        toast(
                            "Ya tienes la última versión ($installed; GitHub ${status.remoteTag})",
                            "You already have the latest version ($installed; GitHub ${status.remoteTag})"
                        )
                    }
                }
                is AppUpdateStatus.Unavailable -> {
                    crashLog.note("app-update-check-failed ${status.reason}")
                    if (manual) {
                        toast(
                            "No se pudo consultar GitHub: ${status.reason}",
                            "Could not reach GitHub: ${status.reason}"
                        )
                    }
                }
            }
        }
    }

    private fun downloadAndInstall(release: AppRelease) {
        lifecycleScope.launch {
            crashLog.note("app-update ${release.tag}")
            toast("Descargando ${release.tag}…", "Downloading ${release.tag}…")
            val file = File(cacheDir, "DW2003-update.apk")
            val downloaded = withContext(Dispatchers.IO) {
                runCatching {
                    URL(release.apkUrl).openStream().use { input ->
                        file.outputStream().use { input.copyTo(it) }
                    }
                    file.takeIf { it.length() > 0 }
                }.getOrElse { error ->
                    toast("No se pudo descargar: ${error.message}", "Download failed: ${error.message}")
                    null
                }
            }
            if (downloaded == null || isFinishing || isDestroyed) return@launch
            if (!packageManager.canRequestPackageInstalls()) {
                startActivity(
                    Intent(
                        Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                        Uri.parse("package:$packageName")
                    )
                )
                toast(
                    "Activa instalar apps desconocidas y pulsa Buscar actualización otra vez.",
                    "Allow unknown apps, then tap Check for update again."
                )
                return@launch
            }
            val uri = FileProvider.getUriForFile(this@MainActivity, "$packageName.files", downloaded)
            startActivity(
                Intent(Intent.ACTION_VIEW)
                    .setDataAndType(uri, "application/vnd.android.package-archive")
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            )
        }
    }

    private fun showCrashLog() {
        val language = resolvedLanguage()
        val text = crashLog.read().orEmpty().ifBlank {
            CompanionUiText.pick(language, "Todavía no hay un crash registrado.", "No crash has been recorded yet.")
        }
        val view = ScrollView(this).apply {
            addView(TextView(this@MainActivity).apply {
                this.text = text
                setPadding(36, 24, 36, 24)
                textSize = 12f
                setTextIsSelectable(true)
            })
        }
        AlertDialog.Builder(this)
            .setTitle(CompanionUiText.pick(language, "Último crash", "Last crash"))
            .setView(view)
            .setPositiveButton(CompanionUiText.pick(language, "Cerrar", "Close"), null)
            .setNeutralButton(CompanionUiText.pick(language, "Borrar", "Clear")) { _, _ ->
                crashLog.clear()
                if (settingsDialog != null) showAppSettings()
            }
            .setNegativeButton(CompanionUiText.pick(language, "Compartir", "Share")) { _, _ ->
                val send = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "DW2003 Dual Screen crash")
                    putExtra(Intent.EXTRA_TEXT, text)
                }
                startActivity(Intent.createChooser(send, "Crash log"))
            }
            .show()
    }

    private fun toggleGameHud() {
        val next = !gameHudVisible()
        getPreferences(MODE_PRIVATE).edit().putBoolean(PREF_GAME_HUD, next).apply()
        syncDashboardExtras()
        if (settingsDialog != null) showAppSettings()
        toast(
            if (next) "HUD del juego visible" else "HUD del juego oculto",
            if (next) "Game HUD visible" else "Game HUD hidden",
            Toast.LENGTH_SHORT
        )
    }

    private fun showBattleScaleMenu() {
        val options = BattleScale.entries
        val language = resolvedLanguage()
        AlertDialog.Builder(this)
            .setTitle(CompanionUiText.pick(language, "Resolución en combate", "Battle resolution"))
            .setSingleChoiceItems(
                options.map { CompanionUiText.battleScale(language, it) }.toTypedArray(),
                options.indexOf(battleScale)
            ) { dialog, index ->
                battleScale = options[index]
                lastEnhancementEnabled = null
                getPreferences(MODE_PRIVATE).edit()
                    .putString(PREF_BATTLE_SCALE, battleScale.name)
                    .apply()
                applyBattleEnhancement(repository.snapshot.value.mode, announce = true)
                dialog.dismiss()
                if (settingsDialog != null) showAppSettings()
            }
            .setNegativeButton(CompanionUiText.pick(language, "Cancelar", "Cancel"), null)
            .show()
    }

    private fun toggleVirtualControls() {
        val next = !virtualControlsVisible()
        getPreferences(MODE_PRIVATE).edit().putBoolean(PREF_VIRTUAL_GAMEPAD, next).apply()
        virtualController?.gamepadVisible = next
        localDashboard?.controlsVisible = next
        presentation?.setControlsVisible(next)
        toast(
            if (next) "Controles virtuales visibles" else "Controles virtuales ocultos",
            if (next) "Virtual controls visible" else "Virtual controls hidden",
            Toast.LENGTH_SHORT
        )
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
        private const val PREF_PANE_ARRANGEMENT = "pane_arrangement"
        private const val PREF_LANGUAGE = "companion_language"
        private const val PREF_GAME_HUD = "game_hud"
        private const val PREF_BATTLE_SCALE = "battle_scale"
        // RETRO_DEVICE_SUBCLASS(RETRO_DEVICE_ANALOG, 1) — DualShock
        private const val RETRO_DEVICE_PSE_DUALSHOCK = (2 shl 8) or 5
        private val GAME_KEYS = setOf(
            KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_RIGHT,
            KeyEvent.KEYCODE_BUTTON_A, KeyEvent.KEYCODE_BUTTON_B, KeyEvent.KEYCODE_BUTTON_X, KeyEvent.KEYCODE_BUTTON_Y,
            KeyEvent.KEYCODE_BUTTON_L1, KeyEvent.KEYCODE_BUTTON_L2, KeyEvent.KEYCODE_BUTTON_R1, KeyEvent.KEYCODE_BUTTON_R2,
            KeyEvent.KEYCODE_BUTTON_START, KeyEvent.KEYCODE_BUTTON_SELECT, KeyEvent.KEYCODE_BUTTON_THUMBL, KeyEvent.KEYCODE_BUTTON_THUMBR
        )
    }
}
