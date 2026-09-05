package com.digitaladventure.dw2003.data

import org.junit.Assert.*
import org.junit.Test

class AppUpdateCheckerTest {
    @Test fun decodesChangelogEscapesAndUsesReleaseNameNotAuthorName() {
        val release = AppUpdateChecker().parse("""{
            "author":{"name":"Author"},
            "tag_name":"v1.0.7", "name":"Map fixes",
            "body":"## Changes\r\n- Fixed \"Open Map Tab\"\n- Correcci\u00f3n de rutas C:\\games",
            "assets":[{"browser_download_url":"https:\/\/example.com\/app.apk"}]
        }""")!!
        assertEquals("Map fixes", release.name)
        assertEquals("## Changes\r\n- Fixed \"Open Map Tab\"\n- Corrección de rutas C:\\games", release.changelog)
        assertEquals("https://example.com/app.apk", release.apkUrl)
    }

    @Test fun missingAndNullNotesLeaveUpdateAvailable() {
        for (body in listOf("", ",\"body\":null", ",\"body\":\"\"")) {
            val release = AppUpdateChecker().parse("""{"tag_name":"v1.0.7","assets":[{"browser_download_url":"https://example/app.apk"}]$body}""")!!
            assertEquals("", release.changelog)
        }
    }

    @Test fun malformedJsonOrMissingApkDoesNotProduceAnUpdate() {
        assertNull(AppUpdateChecker().parse("not JSON"))
        assertNull(AppUpdateChecker().parse("""{"tag_name":"v1.0.7","body":"Download app.apk"}"""))
    }
}
