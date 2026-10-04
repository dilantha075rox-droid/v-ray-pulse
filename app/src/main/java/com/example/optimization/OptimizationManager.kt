package com.example.optimization

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.storage.vlessDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

enum class BatteryProfile {
    MAXIMUM, BALANCED, PERFORMANCE
}

enum class UpdateRate {
    LOW, BALANCED, HIGH;

    val intervalMs: Long
        get() = when (this) {
            LOW -> 3000L
            BALANCED -> 1500L
            HIGH -> 800L
        }
}

data class OptimizationSettings(
    val masterEnabled: Boolean = true,
    val profile: BatteryProfile = BatteryProfile.BALANCED,
    val screenOffOpt: Boolean = true,
    val backgroundOpt: Boolean = true,
    val liveTrafficMonitor: Boolean = true,
    val updateRate: UpdateRate = UpdateRate.BALANCED,
    val uiAnimations: Boolean = true,
    val cpuOptimization: Boolean = true,
    val memoryOptimization: Boolean = true
)

object OptimizationManager {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val keyMaster = booleanPreferencesKey("opt_master")
    private val keyProfile = stringPreferencesKey("opt_profile")
    private val keyScreenOff = booleanPreferencesKey("opt_screen_off")
    private val keyBackground = booleanPreferencesKey("opt_background")
    private val keyTrafficMonitor = booleanPreferencesKey("opt_traffic_monitor")
    private val keyUpdateRate = stringPreferencesKey("opt_update_rate")
    private val keyAnimations = booleanPreferencesKey("opt_animations")
    private val keyCpu = booleanPreferencesKey("opt_cpu")
    private val keyMemory = booleanPreferencesKey("opt_memory")

    private val _settings = MutableStateFlow(OptimizationSettings())
    val settings: StateFlow<OptimizationSettings> = _settings.asStateFlow()

    private val _isScreenOn = MutableStateFlow(true)
    val isScreenOn: StateFlow<Boolean> = _isScreenOn.asStateFlow()

    private val _isAppInForeground = MutableStateFlow(true)
    val isAppInForeground: StateFlow<Boolean> = _isAppInForeground.asStateFlow()

    private var isInitialized = false

