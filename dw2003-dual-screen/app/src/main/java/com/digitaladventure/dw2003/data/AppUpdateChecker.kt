package com.digitaladventure.dw2003.data

import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

data class AppRelease(
    val tag: String,
    val name: String,
    val apkUrl: String,
    val htmlUrl: String,
    val changelog: String = ""
)

sealed class AppUpdateStatus {
    data class Available(val release: AppRelease) : AppUpdateStatus()
    data class Current(val installed: String, val remoteTag: String) : AppUpdateStatus()
    data class Unavailable(val reason: String) : AppUpdateStatus()
}

class AppUpdateChecker(
    private val endpoint: String = LATEST_RELEASE
) {
    fun check(installed: String): AppUpdateStatus {
        val release = requestLatest().getOrElse { error ->
            return AppUpdateStatus.Unavailable(error.message ?: error.javaClass.simpleName)
        }
        return if (AppVersion.isNewer(release.tag, installed)) {
            AppUpdateStatus.Available(release)
        } else {
            AppUpdateStatus.Current(installed, release.tag)
        }
    }

    fun latestNewerThan(installed: String): AppRelease? =
        (check(installed) as? AppUpdateStatus.Available)?.release

    fun fetchLatest(): AppRelease? = requestLatest().getOrNull()

    fun parse(json: String): AppRelease? = runCatching {
        val root = JSONObject(json)
        val tag = root.optString("tag_name").takeIf { it.isNotBlank() } ?: return null
        val assets = root.optJSONArray("assets")
        val apkUrl = (0 until (assets?.length() ?: 0)).asSequence()
            .mapNotNull { assets?.optJSONObject(it)?.optString("browser_download_url") }
            .firstOrNull { it.endsWith(".apk", ignoreCase = true) }
            ?: root.optString("browser_download_url").takeIf { it.endsWith(".apk", ignoreCase = true) }
            ?: return null
        AppRelease(
            tag = tag,
            name = root.optString("name").ifBlank { tag },
            apkUrl = apkUrl,
            htmlUrl = root.optString("html_url"),
            changelog = if (root.isNull("body")) "" else root.optString("body").trim()
        )
    }.getOrNull()

    private fun requestLatest(): Result<AppRelease> {
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            connectTimeout = 12_000
            readTimeout = 12_000
            instanceFollowRedirects = true
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("User-Agent", "DW2003-Dual-Screen")
        }
        return try {
            val code = connection.responseCode
            if (code == HttpURLConnection.HTTP_NOT_FOUND) {
                return Result.failure(
                    IOException("El repositorio de GitHub es privado o no publica releases")
                )
            }
            if (code !in 200..299) {
                return Result.failure(IOException("GitHub HTTP $code"))
            }
            val parsed = parse(connection.inputStream.bufferedReader().readText())
                ?: return Result.failure(IOException("GitHub no publicó un APK en el último release"))
            Result.success(parsed)
        } catch (error: Exception) {
            Result.failure(error)
        } finally {
            connection.disconnect()
        }
    }

    companion object {
        const val LATEST_RELEASE = "https://api.github.com/repos/rsigristc/DW3-DS-Android/releases/latest"
        const val RELEASES_PAGE = "https://github.com/rsigristc/DW3-DS-Android/releases"
    }
}
