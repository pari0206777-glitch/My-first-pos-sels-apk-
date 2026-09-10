package com.example

import com.example.data.update.AppUpdateInfo
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AppUpdateUnitTest {

    private fun isUpdateAvailable(
        currentCode: Int,
        remoteCode: Int,
        currentName: String = "",
        remoteName: String = ""
    ): Boolean {
        if (remoteCode > currentCode) return true
        if (remoteCode < currentCode) return false
        return compareSemanticVersions(remoteName, currentName) > 0
    }

    private fun compareSemanticVersions(v1: String, v2: String): Int {
        val clean1 = v1.removePrefix("v").trim()
        val clean2 = v2.removePrefix("v").trim()
        val parts1 = clean1.split(".").mapNotNull { it.toIntOrNull() }
        val parts2 = clean2.split(".").mapNotNull { it.toIntOrNull() }

        val maxLen = maxOf(parts1.size, parts2.size)
        for (i in 0 until maxLen) {
            val num1 = parts1.getOrElse(i) { 0 }
            val num2 = parts2.getOrElse(i) { 0 }
            if (num1 != num2) {
                return num1.compareTo(num2)
            }
        }
        return 0
    }

    private fun isMandatoryUpdate(currentCode: Int, updateInfo: AppUpdateInfo): Boolean {
        return updateInfo.mandatoryUpdate || currentCode < updateInfo.minimumSupportedVersionCode
    }

    @Test
    fun testVersionComparison_NewerVersionCode() {
        // Current: 1.0.0 (code 1), Remote: 1.0.1 (code 2) -> Update Available
        assertTrue(isUpdateAvailable(currentCode = 1, remoteCode = 2, currentName = "1.0.0", remoteName = "1.0.1"))
    }

    @Test
    fun testVersionComparison_SameVersionCode() {
        // Current: 1.0.0 (code 1), Remote: 1.0.0 (code 1) -> Up to Date
        assertFalse(isUpdateAvailable(currentCode = 1, remoteCode = 1, currentName = "1.0.0", remoteName = "1.0.0"))
    }

    @Test
    fun testVersionComparison_OlderRemoteVersionCode() {
        // Current: 1.0.1 (code 2), Remote: 1.0.0 (code 1) -> No update
        assertFalse(isUpdateAvailable(currentCode = 2, remoteCode = 1, currentName = "1.0.1", remoteName = "1.0.0"))
    }

    @Test
    fun testVersionComparison_SemanticVersionBump() {
        // Version codes same (e.g. 1), but semantic version 1.0.1 > 1.0.0
        assertTrue(isUpdateAvailable(currentCode = 1, remoteCode = 1, currentName = "1.0.0", remoteName = "1.0.1"))
        assertFalse(isUpdateAvailable(currentCode = 1, remoteCode = 1, currentName = "1.0.1", remoteName = "1.0.0"))
    }

    @Test
    fun testMandatoryUpdateLogic() {
        val optionalUpdate = AppUpdateInfo(
            latestVersion = "1.0.1",
            versionCode = 2,
            downloadUrl = "https://example.com/app.apk",
            releaseNotes = "Minor fixes",
            mandatoryUpdate = false,
            minimumSupportedVersionCode = 1
        )
        assertFalse(isMandatoryUpdate(currentCode = 1, updateInfo = optionalUpdate))

        val explicitMandatoryUpdate = AppUpdateInfo(
            latestVersion = "2.0.0",
            versionCode = 3,
            downloadUrl = "https://example.com/app.apk",
            releaseNotes = "Critical GST update",
            mandatoryUpdate = true,
            minimumSupportedVersionCode = 1
        )
        assertTrue(isMandatoryUpdate(currentCode = 1, updateInfo = explicitMandatoryUpdate))

        val minVersionEnforcedUpdate = AppUpdateInfo(
            latestVersion = "2.0.0",
            versionCode = 5,
            downloadUrl = "https://example.com/app.apk",
            releaseNotes = "Database schema migration",
            mandatoryUpdate = false,
            minimumSupportedVersionCode = 3 // requires at least code 3
        )
        // User is on code 2 (< 3) -> mandatory
        assertTrue(isMandatoryUpdate(currentCode = 2, updateInfo = minVersionEnforcedUpdate))
        // User is on code 3 (>= 3) -> optional
        assertFalse(isMandatoryUpdate(currentCode = 3, updateInfo = minVersionEnforcedUpdate))
    }

    @Test
    fun testAppUpdateInfoJsonSerialization() {
        val original = AppUpdateInfo(
            latestVersion = "1.0.2",
            versionCode = 3,
            downloadUrl = "https://example.com/vyaparpos.apk",
            releaseNotes = "Fast thermal billing\nBarcode scanning speedup",
            mandatoryUpdate = true,
            minimumSupportedVersion = "1.0.1",
            minimumSupportedVersionCode = 2,
            sha256 = "abcdef1234567890"
        )

        val jsonStr = original.toJson()
        val parsed = AppUpdateInfo.fromJson(jsonStr)

        assertEquals(original.latestVersion, parsed.latestVersion)
        assertEquals(original.versionCode, parsed.versionCode)
        assertEquals(original.downloadUrl, parsed.downloadUrl)
        assertEquals(original.releaseNotes, parsed.releaseNotes)
        assertEquals(original.mandatoryUpdate, parsed.mandatoryUpdate)
        assertEquals(original.minimumSupportedVersion, parsed.minimumSupportedVersion)
        assertEquals(original.minimumSupportedVersionCode, parsed.minimumSupportedVersionCode)
        assertEquals(original.sha256, parsed.sha256)
    }
}
