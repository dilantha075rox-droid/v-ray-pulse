package com.example.telemetry

import android.net.TrafficStats
import android.os.Process
import com.example.model.TelemetryData
import com.example.optimization.OptimizationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket

object TelemetryManager {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var telemetryJob: Job? = null

    private val _telemetry = MutableStateFlow(TelemetryData())
    val telemetry: StateFlow<TelemetryData> = _telemetry.asStateFlow()

    private var startRxBytes = 0L
    private var startTxBytes = 0L
    private var lastRxBytes = 0L
    private var lastTxBytes = 0L
    private var lastTimestamp = 0L
    private var sessionStartTimestamp = 0L
    private var isConnected = false
    private var currentHost: String? = null
    private var currentPort: Int = 443
    private var cachedPing: Long = -1

    fun startTelemetry(host: String? = null, port: Int = 443) {
        currentHost = host
        currentPort = port
        isConnected = true
        resetCounters()

        telemetryJob?.cancel()
        telemetryJob = scope.launch {
            if (!host.isNullOrBlank()) {
                measureServerPing(host, port)
            }

            var pingTick = 0

            while (isActive) {
                val delayMs = OptimizationManager.getRefreshIntervalMs()
                delay(delayMs)

                if (OptimizationManager.shouldPauseUiSampling()) {
                    continue
                }

                val currentTime = System.currentTimeMillis()
                val deltaSec = ((currentTime - lastTimestamp) / 1000.0).coerceAtLeast(0.1)

                val uid = Process.myUid()
                var currentRx = TrafficStats.getUidRxBytes(uid)
                var currentTx = TrafficStats.getUidTxBytes(uid)

                if (currentRx < 0) {
                    currentRx = TrafficStats.getTotalRxBytes()
                    currentTx = TrafficStats.getTotalTxBytes()
                }

                val deltaRx = (currentRx - lastRxBytes).coerceAtLeast(0L)
                val deltaTx = (currentTx - lastTxBytes).coerceAtLeast(0L)

                val downSpeed = (deltaRx / deltaSec).toLong()
                val upSpeed = (deltaTx / deltaSec).toLong()

                val totalDown = (currentRx - startRxBytes).coerceAtLeast(0L)
                val totalUp = (currentTx - startTxBytes).coerceAtLeast(0L)

                val uptimeSec = if (sessionStartTimestamp > 0) {
                    (currentTime - sessionStartTimestamp) / 1000
                } else {
                    0L
                }

                lastRxBytes = currentRx
                lastTxBytes = currentTx
                lastTimestamp = currentTime

                pingTick++
                if (pingTick >= 10 && !host.isNullOrBlank()) {
                    pingTick = 0
                    launch { measureServerPing(host, port) }
                }

                _telemetry.value = TelemetryData(
                    downSpeedBytes = downSpeed,
                    upSpeedBytes = upSpeed,
                    totalDownBytes = totalDown,
                    totalUpBytes = totalUp,
                    sessionUptimeSeconds = uptimeSec,
                    pingMs = cachedPing,
                    port = 10808,
                    proxyType = "SOCKS5"
                )
            }
        }
    }

    fun stopTelemetry() {
        isConnected = false
        telemetryJob?.cancel()
        telemetryJob = null
        _telemetry.value = TelemetryData(
            pingMs = cachedPing,
            port = 10808,
            proxyType = "SOCKS5"
        )
    }

    suspend fun measureServerPing(host: String, port: Int): Long = withContext(Dispatchers.IO) {
        val ping = try {
            val startTime = System.currentTimeMillis()
            val socket = Socket()
            socket.connect(InetSocketAddress(host, port), 2500)
            val duration = System.currentTimeMillis() - startTime
            socket.close()
            duration
        } catch (_: Exception) {
            -1L
        }
        cachedPing = ping
        _telemetry.value = _telemetry.value.copy(pingMs = ping)
        ping
    }

    private fun resetCounters() {
        val uid = Process.myUid()
        var rx = TrafficStats.getUidRxBytes(uid)
        var tx = TrafficStats.getUidTxBytes(uid)
        if (rx < 0) {
            rx = TrafficStats.getTotalRxBytes()
            tx = TrafficStats.getTotalTxBytes()
        }
        startRxBytes = rx
        startTxBytes = tx
        lastRxBytes = rx
        lastTxBytes = tx
        lastTimestamp = System.currentTimeMillis()
        sessionStartTimestamp = System.currentTimeMillis()
    }
}
