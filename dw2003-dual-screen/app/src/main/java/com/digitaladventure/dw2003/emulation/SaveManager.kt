package com.digitaladventure.dw2003.emulation

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log
import com.swordfish.libretrodroid.GLRetroView
import java.io.File
import java.io.FileOutputStream

class SaveManager(context: Context) {
    private val saveDirectory = File(context.filesDir, "saves").apply { mkdirs() }
    private val memoryCard = File(saveDirectory, "dw2003-memory-card.srm")
    private val backupCard = File(saveDirectory, "dw2003-memory-card.backup.srm")

    init {
        ensureInstalled()
    }

    val hasSave: Boolean
        get() = memoryCard.isFile && AppFileRules.isValidMemoryCardSize(memoryCard.length())

    val sizeBytes: Long
        get() = memoryCard.takeIf(File::isFile)?.length() ?: 0L

    fun load(): ByteArray = memoryCard.readBytes()

    @Synchronized
    fun ensureInstalled(): Boolean {
        val current = memoryCard.takeIf(File::isFile)?.runCatching(File::readBytes)?.getOrNull()
        if (current != null && AppFileRules.hasMemoryCardSignature(current)) return false
        if (memoryCard.isFile) {
            memoryCard.copyTo(File(saveDirectory, "dw2003-memory-card.invalid.srm"), overwrite = true)
        }
        val temporary = File(saveDirectory, "dw2003-memory-card.install.tmp")
        temporary.writeBytes(MemoryCardFactory.createFormatted())
        if (!temporary.renameTo(memoryCard)) {
            temporary.copyTo(memoryCard, overwrite = true)
            temporary.delete()
        }
        return true
    }

    @Synchronized
    fun save(view: GLRetroView) {
        runCatching {
            val data = view.serializeSRAM()
            if (data.isEmpty()) return
            persistSnapshot(data)
        }.onFailure { Log.e(TAG, "No se pudo guardar la tarjeta", it) }
    }

    /** Installs the card paired with a loaded quick state and persists it for the next boot. */
    @Synchronized
    fun persistSnapshot(data: ByteArray) {
        require(AppFileRules.hasMemoryCardSignature(data)) { "Memory Card de estado inválida" }
        if (memoryCard.isFile && !memoryCard.readBytes().contentEquals(data)) {
            memoryCard.copyTo(backupCard, overwrite = true)
        }
        val temporary = File(saveDirectory, "dw2003-memory-card.tmp")
        FileOutputStream(temporary).use { output ->
            output.write(data)
            output.fd.sync()
        }
        if (!temporary.renameTo(memoryCard)) {
            temporary.copyTo(memoryCard, overwrite = true)
            temporary.delete()
        }
    }

    @Synchronized
    fun importCard(resolver: ContentResolver, uri: Uri): Long {
        val temporary = File(saveDirectory, "dw2003-memory-card.import.tmp")
        temporary.delete()
        resolver.openInputStream(uri).use { input ->
            requireNotNull(input) { "No se pudo abrir la Memory Card" }
            temporary.outputStream().use(input::copyTo)
        }

        val payloadOffset = AppFileRules.memoryCardPayloadOffset(temporary.length())
        if (payloadOffset == null) {
            temporary.delete()
            throw IllegalArgumentException("Formato no reconocido: usa SRM/MCR, VGS/MEM o GME")
        }
        if (payloadOffset > 0) {
            val normalized = temporary.readBytes().copyOfRange(
                payloadOffset,
                payloadOffset + AppFileRules.PLAYSTATION_MEMORY_CARD_SIZE.toInt()
            )
            temporary.writeBytes(normalized)
        }
        if (!AppFileRules.hasMemoryCardSignature(temporary.readBytes())) {
            temporary.delete()
            throw IllegalArgumentException("La tarjeta no contiene una cabecera PlayStation válida")
        }

        if (memoryCard.isFile) memoryCard.copyTo(backupCard, overwrite = true)
        if (!temporary.renameTo(memoryCard)) {
            temporary.copyTo(memoryCard, overwrite = true)
            temporary.delete()
        }
        return memoryCard.length()
    }

    @Synchronized
    fun exportCard(resolver: ContentResolver, uri: Uri): Long {
        require(hasSave) { "Todavía no existe una Memory Card válida" }
        resolver.openOutputStream(uri, "wt").use { output ->
            requireNotNull(output) { "No se pudo crear el archivo de destino" }
            memoryCard.inputStream().use { it.copyTo(output) }
        }
        return memoryCard.length()
    }

    companion object {
        private const val TAG = "DW2003SaveManager"
    }
}
