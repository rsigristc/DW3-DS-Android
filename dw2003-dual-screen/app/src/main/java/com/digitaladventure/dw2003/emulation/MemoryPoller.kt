package com.digitaladventure.dw2003.emulation

import android.util.Log
import com.digitaladventure.dw2003.data.DwTextDecoder
import com.digitaladventure.dw2003.data.GameStateReader
import com.digitaladventure.dw2003.data.GameStateRepository
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
    private val scope: CoroutineScope
) {
    private val reader = GameStateReader()
    private var job: Job? = null

    fun start() {
        if (job != null) return
        job = scope.launch(Dispatchers.Default) {
            while (isActive) {
                try {
                    val main = read(GameStateReader.MAIN_BASE, GameStateReader.MAIN_LENGTH)
                    val overlay = read(GameStateReader.OVERLAY_BASE, 4)
                    val signature = GameStateReader.u32(overlay, 0)
                    repository.publish(reader.parse(main, signature, readObjective()))
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

    private fun readObjective(): String? {
        val scratch = read(GameStateReader.SCRATCH_BASE, GameStateReader.SCRATCH_LENGTH)
        return listOf(0x08, 0x0C, 0x10)
            .map { GameStateReader.u32(scratch, it) }
            .filter { it in 0x80000000L..0x801FFFFFL }
            .mapNotNull { pointer ->
                val decoded = DwTextDecoder.decode(read((pointer and 0x1FFFFF).toInt(), 256))
                decoded.takeIf { text -> text.count(Char::isLetter) >= 4 }
            }
            .maxByOrNull(String::length)
    }

    companion object {
        private const val TAG = "DW2003MemoryPoller"
        private const val POLL_INTERVAL_MS = 350L
    }
}
