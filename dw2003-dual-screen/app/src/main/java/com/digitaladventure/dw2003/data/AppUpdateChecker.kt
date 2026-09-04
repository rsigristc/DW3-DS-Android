package com.digitaladventure.dw2003.data

import org.json.JSONObject
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
        val root = runCatching { JSONObject(json) }.getOrNull() ?: return null
        val tag = root.optString("tag_name")
        if (tag.isBlank()) return null
        val assets = root.optJSONArray("assets") ?: return null
        val apk = (0 until assets.length())
            .map { assets.optJSONObject(it) }
            .firstOrNull { item ->
                item?.optString("name").orEmpty().endsWith(".apk", ignoreCase = true)
            } ?: return null
        val apkUrl = apk.optString("browser_download_url")
        if (apkUrl.isBlank()) return null
        return AppRelease(
            tag = tag,
            name = root.optString("name").ifBlank { tag },
            apkUrl = apkUrl,
            htmlUrl = root.optString("html_url")
        )
    }

    companion object {
        const val LATEST_RELEASE = "https://api.github.com/repos/rsigristc/DW3-DS-Android/releases/latest"
        const val RELEASES_PAGE = "https://github.com/rsigristc/DW3-DS-Android/releases"
    }
}
