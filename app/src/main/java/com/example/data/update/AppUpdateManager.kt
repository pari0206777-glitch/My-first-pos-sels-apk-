package com.example.data.update

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.content.FileProvider
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

sealed class AppUpdateCheckResult {
    data class UpdateAvailable(
        val updateInfo: AppUpdateInfo,
        val isMandatory: Boolean,
        val shouldAutoPrompt: Boolean
    ) : AppUpdateCheckResult()

    data class UpToDate(
        val currentVersion: String,
        val currentVersionCode: Int
    ) : AppUpdateCheckResult()

    data class Error(
        val message: String,
        val isOffline: Boolean
    ) : AppUpdateCheckResult()
}

class AppUpdateManager(private val context: Context) {
    val preferences = AppUpdatePreferences(context)

    val currentVersionName: String
        get() = BuildConfig.VERSION_NAME

    val currentVersionCode: Int
        get() = BuildConfig.VERSION_CODE

    /**
     * Pure function for version comparison.
     * Evaluates whether remote version is strictly newer than current version.
     */
    fun isUpdateAvailable(
        currentCode: Int,
        remoteCode: Int,
        currentName: String = "",
        remoteName: String = ""
    ): Boolean {
        if (remoteCode > currentCode) return true
        if (remoteCode < currentCode) return false

        // If version codes are equal, perform semantic version fallback comparison
        return compareSemanticVersions(remoteName, currentName) > 0
    }

    /**
     * Checks if update is mandatory (developer flag or minimum supported version rule).
     */
    fun isMandatoryUpdate(currentCode: Int, updateInfo: AppUpdateInfo): Boolean {
        return updateInfo.mandatoryUpdate || currentCode < updateInfo.minimumSupportedVersionCode
    }

    /**
     * Semantic version string comparison (e.g. "1.0.1" vs "1.0.0").
     * Returns > 0 if v1 > v2, 0 if equal, < 0 if v1 < v2.
     */
    fun compareSemanticVersions(v1: String, v2: String): Int {
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

    /**
     * Checks for updates from the configured remote source.
     * Offline-first: catches all network and I/O errors gracefully without blocking or throwing.
     */
    suspend fun checkForUpdate(isManualCheck: Boolean = false): AppUpdateCheckResult = withContext(Dispatchers.IO) {
        preferences.lastCheckTimestamp = System.currentTimeMillis()

        // 1. Check for Simulation Modes (for easy developer and automated testing)
        when (preferences.simulationMode) {
            "OPTIONAL" -> {
                val simulatedInfo = AppUpdateInfo(
                    latestVersion = "1.0.1",
                    versionCode = currentVersionCode + 1,
                    downloadUrl = "https://github.com/vyaparpos/releases/releases/download/v1.0.1/VyaparPOS_1.0.1.apk",
                    releaseNotes = "• Faster barcode scanner with auto-focus\n• Added customer khata ledger & dues reminder\n• Thermal printing alignment fixes",
                    mandatoryUpdate = false,
                    minimumSupportedVersion = "1.0.0",
                    minimumSupportedVersionCode = currentVersionCode
                )
                val isMandatory = isMandatoryUpdate(currentVersionCode, simulatedInfo)
                val shouldPrompt = isManualCheck || (simulatedInfo.versionCode != preferences.lastDismissedVersionCode)
                return@withContext AppUpdateCheckResult.UpdateAvailable(simulatedInfo, isMandatory, shouldPrompt)
            }
            "MANDATORY" -> {
                val simulatedInfo = AppUpdateInfo(
                    latestVersion = "2.0.0",
                    versionCode = currentVersionCode + 2,
                    downloadUrl = "https://github.com/vyaparpos/releases/releases/download/v2.0.0/VyaparPOS_2.0.0.apk",
                    releaseNotes = "CRITICAL SECURITY UPDATE:\n• Mandatory GST compliance tax-slab update\n• Local database encryption upgrade\n• Update required to continue POS billing",
                    mandatoryUpdate = true,
                    minimumSupportedVersion = "2.0.0",
                    minimumSupportedVersionCode = currentVersionCode + 2
                )
                return@withContext AppUpdateCheckResult.UpdateAvailable(simulatedInfo, isMandatory = true, shouldAutoPrompt = true)
            }
            "UP_TO_DATE" -> {
                return@withContext AppUpdateCheckResult.UpToDate(currentVersionName, currentVersionCode)
            }
            "NETWORK_ERROR" -> {
                return@withContext AppUpdateCheckResult.Error("Simulated network timeout. App running in offline mode.", isOffline = true)
            }
        }

        // 2. Real Remote Check
        val urlString = preferences.updateSourceUrl
        if (urlString.isBlank()) {
            return@withContext AppUpdateCheckResult.Error("Update source URL is not configured.", isOffline = false)
        }

        var connection: HttpURLConnection? = null
        try {
            val url = URL(urlString)
            connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 5000
                readTimeout = 5000
                requestMethod = "GET"
                setRequestProperty("Accept", "application/json")
                setRequestProperty("User-Agent", "VyaparPOS-Android/${currentVersionName}")
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val responseBody = connection.inputStream.bufferedReader().use { it.readText() }
                val updateInfo = AppUpdateInfo.fromJson(responseBody)

                val hasNewVersion = isUpdateAvailable(
                    currentCode = currentVersionCode,
                    remoteCode = updateInfo.versionCode,
                    currentName = currentVersionName,
                    remoteName = updateInfo.latestVersion
                )

                if (hasNewVersion) {
                    val isMandatory = isMandatoryUpdate(currentVersionCode, updateInfo)
                    val shouldPrompt = isMandatory || isManualCheck || (updateInfo.versionCode != preferences.lastDismissedVersionCode)
                    AppUpdateCheckResult.UpdateAvailable(updateInfo, isMandatory, shouldPrompt)
                } else {
                    AppUpdateCheckResult.UpToDate(currentVersionName, currentVersionCode)
                }
            } else {
                AppUpdateCheckResult.Error("Server returned response code $responseCode", isOffline = false)
            }
        } catch (e: IOException) {
            // Network unreachable, DNS resolution failed, timeout, or airplane mode
            AppUpdateCheckResult.Error(
                message = "Unable to connect to update server (${e.localizedMessage ?: "Offline"}). Continuing in offline mode.",
                isOffline = true
            )
        } catch (e: Exception) {
            AppUpdateCheckResult.Error(
                message = "Update check failed: ${e.localizedMessage ?: "Unknown error"}",
                isOffline = false
            )
        } finally {
            connection?.disconnect()
        }
    }

