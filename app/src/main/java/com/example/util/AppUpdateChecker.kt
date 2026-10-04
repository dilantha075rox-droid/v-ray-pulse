package com.example.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class UpdateCheckResult(
    val isChecking: Boolean = false,
    val isUpToDate: Boolean = false,
    val updateAvailable: Boolean = false,
    val latestVersion: String = "1.0",
    val downloadUrl: String = "https://github.com/dilantha075rox-droid/v-ray-pulse/releases",
    val releaseNotes: String = "",
    val lastCheckedText: String = "",
    val errorMessage: String? = null
)

object AppUpdateChecker {

    var GITHUB_OWNER = "dilantha075rox-droid"
    var GITHUB_REPO = "v-ray-pulse"

    suspend fun checkForUpdates(currentVersion: String): UpdateCheckResult = withContext(Dispatchers.IO) {
        val timeStr = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
        val apiUrl = "https://api.github.com/repos/$GITHUB_OWNER/$GITHUB_REPO/releases/latest"
        val repoWebUrl = "https://github.com/$GITHUB_OWNER/$GITHUB_REPO/releases"

        try {
            val url = URL(apiUrl)
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            conn.requestMethod = "GET"
            conn.setRequestProperty("User-Agent", "VRayPulse-Android")

            val responseCode = conn.responseCode

            if (responseCode == HttpURLConnection.HTTP_OK) {
                val json = conn.inputStream.bufferedReader().use { it.readText() }

                val rawTag = Regex("\"tag_name\"\\s*:\\s*\"([^\"]+)\"").find(json)?.groupValues?.get(1) ?: "v1.0"
                val rawName = Regex("\"name\"\\s*:\\s*\"([^\"]+)\"").find(json)?.groupValues?.get(1) ?: rawTag
                val body = Regex("\"body\"\\s*:\\s*\"([^\"]+)\"").find(json)?.groupValues?.get(1) ?: "New release update available"
                val apkDownloadUrl = Regex("\"browser_download_url\"\\s*:\\s*\"([^\"]+\\.apk)\"").find(json)?.groupValues?.get(1)
                    ?: Regex("\"html_url\"\\s*:\\s*\"([^\"]+)\"").find(json)?.groupValues?.get(1)
                    ?: repoWebUrl

                val latestNum = Regex("(\\d+(\\.\\d+)*)").find(rawName)?.groupValues?.get(1)
                    ?: Regex("(\\d+(\\.\\d+)*)").find(rawTag)?.groupValues?.get(1)
                    ?: "1.0"

                val currentNum = Regex("(\\d+(\\.\\d+)*)").find(currentVersion)?.groupValues?.get(1) ?: "1.0"

                val isNewer = compareVersions(latestNum, currentNum) > 0 || (rawTag != "v$currentVersion" && rawTag != currentVersion)

                val displayTitle = if (rawName.isNotBlank() && rawName != rawTag) "$rawName ($rawTag)" else rawTag

                UpdateCheckResult(
                    isChecking = false,
                    isUpToDate = !isNewer,
                    updateAvailable = isNewer,
                    latestVersion = displayTitle,
                    downloadUrl = apkDownloadUrl,
                    releaseNotes = body.take(120),
                    lastCheckedText = "Checked at $timeStr"
                )
            } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                UpdateCheckResult(
                    isChecking = false,
                    isUpToDate = false,
                    latestVersion = "v$currentVersion",
                    downloadUrl = repoWebUrl,
                    lastCheckedText = "Checked at $timeStr (Repo is Private on GitHub)"
                )
            } else {
                UpdateCheckResult(
                    isChecking = false,
                    isUpToDate = true,
                    latestVersion = "v$currentVersion",
                    downloadUrl = repoWebUrl,
                    lastCheckedText = "Checked at $timeStr (Code $responseCode)"
                )
            }
        } catch (e: Exception) {
            UpdateCheckResult(
                isChecking = false,
                isUpToDate = true,
                latestVersion = "v$currentVersion",
                downloadUrl = repoWebUrl,
                lastCheckedText = "Checked at $timeStr"
            )
        }
    }

    private fun compareVersions(v1: String, v2: String): Int {
        val parts1 = v1.split(".").mapNotNull { it.toIntOrNull() }
        val parts2 = v2.split(".").mapNotNull { it.toIntOrNull() }
        val maxLen = maxOf(parts1.size, parts2.size)

        for (i in 0 until maxLen) {
            val p1 = parts1.getOrElse(i) { 0 }
            val p2 = parts2.getOrElse(i) { 0 }
            if (p1 != p2) return p1.compareTo(p2)
        }
        return 0
    }
}
