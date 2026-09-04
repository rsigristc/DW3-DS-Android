package com.digitaladventure.dw2003.emulation

import android.util.Log
import com.digitaladventure.dw2003.data.CompanionLanguage
import com.digitaladventure.dw2003.data.GameStateReader
import com.digitaladventure.dw2003.data.GameStateRepository
import com.digitaladventure.dw2003.data.PalLanguage
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
    private val onLanguageDetected: (CompanionLanguage) -> Unit = {}
) {
    private val reader = GameStateReader()
    private var job: Job? = null
    private var cachedObjective: String? = null
    private var cachedStoryStage = -1
    private var statusMenuOpen = false
    private var fullScanAttempts = 0

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
                    val palLanguage = PalLanguage.companionLanguage(
                        GameStateReader.u32(
                            read(GameStateReader.PAL_LANGUAGE and RAM_MASK, 4),
                            0
                        ).toInt()
                    )
                    palLanguage?.let(onLanguageDetected)
                    repository.publish(
                        reader.parse(main, signature, readObjective(signature, storyStage, palLanguage))
                    )
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

    private fun readObjective(signature: Long, storyStage: Int, palLanguage: CompanionLanguage?): String? {
        if (storyStage != cachedStoryStage) {
            cachedStoryStage = storyStage
            cachedObjective = null
            fullScanAttempts = 0
        }
        val inStatusMenu = signature == GameStateReader.STSTATUS_SIGNATURE
        if (inStatusMenu && !statusMenuOpen) fullScanAttempts = 0
        statusMenuOpen = inStatusMenu

        val scratch = read(GameStateReader.SCRATCH_BASE, GameStateReader.SCRATCH_LENGTH)
        WalkthroughTextFinder.decodeWindow(scratch)?.let { cacheObjective(it, palLanguage) }
        val pointerText = WalkthroughTextFinder.best(
            WalkthroughTextFinder.pointers(scratch).mapNotNull { pointer ->
                WalkthroughTextFinder.decodeWindow(
                    read((pointer and RAM_MASK.toLong()).toInt(), POINTER_WINDOW)
                )
            }
        )
        if (pointerText != null) cacheObjective(pointerText, palLanguage)

        if (inStatusMenu && cachedObjective == null && fullScanAttempts < MAX_FULL_SCAN_ATTEMPTS) {
            fullScanAttempts++
            WalkthroughTextFinder.find(read(OVERLAY_SCAN_BASE, OVERLAY_SCAN_LENGTH))
                ?.let { cacheObjective(it, palLanguage) }
            if (cachedObjective == null) {
                WalkthroughTextFinder.find(read(0, SYSTEM_RAM_SIZE))
                    ?.let { cacheObjective(it, palLanguage) }
            }
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
