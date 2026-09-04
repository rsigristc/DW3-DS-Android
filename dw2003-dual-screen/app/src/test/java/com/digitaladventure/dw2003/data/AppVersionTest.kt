package com.digitaladventure.dw2003.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppVersionTest {
    @Test
    fun detectsNewerGithubTags() {
        assertTrue(AppVersion.isNewer("v0.7.19-poc", "0.7.18-poc-debug"))
        assertTrue(AppVersion.isNewer("v0.7.18-poc", "0.7.17-poc-debug"))
        assertTrue(AppVersion.isNewer("v0.7.17-poc", "0.7.16-poc-debug"))
        assertFalse(AppVersion.isNewer("v0.7.16-poc", "0.7.16-poc-debug"))
        assertFalse(AppVersion.isNewer("v0.7.15-poc", "0.7.16-poc"))
        assertEquals(listOf(0, 7, 16), AppVersion.numericParts("0.7.16-poc-debug"))
    }

    @Test
    fun parsesPublicReleaseJson() {
        val release = AppUpdateChecker().parse(
            """
            {
              "tag_name": "v0.7.18-poc",
              "name": "DW2003 Dual Screen v0.7.18-poc",
              "html_url": "https://github.com/rsigristc/DW3-DS-Android/releases/tag/v0.7.18-poc",
              "assets": [
                {"name": "SHA256SUMS.txt", "browser_download_url": "https://example/SHA256SUMS.txt"},
                {"name": "DW2003-Dual-Screen-v0.7.18-poc-debug.apk", "browser_download_url": "https://example/app.apk"}
              ]
            }
            """.trimIndent()
        )!!
        assertEquals("v0.7.18-poc", release.tag)
        assertEquals("https://example/app.apk", release.apkUrl)
        assertTrue(AppVersion.isNewer(release.tag, "0.7.17-poc"))
    }

    @Test
    fun parsesEscapedDownloadUrls() {
        val release = AppUpdateChecker().parse(
            """{"tag_name":"v0.7.19-poc","browser_download_url":"https:\/\/example\/app.apk"}"""
        )!!
        assertEquals("https://example/app.apk", release.apkUrl)
    }
}
