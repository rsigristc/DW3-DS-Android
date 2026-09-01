package com.digitaladventure.dw2003.emulation

import android.content.Context
import com.swordfish.libretrodroid.GLRetroView
import java.io.File
import java.io.FileOutputStream

class QuickStateManager(context: Context, romKey: String) {
    private val directory = File(context.filesDir, "states").apply { mkdirs() }
    private val stateFile = File(directory, "dw2003-${safeKey(romKey)}.state")
    private val sramFile = File(directory, "dw2003-${safeKey(romKey)}.state.srm")

    val hasState: Boolean
        get() = stateFile.isFile && stateFile.length() > 0 &&
            sramFile.isFile && AppFileRules.isValidMemoryCardSize(sramFile.length())

    val hasLegacyState: Boolean
        get() = stateFile.isFile && stateFile.length() > 0 && !sramFile.isFile

    @Synchronized
    fun save(view: GLRetroView): SaveResult {
        val payload = view.serializeState()
        val sram = view.serializeSRAM()
        require(payload.isNotEmpty()) { "El núcleo aún no está listo para guardar" }
        require(AppFileRules.hasMemoryCardSignature(sram)) { "La Memory Card del núcleo no es válida" }
        atomicWrite(stateFile, payload)
        atomicWrite(sramFile, sram)
        return SaveResult(stateFile.length(), sram)
    }

    @Synchronized
    fun load(view: GLRetroView): LoadResult? {
        if (hasLegacyState) {
            throw IllegalStateException("Este estado fue creado sin una Memory Card emparejada. Crea un estado nuevo con la versión 0.6.")
        }
        if (!hasState) return null
        val state = stateFile.readBytes()
        val sram = sramFile.readBytes()
        require(AppFileRules.hasMemoryCardSignature(sram)) { "La Memory Card emparejada está dañada" }
        if (!view.unserializeState(state)) return null
        if (!view.unserializeSRAM(sram)) return null
        return LoadResult(sram)
    }

    private fun atomicWrite(destination: File, payload: ByteArray) {
        val temporary = File(directory, "${destination.name}.tmp")
        FileOutputStream(temporary).use { output ->
            output.write(payload)
            output.fd.sync()
        }
        if (!temporary.renameTo(destination)) {
            temporary.copyTo(destination, overwrite = true)
            temporary.delete()
        }
    }

    data class LoadResult(val memoryCard: ByteArray)
    data class SaveResult(val stateBytes: Long, val memoryCard: ByteArray)

    companion object {
        internal fun safeKey(value: String): String = value
            .lowercase()
            .filter(Char::isLetterOrDigit)
            .take(40)
            .ifEmpty { "rom-desconocida" }
    }
}
