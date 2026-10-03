package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.os.StrictMode
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.builder.XrayConfigBuilder
import com.example.model.VlessConfig
import com.example.parser.VlessParser
import go.Seq
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import libv2ray.CoreCallbackHandler
import libv2ray.CoreController
import libv2ray.Libv2ray
import java.util.concurrent.atomic.AtomicBoolean

class V2rayVpnService : VpnService() {

    companion object {
        private const val TAG = "VRayPulseVpn"
        const val ACTION_CONNECT = "com.example.service.CONNECT"
        const val ACTION_DISCONNECT = "com.example.service.DISCONNECT"
        const val EXTRA_VLESS_URL = "extra_vless_url"

        private const val NOTIFICATION_CHANNEL_ID = "vray_pulse_vpn_channel"
        private const val NOTIFICATION_ID = 1001

        fun startVpn(context: Context, vlessUrl: String) {
            val intent = Intent(context, V2rayVpnService::class.java).apply {
                action = ACTION_CONNECT
                putExtra(EXTRA_VLESS_URL, vlessUrl)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopVpn(context: Context) {
            val intent = Intent(context, V2rayVpnService::class.java).apply {
                action = ACTION_DISCONNECT
            }
            context.startService(intent)
        }
    }

    private var vpnInterface: ParcelFileDescriptor? = null
    private var coreController: CoreController? = null
    private val isRunningLock = AtomicBoolean(false)
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var currentConfig: VlessConfig? = null

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "VPN Service created")
        try {
            val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
            Seq.setContext(applicationContext)
            Libv2ray.initCoreEnv(filesDir.absolutePath, "")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Core environment", e)
        }
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        Log.i(TAG, "VPN Service onStartCommand: action=$action")

        when (action) {
            ACTION_CONNECT -> {
                val rawUrl = intent.getStringExtra(EXTRA_VLESS_URL).orEmpty()
                if (rawUrl.isBlank()) {
                    Log.e(TAG, "No VLESS URL provided")
                    VpnStateManager.setError("Missing VLESS configuration")
                    stopSelf()
                    return START_NOT_STICKY
                }
                startVpnTunnel(rawUrl)
                return START_STICKY
            }
            ACTION_DISCONNECT -> {
                stopVpnTunnel()
                return START_NOT_STICKY
            }
            else -> {
                // System restart or prepare
                if (vpnInterface == null && !isRunningLock.get()) {
                    stopSelf()
                }
                return START_NOT_STICKY
            }
        }
    }

