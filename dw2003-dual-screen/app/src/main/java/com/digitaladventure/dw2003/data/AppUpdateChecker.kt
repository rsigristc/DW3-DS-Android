package com.digitaladventure.dw2003.data

import java.net.HttpURLConnection
import java.net.URL

data class AppRelease(
    val tag: String,
    val name: String,
    val apkUrl: String,
    val htmlUrl: String
)

class AppUpdateChecker(
    private val endpoint: String = LATEST_RELEASE
) {
    fun latestNewerThan(installed: String): AppRelease? {
        val release = fetchLatest() ?: return null
        return release.takeIf { AppVersion.isNewer(it.tag, installed) }
    }

    fun fetchLatest(): AppRelease? {
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            connectTimeout = 12_000
            readTimeout = 12_000
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("User-Agent", "DW2003-Dual-Screen")
        }
        return try {
            if (connection.responseCode !in 200..299) return null
            parse(connection.inputStream.bufferedReader().readText())
        } finally {
            connection.disconnect()
        }
    }

    fun parse(json: String): AppRelease? {
        val tag = stringField(json, "tag_name") ?: return null
        val apkUrl = Regex(""""browser_download_url"\s*:\s*"(https?://[^"]+\.apk)"""", RegexOption.IGNORE_CASE)
            .find(json)
            ?.groupValues
            ?.get(1)
            ?: return null
        return AppRelease(
            tag = tag,
            name = stringField(json, "name") ?: tag,
            apkUrl = apkUrl,
            htmlUrl = stringField(json, "html_url").orEmpty()
        )
    }

    private fun stringField(json: String, key: String): String? =
        Regex(""""$key"\s*:\s*"([^"]+)"""").find(json)?.groupValues?.get(1)?.takeIf { it.isNotBlank() }

    companion object {
        const val LATEST_RELEASE = "https://api.github.com/repos/rsigristc/DW3-DS-Android/releases/latest"
        const val RELEASES_PAGE = "https://github.com/rsigristc/DW3-DS-Android/releases"
    }
}
