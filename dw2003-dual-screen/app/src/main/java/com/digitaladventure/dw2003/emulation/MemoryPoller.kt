package com.digitaladventure.dw2003.emulation

import android.util.Log
import com.digitaladventure.dw2003.data.AreaCatalog
import com.digitaladventure.dw2003.data.CompanionLanguage
import com.digitaladventure.dw2003.data.GameStateReader
import com.digitaladventure.dw2003.data.GameStateRepository
import com.digitaladventure.dw2003.data.PalLanguage
import com.digitaladventure.dw2003.data.CompanionRomFeatures
import com.digitaladventure.dw2003.data.WalkthroughTextFinder
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
    private val onLanguageDetected: (CompanionLanguage) -> Unit = {}
) {
    private val reader = GameStateReader()
    private var job: Job? = null
    private var cachedObjective: String? = null
    private var cachedStoryStage = -1
    private var cachedMapId = -1
    private var lastSignature = 0L
    private var overlayScanAttempts = 0
    private var fullScanAttempts = 0
    private var pollCount = 0

    fun start() {
        if (job != null) return
        job = scope.launch(Dispatchers.Default) {
            while (isActive) {
                try {
                    val main = read(GameStateReader.MAIN_BASE, GameStateReader.MAIN_LENGTH)
                    val overlay = read(GameStateReader.OVERLAY_BASE, 4)
                    val signature = GameStateReader.u32(overlay, 0)
                    val storyStage = GameStateReader.u16(
                        main,
                        GameStateReader.STORY_STAGE - GameStateReader.MAIN_BASE
                    )
                    val areaId = GameStateReader.u16(main, GameStateReader.AREA - GameStateReader.MAIN_BASE)
                    val mapId = GameStateReader.u16(main, GameStateReader.MAP_ID - GameStateReader.MAIN_BASE)
                    val palLanguage = if (features.detectPalLanguage) {
                        PalLanguage.companionLanguage(
                            GameStateReader.u32(
                                read(GameStateReader.PAL_LANGUAGE and RAM_MASK, 4),
                                0
                            ).toInt()
                        )
                    } else {
                        null
                    }
                    palLanguage?.let(onLanguageDetected)
                    val objective = if (features.supportsWalkthrough) {
                        readObjective(signature, storyStage, mapId, areaId, palLanguage)
                    } else {
                        null
                    }
                    repository.publish(reader.parse(main, signature, objective, features))
                } catch (error: Exception) {
                    Log.d(TAG, "RAM not ready yet", error)
                }
                delay(POLL_INTERVAL_MS)
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
    }

    private fun read(offset: Int, length: Int): ByteArray =
        view.readMemory(LibretroDroid.MEMORY_SYSTEM_RAM, offset, length)

    private fun readObjective(
        signature: Long,
        storyStage: Int,
        mapId: Int,
        areaId: Int,
        palLanguage: CompanionLanguage?
    ): String? {
        val inBattle = signature == GameStateReader.FIGHTST2_SIGNATURE
        val menuOpen = signature == GameStateReader.STSTATUS_SIGNATURE
        if (storyStage != cachedStoryStage || mapId != cachedMapId) {
            cachedStoryStage = storyStage
            cachedMapId = mapId
            cachedObjective = null
            overlayScanAttempts = 0
            fullScanAttempts = 0
        }
        if (signature != lastSignature) {
            lastSignature = signature
            overlayScanAttempts = 0
            fullScanAttempts = 0
        }

        val locationWords = WalkthroughTextFinder.locationKeywords(
            AreaCatalog.name(mapId),
            AreaCatalog.name(areaId)
        )
        val candidates = mutableListOf<String>()
        val scratch = read(GameStateReader.SCRATCH_BASE, GameStateReader.SCRATCH_LENGTH)
        WalkthroughTextFinder.decodeWindow(scratch)?.let(candidates::add)
        WalkthroughTextFinder.best(
            WalkthroughTextFinder.pointers(scratch).mapNotNull { pointer ->
                WalkthroughTextFinder.decodeWindow(
                    read((pointer and RAM_MASK.toLong()).toInt(), POINTER_WINDOW)
                )
            }
        )?.let(candidates::add)

        pollCount++
        val refreshOverlay = !inBattle && (
            menuOpen ||
                overlayScanAttempts < 4 ||
                pollCount % 4 == 0 ||
                cachedObjective == null
            )
        if (refreshOverlay) {
            if (!menuOpen && overlayScanAttempts < 4) overlayScanAttempts++
            WalkthroughTextFinder.find(read(OVERLAY_SCAN_BASE, OVERLAY_SCAN_LENGTH))
                ?.let(candidates::add)
        }
        if (menuOpen && candidates.isEmpty() && fullScanAttempts < MAX_FULL_SCAN_ATTEMPTS) {
            fullScanAttempts++
            WalkthroughTextFinder.find(read(0, SYSTEM_RAM_SIZE))?.let(candidates::add)
        }

        val best = candidates.maxByOrNull { WalkthroughTextFinder.score(it, false, locationWords) }
        if (best != null && WalkthroughTextFinder.score(best, false, locationWords) > Int.MIN_VALUE) {
            cacheObjective(best, palLanguage)
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
        private const val POLL_INTERVAL_MS = 350L
        private const val SYSTEM_RAM_SIZE = 0x200000
        private const val OVERLAY_SCAN_BASE = 0x80000
        private const val OVERLAY_SCAN_LENGTH = 0x40000
        private const val POINTER_WINDOW = 256
        private const val MAX_FULL_SCAN_ATTEMPTS = 3
        private const val RAM_MASK = 0x1FFFFF
    }
}
