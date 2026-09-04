package com.digitaladventure.dw2003.data

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

data class AppRelease(
    val tag: String,
    val name: String,
    val apkUrl: String,
    val htmlUrl: String
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

    fun parse(json: String): AppRelease? {
        val tag = stringField(json, "tag_name") ?: return null
        val apkUrl = Regex(""""browser_download_url"\s*:\s*"([^"]+\.apk)"""", RegexOption.IGNORE_CASE)
            .find(json)
            ?.groupValues
            ?.get(1)
            ?.replace("\\/", "/")
            ?: return null
        return AppRelease(
            tag = tag,
            name = stringField(json, "name") ?: tag,
            apkUrl = apkUrl,
            htmlUrl = stringField(json, "html_url").orEmpty().replace("\\/", "/")
        )
    }

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

    private fun stringField(json: String, key: String): String? =
        Regex(""""$key"\s*:\s*"([^"]+)"""").find(json)?.groupValues?.get(1)?.takeIf { it.isNotBlank() }

    companion object {
        const val LATEST_RELEASE = "https://api.github.com/repos/rsigristc/DW3-DS-Android/releases/latest"
        const val RELEASES_PAGE = "https://github.com/rsigristc/DW3-DS-Android/releases"
    }
}
