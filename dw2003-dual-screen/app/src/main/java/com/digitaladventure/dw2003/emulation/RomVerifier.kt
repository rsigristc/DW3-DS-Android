package com.digitaladventure.dw2003.emulation

import android.content.ContentResolver
import android.net.Uri
import java.security.MessageDigest

object RomVerifier {
    const val ORIGINAL_SHA1 = "457cb233349ba841e03b33d8060f8fbcadd45cb3"
    const val FLAWES_MOD_2_SHA1 = "5277dfd1b7b6b237ea93bfca2723c9b4baaa75d1"

    enum class Variant(val label: String) {
        ORIGINAL("Digimon World 2003 Europa"),
        FLAWES_MOD_2("Flawe's Mod 2.0 combinado"),
        UNKNOWN("Imagen no verificada")
    }

    data class Result(val variant: Variant, val sha1: String)

    fun verify(resolver: ContentResolver, uri: Uri): Result {
        val digest = MessageDigest.getInstance("SHA-1")
        resolver.openInputStream(uri).use { input ->
            requireNotNull(input) { "No se pudo abrir el archivo" }
            val buffer = ByteArray(1024 * 1024)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                digest.update(buffer, 0, count)
            }
        }
        val hash = digest.digest().joinToString("") { "%02x".format(it) }
        val variant = when (hash) {
            ORIGINAL_SHA1 -> Variant.ORIGINAL
            FLAWES_MOD_2_SHA1 -> Variant.FLAWES_MOD_2
            else -> Variant.UNKNOWN
        }
        return Result(variant, hash)
    }
}
