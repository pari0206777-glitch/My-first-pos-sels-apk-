package com.example.data.update

import android.content.Context
import android.content.SharedPreferences

class AppUpdatePreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("vyaparpos_update_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_UPDATE_URL = "update_source_url"
        private const val KEY_LAST_DISMISSED_VERSION = "last_dismissed_version_code"
        private const val KEY_LAST_CHECK_TIME = "last_check_timestamp"
        private const val KEY_SIMULATE_MODE = "simulate_mode" // "NONE", "OPTIONAL", "MANDATORY", "UP_TO_DATE", "NETWORK_ERROR"

        // Default public endpoint template (e.g., GitHub raw or custom domain)
        const val DEFAULT_UPDATE_URL = "https://raw.githubusercontent.com/vyaparpos/releases/main/version.json"
    }

    var updateSourceUrl: String
        get() = prefs.getString(KEY_UPDATE_URL, DEFAULT_UPDATE_URL) ?: DEFAULT_UPDATE_URL
        set(value) = prefs.edit().putString(KEY_UPDATE_URL, value.trim()).apply()

    var lastDismissedVersionCode: Int
        get() = prefs.getInt(KEY_LAST_DISMISSED_VERSION, 0)
        set(value) = prefs.edit().putInt(KEY_LAST_DISMISSED_VERSION, value).apply()

    var lastCheckTimestamp: Long
        get() = prefs.getLong(KEY_LAST_CHECK_TIME, 0L)
        set(value) = prefs.edit().putLong(KEY_LAST_CHECK_TIME, value).apply()

    var simulationMode: String
        get() = prefs.getString(KEY_SIMULATE_MODE, "NONE") ?: "NONE"
        set(value) = prefs.edit().putString(KEY_SIMULATE_MODE, value).apply()

    fun resetDismissedVersion() {
        prefs.edit().remove(KEY_LAST_DISMISSED_VERSION).apply()
    }
}
