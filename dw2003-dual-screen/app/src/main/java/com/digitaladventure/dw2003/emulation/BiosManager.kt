package com.digitaladventure.dw2003.emulation

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import java.io.File
import java.security.MessageDigest

class BiosManager(context: Context) {
    val systemDirectory: File = File(context.filesDir, "system").apply { mkdirs() }
    private val biosFile = File(systemDirectory, BIOS_FILE_NAME)
    private val backupFile = File(systemDirectory, "$BIOS_FILE_NAME.backup")

    val isInstalled: Boolean
        get() = biosFile.isFile && AppFileRules.isValidBiosSize(biosFile.length())

    fun importBios(resolver: ContentResolver, uri: Uri): Result {
        val temporary = File(systemDirectory, "$BIOS_FILE_NAME.tmp")
        temporary.delete()
        resolver.openInputStream(uri).use { input ->
            requireNotNull(input) { "No se pudo abrir el BIOS" }
            temporary.outputStream().use(input::copyTo)
        }

        if (!AppFileRules.isEuropeanPlayStationBios(temporary.readBytes())) {
            temporary.delete()
            throw IllegalArgumentException("El archivo no parece un BIOS europeo de PlayStation de 512 KiB")
        }

        if (biosFile.isFile) biosFile.copyTo(backupFile, overwrite = true)
        replaceAtomically(temporary, biosFile)
        return Result(biosFile.length(), sha1(biosFile))
    }

    private fun replaceAtomically(temporary: File, destination: File) {
        if (!temporary.renameTo(destination)) {
            temporary.copyTo(destination, overwrite = true)
            temporary.delete()
        }
    }

    private fun sha1(file: File): String {
        val digest = MessageDigest.getInstance("SHA-1")
        file.inputStream().use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                digest.update(buffer, 0, count)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    data class Result(val sizeBytes: Long, val sha1: String)

    companion object {
        const val BIOS_FILE_NAME = "scph5502.bin"
    }
}
