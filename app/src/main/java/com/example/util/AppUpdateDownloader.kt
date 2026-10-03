package com.example.util

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

sealed class DownloadState {
    object Idle : DownloadState()
    data class Downloading(val progressPercent: Int) : DownloadState()
    data class Downloaded(val apkFile: File) : DownloadState()
    data class Error(val message: String) : DownloadState()
}

object AppUpdateDownloader {

    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val downloadState: StateFlow<DownloadState> = _downloadState.asStateFlow()

    suspend fun downloadAndInstallApk(context: Context, downloadUrl: String): Unit = withContext(Dispatchers.IO) {
        try {
            _downloadState.value = DownloadState.Downloading(0)

            val apkFile = File(context.cacheDir, "vray_pulse_update.apk")
            if (apkFile.exists()) {
                apkFile.delete()
            }

            val url = URL(downloadUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 8000
            connection.readTimeout = 8000
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "VRayPulse-Android")
            connection.instanceFollowRedirects = true
            connection.connect()

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP || responseCode == HttpURLConnection.HTTP_MOVED_PERM || responseCode == 307 || responseCode == 308) {
                val redirectUrl = connection.getHeaderField("Location")
                if (!redirectUrl.isNullOrBlank()) {
                    return@withContext downloadAndInstallApk(context, redirectUrl)
                }
            }

            if (responseCode != HttpURLConnection.HTTP_OK) {
                _downloadState.value = DownloadState.Error("Server returned code $responseCode")
                return@withContext
            }

            val totalLength = connection.contentLength
            val inputStream = connection.inputStream
            val outputStream = FileOutputStream(apkFile)

            val buffer = ByteArray(8192)
            var bytesRead: Int
            var totalBytesRead = 0L

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                totalBytesRead += bytesRead

                if (totalLength > 0) {
                    val percent = (totalBytesRead * 100 / totalLength).toInt().coerceIn(0, 100)
                    _downloadState.value = DownloadState.Downloading(percent)
                }
            }

            outputStream.flush()
            outputStream.close()
            inputStream.close()

            _downloadState.value = DownloadState.Downloaded(apkFile)

            withContext(Dispatchers.Main) {
                AppInstaller.installApk(context, apkFile)
            }

        } catch (e: Exception) {
            _downloadState.value = DownloadState.Error(e.message ?: "Failed to download update")
        }
    }

    fun resetState() {
        _downloadState.value = DownloadState.Idle
    }
}
