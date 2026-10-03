package com.example.model

import java.util.Locale

/**
 * Real-time network telemetry metrics for the VPN connection dashboard.
 */
data class TelemetryData(
    val downSpeedBytes: Long = 0,
    val upSpeedBytes: Long = 0,
    val totalDownBytes: Long = 0,
    val totalUpBytes: Long = 0,
    val sessionUptimeSeconds: Long = 0,
    val pingMs: Long = -1,
    val port: Int = 10808,
    val proxyType: String = "SOCKS5"
) {
    val formattedDownSpeed: String
        get() = formatSpeed(downSpeedBytes)

    val formattedUpSpeed: String
        get() = formatSpeed(upSpeedBytes)

    val formattedTotalDown: String
        get() = formatBytes(totalDownBytes)

    val formattedTotalUp: String
        get() = formatBytes(totalUpBytes)

    val formattedUptime: String
        get() {
            val hours = sessionUptimeSeconds / 3600
            val minutes = (sessionUptimeSeconds % 3600) / 60
            val seconds = sessionUptimeSeconds % 60
            return if (hours > 0) {
                String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format(Locale.US, "%02d:%02d", minutes, seconds)
            }
        }

    val formattedPing: String
        get() = if (pingMs >= 0) "${pingMs}ms" else "-- ms"

    private fun formatSpeed(bytesPerSec: Long): String {
        return when {
            bytesPerSec < 1024 -> "$bytesPerSec B/s"
            bytesPerSec < 1024 * 1024 -> String.format(Locale.US, "%.1f KB/s", bytesPerSec / 1024.0)
            else -> String.format(Locale.US, "%.2f MB/s", bytesPerSec / (1024.0 * 1024.0))
        }
    }

    private fun formatBytes(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format(Locale.US, "%.2f KB", bytes / 1024.0)
            bytes < 1024 * 1024 * 1024 -> String.format(Locale.US, "%.2f MB", bytes / (1024.0 * 1024.0))
            else -> String.format(Locale.US, "%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0))
        }
    }
}
