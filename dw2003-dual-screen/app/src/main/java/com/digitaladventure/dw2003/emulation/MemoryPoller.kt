package com.digitaladventure.dw2003.emulation

import android.util.Log
import android.os.SystemClock
import com.digitaladventure.dw2003.data.OverlayScanSchedule
import com.digitaladventure.dw2003.data.CompanionLanguage
import com.digitaladventure.dw2003.data.GameStateReader
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
    private val objectiveLanguageOverride: () -> Int? = { null }
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
        val signature = GameStateReader.u32(overlay, 0)
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
        var overlayStageId: Int? = null
        if (!stopped && overlayScans.shouldScan(locationKey, signature, SystemClock.elapsedRealtime())) {
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
        repository.publish(
            reader.parse(main, signature, objective, features, overlayStageId)
        )
    }

    private fun read(offset: Int, length: Int): ByteArray {
        check(!stopped) { "Memory poller stopped" }
        return view.readMemory(LibretroDroid.MEMORY_SYSTEM_RAM, offset, length)
    }

    private fun readObjective(
        signature: Long,
        palLanguage: CompanionLanguage?
    ): String? {
        if (signature != GameStateReader.FIGHTST2_SIGNATURE) {
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