    /**
     * Dismisses the current update prompt for optional updates.
     */
    fun dismissUpdate(versionCode: Int) {
        preferences.lastDismissedVersionCode = versionCode
    }

    /**
     * Downloads the APK file to the app's external downloads directory.
     * Reports live progress via onProgress callback.
     * Performs package identity and integrity validation before returning success.
     */
    suspend fun downloadApk(
        updateInfo: AppUpdateInfo,
        onProgress: (Float) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        val downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            ?: context.cacheDir
        if (!downloadDir.exists()) downloadDir.mkdirs()

        val apkFile = File(downloadDir, "VyaparPOS_update_${updateInfo.versionCode}.apk")
        if (apkFile.exists()) apkFile.delete()

        var connection: HttpURLConnection? = null
        try {
            val url = URL(updateInfo.downloadUrl)
            connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 10000
                readTimeout = 30000
                requestMethod = "GET"
                instanceFollowRedirects = true
            }

            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                return@withContext Result.failure(IOException("Download failed with HTTP status $responseCode"))
            }

            val totalLength = connection.contentLengthLong
            var downloadedBytes = 0L

            val input: InputStream = connection.inputStream
            val output = FileOutputStream(apkFile)

            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (input.read(buffer).also { bytesRead = it } != -1) {
                output.write(buffer, 0, bytesRead)
                downloadedBytes += bytesRead
                if (totalLength > 0) {
                    val progress = downloadedBytes.toFloat() / totalLength.toFloat()
                    onProgress(progress)
                }
            }

            output.flush()
            output.close()
            input.close()

            // 1. Verify File Integrity (SHA-256 if provided in metadata)
            if (!updateInfo.sha256.isNullOrBlank()) {
                val computedHash = computeSha256(apkFile)
                if (!computedHash.equals(updateInfo.sha256.trim(), ignoreCase = true)) {
                    apkFile.delete()
                    return@withContext Result.failure(
                        SecurityException("SHA-256 integrity mismatch. Downloaded APK may be corrupted or tampered.")
                    )
                }
            }

            // 2. Verify Android Package Identity & Compatibility
            val packageArchiveInfo = context.packageManager.getPackageArchiveInfo(apkFile.absolutePath, 0)
            if (packageArchiveInfo != null) {
                val downloadedPackage = packageArchiveInfo.packageName
                val currentPackage = context.packageName

                if (downloadedPackage != currentPackage) {
                    apkFile.delete()
                    return@withContext Result.failure(
                        SecurityException("Security check failed: Downloaded package ID ($downloadedPackage) does not match the installed app ($currentPackage). Update aborted.")
                    )
                }
            }

            Result.success(apkFile)
        } catch (e: Exception) {
            if (apkFile.exists()) apkFile.delete()
            Result.failure(e)
        } finally {
            connection?.disconnect()
        }
    }

    /**
     * Launches the official Android package installer for the downloaded APK using FileProvider.
     */
    fun launchInstaller(apkFile: File): Result<Unit> {
        return try {
            // Check for Unknown App Sources permission on Android 8.0+ (Oreo)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!context.packageManager.canRequestPackageInstalls()) {
                    val manageIntent = Intent(
                        Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                        Uri.parse("package:${context.packageName}")
                    ).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(manageIntent)
                    return Result.failure(
                        SecurityException("Please enable 'Allow from this source' for VyaparPOS, then tap Update Now again.")
                    )
                }
            }

            val apkUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(installIntent)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun computeSha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { stream ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (stream.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
