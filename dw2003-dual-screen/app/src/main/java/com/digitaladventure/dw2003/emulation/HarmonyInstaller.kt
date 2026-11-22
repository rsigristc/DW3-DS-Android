package com.digitaladventure.dw2003.emulation

import com.digitaladventure.dw2003.data.AppVersion
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

class HarmonyInstaller(private val directory: File) {
    data class Release(val tag: String, val notes: String, val patch: String, val manifest: String, val localizedNotes: String?)
    data class Installed(val file: File, val sha1: String)

    fun latest(spanish: Boolean): Release {
        val releases = JSONArray(readText("https://api.github.com/repos/rsigristc/dw2003-qol-patch/releases"))
        return latestInstallable(releases, spanish)
            ?: throw IOException("GitHub no tiene un parche instalable / GitHub has no installable patch")
    }

    fun notes(release: Release): String =
        release.localizedNotes?.let { runCatching { readText(it) }.getOrNull() } ?: release.notes

    companion object {
        private const val ORIGINAL_SHA256 = "fb70dc9a995aed628cf515cabc87c7b14e5142559076ec394dfe793ec3e26a04"
        private val versionTag = Regex("""harmony-(?:beta)?v?\d{1,6}(?:\.\d{1,6})*""")

        internal fun isNewer(remote: String, installed: String): Boolean {
            if (!versionTag.matches(remote) || !versionTag.matches(installed)) return false
            if (AppVersion.isNewer(remote, installed)) return true
            if (AppVersion.isNewer(installed, remote)) return false
            return !remote.startsWith("harmony-beta") && installed.startsWith("harmony-beta")
        }

        internal fun latestInstallable(releases: JSONArray, spanish: Boolean): Release? {
            var latest: Release? = null
            for (index in 0 until releases.length()) {
                val release = parseRelease(releases.optJSONObject(index) ?: continue, spanish) ?: continue
                if (latest == null || isNewer(release.tag, latest.tag)) latest = release
            }
            return latest
        }

        private fun parseRelease(root: JSONObject, spanish: Boolean): Release? {
            val tag = root.optString("tag_name")
            if (root.optBoolean("draft") || !versionTag.matches(tag)) return null
            val assets = root.optJSONArray("assets") ?: return null
            val urls = (0 until assets.length()).mapNotNull { index ->
                val asset = assets.optJSONObject(index) ?: return@mapNotNull null
                val name = asset.optString("name")
                val url = asset.optString("browser_download_url")
                val expectedUrl = "https://github.com/rsigristc/dw2003-qol-patch/releases/download/$tag/$name"
                if (url == expectedUrl) name to url else null
            }.toMap()
            val patch = urls.keys.singleOrNull {
                it.startsWith("dw2003-harmony-") && it.endsWith(".bps") && it.removeSuffix(".bps") + ".json" in urls
            } ?: return null
            return Release(
                tag, if (root.isNull("body")) "" else root.optString("body"), urls.getValue(patch),
                urls.getValue(patch.removeSuffix(".bps") + ".json"),
                urls[if (spanish) "CHANGELOG.es.md" else "CHANGELOG.en.md"]
            )
        }
    }