    fun init(context: Context) {
        if (isInitialized) return
        isInitialized = true

        // Register screen state receiver
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
        }
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                when (intent?.action) {
                    Intent.ACTION_SCREEN_OFF -> _isScreenOn.value = false
                    Intent.ACTION_SCREEN_ON -> _isScreenOn.value = true
                }
            }
        }
        context.applicationContext.registerReceiver(receiver, filter)

        // Read preferences
        scope.launch {
            context.vlessDataStore.data.map { prefs ->
                OptimizationSettings(
                    masterEnabled = prefs[keyMaster] ?: true,
                    profile = try {
                        BatteryProfile.valueOf(prefs[keyProfile] ?: BatteryProfile.BALANCED.name)
                    } catch (_: Exception) {
                        BatteryProfile.BALANCED
                    },
                    screenOffOpt = prefs[keyScreenOff] ?: true,
                    backgroundOpt = prefs[keyBackground] ?: true,
                    liveTrafficMonitor = prefs[keyTrafficMonitor] ?: true,
                    updateRate = try {
                        UpdateRate.valueOf(prefs[keyUpdateRate] ?: UpdateRate.BALANCED.name)
                    } catch (_: Exception) {
                        UpdateRate.BALANCED
                    },
                    uiAnimations = prefs[keyAnimations] ?: true,
                    cpuOptimization = prefs[keyCpu] ?: true,
                    memoryOptimization = prefs[keyMemory] ?: true
                )
            }.collect { updated ->
                _settings.value = updated
            }
        }
    }

    fun setAppForegroundState(inForeground: Boolean) {
        _isAppInForeground.value = inForeground
    }

    suspend fun updateMaster(enabled: Boolean, context: Context) {
        _settings.value = _settings.value.copy(masterEnabled = enabled)
        context.vlessDataStore.edit { it[keyMaster] = enabled }
    }

    suspend fun applyProfile(profile: BatteryProfile, context: Context) {
        val preset = when (profile) {
            BatteryProfile.MAXIMUM -> _settings.value.copy(
                profile = BatteryProfile.MAXIMUM,
                screenOffOpt = true,
                backgroundOpt = true,
                liveTrafficMonitor = false,
                updateRate = UpdateRate.LOW,
                uiAnimations = false,
                cpuOptimization = true,
                memoryOptimization = true
            )
            BatteryProfile.BALANCED -> _settings.value.copy(
                profile = BatteryProfile.BALANCED,
                screenOffOpt = true,
                backgroundOpt = true,
                liveTrafficMonitor = true,
                updateRate = UpdateRate.BALANCED,
                uiAnimations = true,
                cpuOptimization = true,
                memoryOptimization = true
            )
            BatteryProfile.PERFORMANCE -> _settings.value.copy(
                profile = BatteryProfile.PERFORMANCE,
                screenOffOpt = true,
                backgroundOpt = true,
                liveTrafficMonitor = true,
                updateRate = UpdateRate.HIGH,
                uiAnimations = true,
                cpuOptimization = true,
                memoryOptimization = true
            )
        }

        _settings.value = preset
        context.vlessDataStore.edit { prefs ->
            prefs[keyProfile] = preset.profile.name
            prefs[keyScreenOff] = preset.screenOffOpt
            prefs[keyBackground] = preset.backgroundOpt
            prefs[keyTrafficMonitor] = preset.liveTrafficMonitor
            prefs[keyUpdateRate] = preset.updateRate.name
            prefs[keyAnimations] = preset.uiAnimations
            prefs[keyCpu] = preset.cpuOptimization
            prefs[keyMemory] = preset.memoryOptimization
        }
    }

    suspend fun updateSetting(
        context: Context,
        screenOff: Boolean? = null,
        background: Boolean? = null,
        trafficMonitor: Boolean? = null,
        updateRate: UpdateRate? = null,
        animations: Boolean? = null,
        cpu: Boolean? = null,
        memory: Boolean? = null
    ) {
        val current = _settings.value
        val updated = current.copy(
            screenOffOpt = screenOff ?: current.screenOffOpt,
            backgroundOpt = background ?: current.backgroundOpt,
            liveTrafficMonitor = trafficMonitor ?: current.liveTrafficMonitor,
            updateRate = updateRate ?: current.updateRate,
            uiAnimations = animations ?: current.uiAnimations,
            cpuOptimization = cpu ?: current.cpuOptimization,
            memoryOptimization = memory ?: current.memoryOptimization
        )

        _settings.value = updated
        context.vlessDataStore.edit { prefs ->
            screenOff?.let { prefs[keyScreenOff] = it }
            background?.let { prefs[keyBackground] = it }
            trafficMonitor?.let { prefs[keyTrafficMonitor] = it }
            updateRate?.let { prefs[keyUpdateRate] = it.name }
            animations?.let { prefs[keyAnimations] = it }
            cpu?.let { prefs[keyCpu] = it }
            memory?.let { prefs[keyMemory] = it }
        }
    }

    fun shouldPauseUiSampling(): Boolean {
        val s = _settings.value
        if (!s.masterEnabled) return false
        if (!s.liveTrafficMonitor) return true
        if (s.screenOffOpt && !_isScreenOn.value) return true
        if (s.backgroundOpt && !_isAppInForeground.value) return true
        return false
    }

    fun getRefreshIntervalMs(): Long {
        val s = _settings.value
        return if (s.masterEnabled) s.updateRate.intervalMs else 1000L
    }

    fun getSystemBatteryStatus(context: Context): String {
        val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        if (pm != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val isIgnoring = pm.isIgnoringBatteryOptimizations(context.packageName)
            return if (isIgnoring) "UNRESTRICTED" else "OPTIMIZED"
        }
        return "SYSTEM MANAGED"
    }

    fun isVivoDevice(): Boolean {
        val brand = Build.BRAND ?: ""
        val manufacturer = Build.MANUFACTURER ?: ""
        return brand.equals("vivo", ignoreCase = true) || manufacturer.equals("vivo", ignoreCase = true)
    }
}
