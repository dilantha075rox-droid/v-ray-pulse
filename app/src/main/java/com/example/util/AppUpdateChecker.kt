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

    // Configured GitHub owner and repo name
    var GITHUB_OWNER = "dilantha075rox-droid"
    var GITHUB_REPO = "v-ray-pulse"

    suspend fun checkForUpdates(currentVersion: String): UpdateCheckResult = withContext(Dispatchers.IO) {
        val timeStr = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
        val apiUrl = "https://api.github.com/repos/$GITHUB_OWNER/$GITHUB_REPO/releases/latest"
        val repoWebUrl = "https://github.com/$GITHUB_OWNER/$GITHUB_REPO/releases"

        try {
            val url = URL(apiUrl)
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 4000
            conn.readTimeout = 4000
            conn.requestMethod = "GET"
            conn.setRequestProperty("User-Agent", "VRayPulse-Android")

            if (conn.responseCode == 200) {
                val json = conn.inputStream.bufferedReader().use { it.readText() }
                val tag = Regex("\"tag_name\"\\s*:\\s*\"([^\"]+)\"").find(json)?.groupValues?.get(1) ?: "v1.0"
                val body = Regex("\"body\"\\s*:\\s*\"([^\"]+)\"").find(json)?.groupValues?.get(1) ?: "New release available with updates."
                val htmlUrl = Regex("\"html_url\"\\s*:\\s*\"([^\"]+)\"").find(json)?.groupValues?.get(1) ?: repoWebUrl

                val cleanTag = tag.replace("v", "").trim()
                val cleanCurrent = currentVersion.replace("v", "").trim()

                val isNewer = cleanTag != cleanCurrent && cleanTag > cleanCurrent

                UpdateCheckResult(
                    isChecking = false,
                    isUpToDate = !isNewer,
                    updateAvailable = isNewer,
                    latestVersion = if (tag.startsWith("v")) tag else "v$tag",
                    downloadUrl = htmlUrl,
                    releaseNotes = body.take(100),
                    lastCheckedText = "Checked at $timeStr"
                )
            } else {
                UpdateCheckResult(
                    isChecking = false,
                    isUpToDate = true,
                    latestVersion = "v$currentVersion",
                    downloadUrl = repoWebUrl,
                    lastCheckedText = "Checked at $timeStr (Up to date)"
                )
            }
        } catch (e: Exception) {
            UpdateCheckResult(
                isChecking = false,
                isUpToDate = true,
                latestVersion = "v$currentVersion",
                downloadUrl = repoWebUrl,
                lastCheckedText = "Checked at $timeStr (Up to date)"
            )
        }
    }
}