    fun install(release: Release, openSource: () -> InputStream, progress: (Int, Int) -> Unit): Installed {
        val manifest = JSONObject(readText(release.manifest))
        require(manifest.getString("format") == "BPS")
        val sourceInfo = manifest.getJSONObject("source")
        val targetInfo = manifest.getJSONObject("target")
        val sourceSize = sourceInfo.getLong("size")
        val targetSize = targetInfo.getLong("size")
        val patchSize = manifest.getInt("patch_size")
        val targetHash = targetInfo.getString("sha256")
        require(sourceSize == 692146560L && sourceInfo.getString("sha256") == ORIGINAL_SHA256)
        require(targetSize in 1..1_100_000_000 && patchSize in 1..32_000_000)
        require(targetHash.matches(Regex("[0-9a-f]{64}")))
        check(directory.isDirectory || directory.mkdirs())
        val result = File(directory, "Harmony-${targetHash.take(16)}.bin")
        val source = File.createTempFile("source-", ".tmp", directory)
        val output = try {
            File.createTempFile("target-", ".tmp", directory)
        } catch (error: Exception) {
            source.delete()
            throw error
        }
        fun report(stage: Int, bytes: Long, total: Long) = progress(stage, (bytes * 100 / total).toInt())
        try {
            val patch = download(release.patch) { input ->
                val data = ByteArray(patchSize)
                var offset = 0
                while (offset < data.size) {
                    val read = input.read(data, offset, minOf(65536, data.size - offset))
                    if (read < 0) throw IOException("Descarga incompleta / Incomplete download")
                    offset += read
                    report(0, offset.toLong(), patchSize.toLong())
                }
                require(input.read() == -1)
                data
            }
            require(hex(MessageDigest.getInstance("SHA-256").digest(patch)) == manifest.getString("patch_sha256")) {
                "SHA-256 del parche incorrecto / Patch SHA-256 mismatch"
            }
            if (directory.usableSpace < sourceSize + targetSize + 32 * 1024 * 1024) {
                throw IOException("Se necesitan ~1.4 GB libres / About 1.4 GB of free space required")
            }
            val sourceHash = MessageDigest.getInstance("SHA-256")
            val buffer = ByteArray(256 * 1024)
            var copied = 0L
            openSource().buffered().use { input ->
                source.outputStream().use { sink ->
                    while (true) {
                        val count = input.read(buffer)
                        if (count < 0) break
                        copied += count
                        if (copied > sourceSize) break
                        sink.write(buffer, 0, count)
                        sourceHash.update(buffer, 0, count)
                        report(1, copied, sourceSize)
                    }
                }
            }
            require(copied == sourceSize && hex(sourceHash.digest()) == ORIGINAL_SHA256) {
                "Selecciona el BIN PAL original SLES-03936, sin parches / Select the clean SLES-03936 PAL BIN"
            }
            BpsPatch.apply(patch, source, output, targetSize) { report(2, it, targetSize) }
            val sha256 = MessageDigest.getInstance("SHA-256")
            val sha1 = MessageDigest.getInstance("SHA-1")
            var checked = 0L
            output.inputStream().use { input ->
                while (true) {
                    val count = input.read(buffer)
                    if (count < 0) break
                    sha256.update(buffer, 0, count)
                    sha1.update(buffer, 0, count)
                    checked += count
                    report(3, checked, targetSize)
                }
            }
            require(hex(sha256.digest()) == targetHash) { "SHA-256 del BIN incorrecto / Output SHA-256 mismatch" }
            check(output.renameTo(result)) { "No se pudo guardar Harmony / Could not save Harmony" }
            return Installed(result, hex(sha1.digest()))
        } finally {
            source.delete()
            output.delete()
        }
    }

    private fun readText(url: String) = download(url) { input ->
        val bytes = input.readNBytesCompat(1024 * 1024)
        require(bytes.size < 1024 * 1024)
        bytes.toString(Charsets.UTF_8)
    }

    private fun InputStream.readNBytesCompat(limit: Int): ByteArray {
        val output = java.io.ByteArrayOutputStream()
        val buffer = ByteArray(8192)
        while (output.size() < limit) {
            val count = read(buffer, 0, minOf(buffer.size, limit - output.size()))
            if (count < 0) break
            output.write(buffer, 0, count)
        }
        return output.toByteArray()
    }

    private fun <T> download(url: String, read: (InputStream) -> T): T {
        val endpoint = URL(url)
        require(endpoint.protocol == "https" && endpoint.host in setOf("api.github.com", "github.com"))
        if (endpoint.host == "github.com") require(endpoint.path.startsWith("/rsigristc/dw2003-qol-patch/releases/download/"))
        val connection = endpoint.openConnection() as HttpURLConnection
        connection.connectTimeout = 15_000
        connection.readTimeout = 25_000
        connection.setRequestProperty("User-Agent", "DW2003-Dual-Screen")
        try {
            if (connection.responseCode !in 200..299) throw IOException("GitHub HTTP ${connection.responseCode}")
            return connection.inputStream.buffered().use(read)
        } finally {
            connection.disconnect()
        }
    }

    private fun hex(bytes: ByteArray): String = bytes.joinToString("") { "%02x".format(it) }

}
