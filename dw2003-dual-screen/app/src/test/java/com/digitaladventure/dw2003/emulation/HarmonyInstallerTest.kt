package com.digitaladventure.dw2003.emulation

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HarmonyInstallerTest {
    @Test fun selectsNewestInstallableHarmonyReleaseIncludingBetas() {
        val releases = JSONArray()
            .put(release("v2026.10.06"))
            .put(release("harmony-beta3.0", hasManifest = false))
            .put(release("harmony-beta4.0").put("draft", true))
            .put(release("harmony-beta2.10").put("prerelease", true))
            .put(release("harmony-beta2.9"))

        val latest = requireNotNull(HarmonyInstaller.latestInstallable(releases, spanish = true))
        assertEquals("harmony-beta2.10", latest.tag)
        assertTrue(latest.patch.endsWith("/dw2003-harmony-beta2.10.bps"))
        assertTrue(latest.manifest.endsWith("/dw2003-harmony-beta2.10.json"))
        assertTrue(requireNotNull(latest.localizedNotes).endsWith("/CHANGELOG.es.md"))
    }

    @Test fun checksImportedPublicVersionsWithoutOfferingDowngrades() {
        val beta20 = requireNotNull(RomVerifier.harmonyReleaseTag(RomVerifier.HARMONY_SEVEN_LANGUAGES_SHA1))
        val beta21 = requireNotNull(RomVerifier.harmonyReleaseTag(RomVerifier.HARMONY_BETA_2_1_SHA1))

        assertEquals(RomVerifier.Variant.HARMONY, RomVerifier.identify(RomVerifier.HARMONY_BETA_2_1_SHA1))
        assertTrue(HarmonyInstaller.isNewer(beta21, beta20))
        assertFalse(HarmonyInstaller.isNewer(beta20, beta21))
        assertFalse(HarmonyInstaller.isNewer(beta21, beta21))
        assertTrue(HarmonyInstaller.isNewer("harmony-beta1.5", "harmony-betav1"))
        assertNull(RomVerifier.harmonyReleaseTag(RomVerifier.HARMONY_TERRIERMON_SHA1))
        assertNull(RomVerifier.harmonyReleaseTag(RomVerifier.USA_SHA1))
    }

    private fun release(tag: String, hasManifest: Boolean = true): JSONObject {
        val base = "dw2003-$tag"
        val names = listOf("$base.bps", "CHANGELOG.es.md") + if (hasManifest) listOf("$base.json") else emptyList()
        val assets = JSONArray()
        for (name in names) {
            assets.put(JSONObject().put("name", name).put(
                "browser_download_url", "https://github.com/rsigristc/dw2003-qol-patch/releases/download/$tag/$name"
            ))
        }
        return JSONObject().put("tag_name", tag).put("assets", assets)
    }
}
