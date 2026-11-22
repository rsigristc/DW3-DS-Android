package com.digitaladventure.dw2003.emulation

import android.content.ContentResolver
import android.net.Uri
import com.digitaladventure.dw2003.data.CompanionRomFeatures
import java.io.InputStream
import java.security.MessageDigest

object RomVerifier {
    const val ORIGINAL_SHA1 = "457cb233349ba841e03b33d8060f8fbcadd45cb3"
    const val FLAWES_MOD_2_SHA1 = "5277dfd1b7b6b237ea93bfca2723c9b4baaa75d1"
    const val USA_SHA1 = "f0b022f9be53cbce14640abd8f01beaadcb35208"

    const val HARMONY_PUBLIC_SHA1 = "297a02a7e594e1995a755f1de45c1633c28a2ea0"
    const val HARMONY_TERRIERMON_SHA1 = "229fd42f63c1173dd497b02fc5e1419b2c44615b"
    const val HARMONY_SEVEN_LANGUAGES_SHA1 = "1e6863b5b2ca4ca247d999a8ed58a95e892f879c"
    const val HARMONY_BETA_2_1_SHA1 = "2f65262417fafde25d775119ce46c995fde7c9dd"

    fun harmonyReleaseTag(sha1: String?): String? = when (sha1) {
        HARMONY_PUBLIC_SHA1 -> "harmony-beta1.5"
        HARMONY_SEVEN_LANGUAGES_SHA1 -> "harmony-beta2.0"
        HARMONY_BETA_2_1_SHA1 -> "harmony-beta2.1"
        else -> null
    }

    const val SERIAL_PAL = "SLES_039.36"
    const val SERIAL_USA = "SLUS_014.36"

    enum class Variant(val label: String) {
        ORIGINAL("Digimon World 2003 Europa"),
        FLAWES_MOD_2("Flawe's Mod 2.0 combinado"),
        USA("Digimon World 3 USA"),
        HARMONY("Digimon World 2003: Harmony"),
        UNKNOWN("Imagen no verificada");

        val features: CompanionRomFeatures
            get() = if (this == USA) CompanionRomFeatures.USA else CompanionRomFeatures.PAL

        val emulatorRegion: String
            get() = if (this == USA) "NTSC" else "PAL"

        companion object {
            fun fromStored(nameOrLabel: String?, sha1: String?): Variant {
                identify(sha1.orEmpty()).takeIf { it != UNKNOWN }?.let { return it }
                return entries.firstOrNull { it.name == nameOrLabel || it.label == nameOrLabel }
                    ?: UNKNOWN
            }
        }
    }

    data class Result(
        val variant: Variant,
        val sha1: String,
        val serial: String? = null
    )

    fun verify(resolver: ContentResolver, uri: Uri): Result {
        resolver.openInputStream(uri).use { input ->
            requireNotNull(input) { "No se pudo abrir el archivo" }
            return inspect(input)
        }
    }

    fun inspect(input: InputStream): Result {
        val digest = MessageDigest.getInstance("SHA-1")
        val overlap = ByteArray(SERIAL_OVERLAP)
        var overlapSize = 0
        var serial: String? = null
        val buffer = ByteArray(1024 * 1024)
        while (true) {
            val count = input.read(buffer)
            if (count < 0) break
            digest.update(buffer, 0, count)
            if (serial == null) {
                serial = findSerial(joinOverlap(overlap, overlapSize, buffer, count))
                val keep = minOf(SERIAL_OVERLAP, count)
                buffer.copyInto(overlap, 0, count - keep, count)
                overlapSize = keep
            }
        }
        val hash = digest.digest().joinToString("") { "%02x".format(it) }
        return Result(identify(hash, serial), hash, serial)
    }

    fun identify(sha1: String, serial: String? = null): Variant {
        val normalized = normalizeSerial(serial)
        return when {
            harmonyReleaseTag(sha1) != null || sha1 == HARMONY_TERRIERMON_SHA1 -> Variant.HARMONY
            sha1 == ORIGINAL_SHA1 -> Variant.ORIGINAL
            sha1 == FLAWES_MOD_2_SHA1 -> Variant.FLAWES_MOD_2
            sha1 == USA_SHA1 || normalized == SERIAL_USA -> Variant.USA
            else -> Variant.UNKNOWN
        }
    }

    fun findSerial(bytes: ByteArray): String? {
        var index = 0
        while (index <= bytes.size - 10) {
            val first = bytes[index].toInt().toChar()
            if (first != 'S' && first != 's') {
                index++
                continue
            }
            val window = bytes.copyOfRange(index, minOf(bytes.size, index + 12))
                .toString(Charsets.US_ASCII)
            normalizeSerial(SERIAL_REGEX.find(window)?.value)?.let { return it }
            index++
        }
        return null
    }

    fun normalizeSerial(raw: String?): String? {
        val match = SERIAL_REGEX.find(raw.orEmpty()) ?: return null
        val region = match.groupValues[1].uppercase()
        if (region != "US" && region != "ES") return null
        return "SL${region}_${match.groupValues[2]}.${match.groupValues[3]}"
    }

    private fun joinOverlap(overlap: ByteArray, overlapSize: Int, buffer: ByteArray, count: Int): ByteArray {
        if (overlapSize == 0) return buffer.copyOf(count)
        return overlap.copyOf(overlapSize) + buffer.copyOf(count)
    }

    private val SERIAL_REGEX = Regex("""SL([UEP][SE])[_-]?(\d{3})\.?(\d{2})""", RegexOption.IGNORE_CASE)
    private const val SERIAL_OVERLAP = 15
}
