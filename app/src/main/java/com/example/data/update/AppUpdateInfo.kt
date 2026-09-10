package com.example.data.update

import org.json.JSONObject

/**
 * Model representing remote update metadata for VyaparPOS Retail & Inventory.
 *
 * Example remote JSON format:
 * {
 *   "latestVersion": "1.0.1",
 *   "versionCode": 2,
 *   "downloadUrl": "https://github.com/my-shop/vyaparpos/releases/download/v1.0.1/app-release.apk",
 *   "releaseNotes": "• Added low-stock alert notifications\n• Faster thermal invoice generation\n• Bug fixes for barcode scanning",
 *   "mandatoryUpdate": false,
 *   "minimumSupportedVersion": "1.0.0",
 *   "minimumSupportedVersionCode": 1,
 *   "sha256": "optional-sha256-hex-hash"
 * }
 */
data class AppUpdateInfo(
    val latestVersion: String,
    val versionCode: Int,
    val downloadUrl: String,
    val releaseNotes: String,
    val mandatoryUpdate: Boolean = false,
    val minimumSupportedVersion: String = "1.0.0",
    val minimumSupportedVersionCode: Int = 1,
    val sha256: String? = null
) {
    fun toJson(): String {
        val json = JSONObject()
        json.put("latestVersion", latestVersion)
        json.put("versionCode", versionCode)
        json.put("downloadUrl", downloadUrl)
        json.put("releaseNotes", releaseNotes)
        json.put("mandatoryUpdate", mandatoryUpdate)
        json.put("minimumSupportedVersion", minimumSupportedVersion)
        json.put("minimumSupportedVersionCode", minimumSupportedVersionCode)
        if (sha256 != null) {
            json.put("sha256", sha256)
        }
        return json.toString()
    }

    companion object {
        fun fromJson(jsonStr: String): AppUpdateInfo {
            val json = JSONObject(jsonStr)
            return AppUpdateInfo(
                latestVersion = json.optString("latestVersion", "1.0.0"),
                versionCode = json.optInt("versionCode", 1),
                downloadUrl = json.optString("downloadUrl", ""),
                releaseNotes = json.optString("releaseNotes", "New update available."),
                mandatoryUpdate = json.optBoolean("mandatoryUpdate", false),
                minimumSupportedVersion = json.optString("minimumSupportedVersion", "1.0.0"),
                minimumSupportedVersionCode = json.optInt("minimumSupportedVersionCode", 1),
                sha256 = if (json.has("sha256")) json.optString("sha256") else null
            )
        }
    }
}