    private fun startVpnTunnel(rawUrl: String) {
        if (isRunningLock.getAndSet(true)) {
            Log.w(TAG, "VPN already running or starting")
            return
        }

        VpnStateManager.updateState(VpnState.Connecting)

        serviceScope.launch {
            try {
                // 1. Parse configuration
                val config = VlessParser.parse(rawUrl)
                currentConfig = config
                Log.i(TAG, "VLESS parsed: ${config.toSafeSummary()}")

                // 2. Build Xray JSON config
                val configJson = XrayConfigBuilder.build(config)

                // 3. Establish Android TUN Interface
                val pfd = withContext(Dispatchers.Main) {
                    establishTunInterface()
                }

                if (pfd == null) {
                    throw IllegalStateException("Failed to establish Android VPN TUN interface")
                }
                vpnInterface = pfd

                // 4. Start foreground notification
                startForeground(NOTIFICATION_ID, buildNotification(config))

                // 5. Start Xray Core
                Log.i(TAG, "Starting Xray core loop with TUN fd=${pfd.fd}...")
                val controller = Libv2ray.newCoreController(object : CoreCallbackHandler {
                    override fun startup(): Long {
                        Log.i(TAG, "CoreCallback: startup")
                        return 0
                    }

                    override fun shutdown(): Long {
                        Log.i(TAG, "CoreCallback: shutdown")
                        return 0
                    }

                    override fun onEmitStatus(status: Long, message: String?): Long {
                        Log.i(TAG, "CoreCallback: status=$status, msg=$message")
                        return 0
                    }
                })
                coreController = controller

                controller.startLoop(configJson, pfd.fd)

                if (!controller.isRunning) {
                    throw IllegalStateException("VPN core failed to start")
                }

                Log.i(TAG, "VPN Tunnel established successfully!")
                com.example.telemetry.TelemetryManager.startTelemetry(config.server, config.port)
                VpnStateManager.setActiveConfig(config)
                VpnStateManager.updateState(VpnState.Connected)

            } catch (e: Exception) {
                Log.e(TAG, "Error starting VPN tunnel: ${e.message}")
                cleanup()
                val friendlyMsg = when {
                    e.message?.contains("permission", ignoreCase = true) == true ->
                        "VPN permission required"
                    e.message?.contains("core", ignoreCase = true) == true ->
                        "VPN core failed to start"
                    e.message?.contains("TUN", ignoreCase = true) == true ->
                        "Unable to create TUN interface"
                    e is IllegalArgumentException ->
                        e.message ?: "Invalid VLESS configuration"
                    else ->
                        "Connection failed: ${e.message?.take(80) ?: "Unknown error"}"
                }
                VpnStateManager.setError(friendlyMsg)
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
    }

    private fun establishTunInterface(): ParcelFileDescriptor? {
        val builder = Builder()
            .setSession("V-RAY PULSE")
            .setMtu(1500)
            .addAddress("10.10.10.1", 30)
            .addRoute("0.0.0.0", 0)
            .addDnsServer("1.1.1.1")
            .addDnsServer("8.8.8.8")

        // Exclude our own package so outbound traffic from Xray core bypasses the TUN
        try {
            builder.addDisallowedApplication(packageName)
        } catch (e: Exception) {
            Log.w(TAG, "Unable to disallow self package: ${e.message}")
        }

        // Close any previous interface
        try {
            vpnInterface?.close()
        } catch (_: Exception) {}

        return builder.establish()
    }

    private fun stopVpnTunnel() {
        Log.i(TAG, "Stopping VPN tunnel...")
        VpnStateManager.updateState(VpnState.Disconnecting)
        serviceScope.launch {
            cleanup()
            VpnStateManager.setActiveConfig(null)
            VpnStateManager.updateState(VpnState.Disconnected)
            withContext(Dispatchers.Main) {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            Log.i(TAG, "VPN Tunnel stopped")
        }
    }

    private fun cleanup() {
        isRunningLock.set(false)
        com.example.telemetry.TelemetryManager.stopTelemetry()
        try {
            coreController?.stopLoop()
        } catch (e: Exception) {
            Log.w(TAG, "Error stopping core loop: ${e.message}")
        }
        coreController = null

        try {
            vpnInterface?.close()
        } catch (e: Exception) {
            Log.w(TAG, "Error closing VPN interface: ${e.message}")
        }
        vpnInterface = null
    }

    override fun onRevoke() {
        Log.w(TAG, "VPN permission revoked by user or system")
        stopVpnTunnel()
        super.onRevoke()
    }

    override fun onDestroy() {
        cleanup()
        serviceScope.cancel()
        Log.i(TAG, "VPN Service destroyed")
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "V-RAY PULSE VPN Status",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows real-time connection status for V-RAY PULSE VPN"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(config: VlessConfig): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingOpenApp = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val disconnectIntent = Intent(this, V2rayVpnService::class.java).apply {
            action = ACTION_DISCONNECT
        }
        val pendingDisconnect = PendingIntent.getService(
            this,
            1,
            disconnectIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "V-RAY PULSE • Connected"
        val content = "Routing via ${config.remarks.ifBlank { config.server }}:${config.port} (${config.displayTransport})"

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(content)
            .setSubText("Dilanthar's cyber rig")
            .setContentIntent(pendingOpenApp)
            .addAction(0, "DISCONNECT", pendingDisconnect)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }
}
