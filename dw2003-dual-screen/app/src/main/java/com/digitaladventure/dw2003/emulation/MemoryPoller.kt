package com.digitaladventure.dw2003.emulation

import android.util.Log
import android.os.SystemClock
import com.digitaladventure.dw2003.data.OverlayScanSchedule
import com.digitaladventure.dw2003.data.CompanionLanguage
import com.digitaladventure.dw2003.data.BattleSetupReader
import com.digitaladventure.dw2003.data.RamWatch
import com.digitaladventure.dw2003.data.GameStateReader
import com.digitaladventure.dw2003.data.FlaweMenuStateReader
import com.digitaladventure.dw2003.data.FlaweOverlayWatch
import com.digitaladventure.dw2003.data.OverlaySceneResolver
import com.digitaladventure.dw2003.model.OverlayScene
import com.digitaladventure.dw2003.data.OverlaySignatures
import com.digitaladventure.dw2003.model.BattleEnemy
import com.digitaladventure.dw2003.model.GameMode
import com.digitaladventure.dw2003.model.RamProbe
import com.digitaladventure.dw2003.data.GameStateRepository
import com.digitaladventure.dw2003.data.PalLanguage
import com.digitaladventure.dw2003.data.CompanionRomFeatures
import com.digitaladventure.dw2003.data.OverlayLocationFinder
import com.digitaladventure.dw2003.data.WalkthroughTextFinder
import com.digitaladventure.dw2003.data.FlaweWalkthroughReader
import com.digitaladventure.dw2003.data.FlaweGuideCatalog
import com.swordfish.libretrodroid.GLRetroView
import com.swordfish.libretrodroid.LibretroDroid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MemoryPoller(
    private val view: GLRetroView,
    private val repository: GameStateRepository,
    private val scope: CoroutineScope,
    private val features: CompanionRomFeatures = CompanionRomFeatures.PAL,
    private val onLanguageDetected: (CompanionLanguage) -> Unit = {},
    private val objectiveLanguageOverride: () -> Int? = { null },
    private val ramCaptures: RamCaptureStore? = null
) {
    private val guide by lazy {
        FlaweGuideCatalog(
            view.context.assets.open("guide/selector.bin").use { it.readBytes() },
            view.context.assets.open("guide/objectives.json").bufferedReader().use { it.readText() }
        )
    }
    private val reader = GameStateReader()
    private var job: Job? = null
    private var cachedObjective: String? = null
    private var cachedStoryStage = -1
    private var cachedEnemies = emptyList<BattleEnemy>()
    private var wasInBattle = false
    private var previousSetup: ByteArray? = null
    private var previousArena: ByteArray? = null
    private var previousTravel: FlaweOverlayWatch.Snapshot? = null
    private var previousScene: OverlayScene? = null
    private val overlayScans = OverlayScanSchedule()
    @Volatile
    private var stopped = false

    fun start() {
        if (job != null) return
        stopped = false
        job = scope.launch(Dispatchers.Default) {
            while (isActive && !stopped) {
                try {
                    pollOnce()
                } catch (error: Exception) {
                    Log.d(TAG, "RAM not ready yet", error)
                }
                delay(POLL_INTERVAL_MS)
            }
        }
    }

    fun stop() {
        stopped = true
        job?.cancel()
        job = null
    }

    private fun pollOnce() {
        val main = read(features.ramBase, GameStateReader.MAIN_LENGTH)
        val overlay = read(GameStateReader.OVERLAY_BASE, 4)
        val overlaySlotBytes = runCatching {
            read(OverlaySignatures.SLOT_BASE, 4)
        }.getOrNull()
        val hookWord = GameStateReader.u32(overlay, 0)
        val slotWord = overlaySlotBytes?.let { GameStateReader.u32(it, 0) } ?: 0L
        val signature = OverlaySignatures.preferred(hookWord, slotWord)
        val storyStage = GameStateReader.u16(
            main,
            GameStateReader.STORY_STAGE - GameStateReader.MAIN_BASE
        )
        val areaId = GameStateReader.u16(main, GameStateReader.AREA - GameStateReader.MAIN_BASE)
        val mapId = GameStateReader.u16(main, GameStateReader.MAP_ID - GameStateReader.MAIN_BASE)
        val languageCode = if (features.detectPalLanguage) GameStateReader.u32(
            read(GameStateReader.PAL_LANGUAGE and RAM_MASK, 4), 0
        ).toInt() else PalLanguage.US_ENGLISH
        val palLanguage = PalLanguage.companionLanguage(languageCode)
        palLanguage?.let(onLanguageDetected)
        val locationKey = (areaId shl 16) or (mapId and 0xFFFF)
        if (storyStage != cachedStoryStage) {
            cachedStoryStage = storyStage
            cachedObjective = null
        }
        val fieldMenuVisible = runCatching { FlaweMenuStateReader.isFieldMenuVisible(::read) }.getOrNull()
        val flaweMapLoaded = runCatching {
            FlaweDirectWarpPatch.matchesV2(read(FlaweDirectWarpPatch.V2_RAM_OFFSET, FlaweDirectWarpPatch.V2_WINDOW_SIZE)) ||
                FlaweDirectWarpPatch.matchesPreferred(
                    read(FlaweDirectWarpPatch.DISPATCHER_RAM_OFFSET, FlaweDirectWarpPatch.WINDOW_SIZE)
                )
        }.getOrDefault(false)
        val liveMode = OverlaySignatures.mode(hookWord, slotWord)
        val scene = OverlaySceneResolver.resolve(liveMode, areaId, mapId, fieldMenuVisible, flaweMapLoaded)
        val inBattle = liveMode == GameMode.BATTLE
        var overlayStageId: Int? = null
        val menuOverlay = areaId == OverlaySceneResolver.MENU_OVERLAY ||
            mapId == OverlaySceneResolver.MENU_OVERLAY
        if (!inBattle && !menuOverlay && !stopped &&
            overlayScans.shouldScan(locationKey, signature, SystemClock.elapsedRealtime())
        ) {
            try {
                val overlayBytes = read(OVERLAY_SCAN_BASE, OVERLAY_SCAN_LENGTH)
                overlayStageId = OverlayLocationFinder.stageId(overlayBytes)
            } catch (error: Exception) {
                Log.d(TAG, "Overlay RAM not ready", error)
            }
        }

        val objective = if (features.supportsWalkthrough) {
            try {
                // The same quest flags select the English guide in every PAL
                // language. Reuse this poll's small snapshot, with no RAM scan.
                guide.objective(main, objectiveLanguageOverride() ?: languageCode)
                    ?: readObjective(signature, palLanguage)
            } catch (error: Exception) {
                Log.d(TAG, "Objective RAM not ready", error)
                cachedObjective
            }
        } else {
            null
        }
        val battleSetup = runCatching {
            read(BattleSetupReader.SETUP_BASE, BattleSetupReader.SETUP_LENGTH)
        }.getOrNull()
        val battleArena = runCatching {
            read(BattleSetupReader.ARENA_BASE, BattleSetupReader.ARENA_LENGTH)
        }.getOrNull()
        val battleScan = if (inBattle) {
            runCatching { read(BattleSetupReader.SCAN_BASE, BattleSetupReader.SCAN_LENGTH) }.getOrNull()
        } else {
            null
        }
        val spanishNames = (objectiveLanguageOverride() ?: languageCode) == PalLanguage.SPANISH ||
            palLanguage == CompanionLanguage.SPANISH
        val travel = if (ramCaptures != null) {
            runCatching { FlaweOverlayWatch.collect(::read) }.getOrNull()
        } else {
            null
        }
        val setupChanges = RamWatch.wordChanges(previousSetup, battleSetup ?: ByteArray(0), BattleSetupReader.SETUP_BASE)
        val arenaChanges = RamWatch.wordChanges(previousArena, battleArena ?: ByteArray(0), BattleSetupReader.ARENA_BASE)
        val travelChanges = FlaweOverlayWatch.wordChanges(previousTravel, travel ?: FlaweOverlayWatch.Snapshot())
        previousSetup = battleSetup
        previousArena = battleArena
        previousTravel = travel
        val mappingScene = scene == OverlayScene.MENU ||
            scene == OverlayScene.MAP ||
            scene == OverlayScene.FAST_TRAVEL
        val sceneEntered = mappingScene && previousScene != scene
        previousScene = scene
        val changes = (
            setupChanges + arenaChanges + if (mappingScene) travelChanges else emptyList()
            ).take(24)
        val ramProbe = if (ramCaptures != null) {
            RamProbe(
                overlaySignature = signature,
                hookWord = hookWord,
                slotWord = slotWord,
                inBattle = inBattle,
                scene = scene.name,
                setupSummary = BattleSetupReader.summarizeSlots(battleSetup ?: ByteArray(0), spanishNames),
                travelSummary = travel?.let {
                    FlaweOverlayWatch.summarize(it, areaId, mapId, fieldMenuVisible, flaweMapLoaded)
                }.orEmpty(),
                setupHex = RamWatch.hexDump(battleSetup ?: ByteArray(0), BattleSetupReader.SETUP_BASE),
                arenaHex = RamWatch.hexDump(battleArena ?: ByteArray(0), BattleSetupReader.ARENA_BASE),
                travelHex = travel?.let { FlaweOverlayWatch.hexDump(it) }.orEmpty(),
                changes = changes,
                captures = ramCaptures.latest
            )
        } else {
            null
        }
        if (ramCaptures != null && ramProbe != null) {
            ramCaptures.maybeCapture(ramProbe, changes, force = sceneEntered)
        }
        val snapshot = reader.parse(
            main,
            signature,
            objective,
            features,
            overlayStageId,
            battleSetup,
            battleArena,
            battleScan,
            spanishNames,
            ramProbe,
            slotWord,
            fieldMenuVisible,
            flaweMapLoaded
        )
        if (snapshot.enemies.isNotEmpty()) {
            cachedEnemies = snapshot.enemies
        }
        val published = if (inBattle && snapshot.enemies.isEmpty() && cachedEnemies.isNotEmpty()) {
            snapshot.copy(enemies = cachedEnemies)
        } else {
            snapshot
        }
        if (!inBattle && wasInBattle) {
            cachedEnemies = emptyList()
        }
        wasInBattle = inBattle
        repository.publish(published)
    }

    private fun read(offset: Int, length: Int): ByteArray {
        check(!stopped) { "Memory poller stopped" }
        return view.readMemory(LibretroDroid.MEMORY_SYSTEM_RAM, offset, length)
    }

    private fun readObjective(
        signature: Long,
        palLanguage: CompanionLanguage?
    ): String? {
        if (OverlaySignatures.mode(signature) != GameMode.BATTLE) {
            FlaweWalkthroughReader.read(::read)?.let { cacheObjective(it, palLanguage) }
        }
        return cachedObjective
    }

    private fun cacheObjective(value: String, palLanguage: CompanionLanguage?) {
        cachedObjective = value
        if (palLanguage == null) {
            WalkthroughTextFinder.language(value)?.let(onLanguageDetected)
        }
    }

    companion object {
        private const val TAG = "DW2003MemoryPoller"
        private const val POLL_INTERVAL_MS = 200L
        private const val OVERLAY_SCAN_BASE = 0x80000
        private const val OVERLAY_SCAN_LENGTH = 0x40000
        private const val RAM_MASK = 0x1FFFFF
    }
}
