package com.example.ui

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.TelemetryData
import com.example.model.VlessConfig
import com.example.parser.VlessParser
import com.example.service.V2rayVpnService
import com.example.service.VpnState
import com.example.service.VpnStateManager
import com.example.storage.VlessRepository
import com.example.telemetry.TelemetryManager
import com.example.util.AppUpdateChecker
import com.example.util.AppUpdateDownloader
import com.example.util.DeviceInfo
import com.example.util.DeviceInfoHelper
import com.example.util.DownloadState
import com.example.util.ServerDetailsData
import com.example.util.ServerDetailsHelper
import com.example.util.UpdateCheckResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class DashboardTab {
    PULSE, NODES, ROUTING, CUSTOM_RIG, EXPERT
}

data class MainUiState(
    val vpnState: VpnState = VpnState.Disconnected,
    val configuredServer: VlessConfig? = null,
    val isImportDialogOpen: Boolean = false,
    val isExpertPanelOpen: Boolean = false,
    val isDetailsDialogOpen: Boolean = false,
    val serverDetails: ServerDetailsData = ServerDetailsData(),
    val errorMessage: String? = null,
    val telemetry: TelemetryData = TelemetryData(),
    val deviceInfo: DeviceInfo? = null,
    val activeTab: DashboardTab = DashboardTab.PULSE,
    val allNodes: List<VlessConfig> = emptyList(),
    val nodePings: Map<String, Long> = emptyMap(),
    val isPinging: Boolean = false,
    val updateStatus: UpdateCheckResult = UpdateCheckResult(latestVersion = "1.0"),
    val downloadState: DownloadState = DownloadState.Idle
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = VlessRepository(application)

    private val _isImportDialogOpen = MutableStateFlow(false)
    private val _isExpertPanelOpen = MutableStateFlow(false)
    private val _isDetailsDialogOpen = MutableStateFlow(false)
    private val _serverDetails = MutableStateFlow(ServerDetailsData())
    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _localConfig = MutableStateFlow<VlessConfig?>(null)
    private val _activeTab = MutableStateFlow(DashboardTab.PULSE)
    private val _allNodes = MutableStateFlow<List<VlessConfig>>(emptyList())
    private val _nodePings = MutableStateFlow<Map<String, Long>>(emptyMap())
    private val _isPinging = MutableStateFlow(false)
    private val _deviceInfo = MutableStateFlow<DeviceInfo?>(null)
    private val _updateStatus = MutableStateFlow(UpdateCheckResult(latestVersion = "1.0"))

    init {
        // Fetch Device info
        _deviceInfo.value = DeviceInfoHelper.getDeviceInfo(application)

        // Collect saved VLESS config
        viewModelScope.launch {
            repository.savedVlessConfig.collect { saved ->
                _localConfig.value = saved
                if (saved != null && TelemetryManager.telemetry.value.pingMs < 0) {
                    pingServer(saved.server, saved.port)
                }
            }
        }

        // Collect all nodes list
        viewModelScope.launch {
            repository.allNodes.collect { nodes ->
                _allNodes.value = nodes
            }
        }
    }

    val uiState: StateFlow<MainUiState> = combine(
        VpnStateManager.state,
        VpnStateManager.activeConfig,
        _localConfig,
        _isImportDialogOpen,
        _isExpertPanelOpen,
        _isDetailsDialogOpen,
        _serverDetails,
        _errorMessage,
        TelemetryManager.telemetry,
        _deviceInfo,
        _activeTab,
        _allNodes,
        _nodePings,
        _updateStatus,
        AppUpdateDownloader.downloadState
    ) { args ->
        @Suppress("UNCHECKED_CAST")
        val vpnState = args[0] as VpnState
        @Suppress("UNCHECKED_CAST")
        val activeConfig = args[1] as VlessConfig?
        @Suppress("UNCHECKED_CAST")
        val localConfig = args[2] as VlessConfig?
        val isDialogOpen = args[3] as Boolean
        val isExpertOpen = args[4] as Boolean
        val isDetailsOpen = args[5] as Boolean
        val serverDetails = args[6] as ServerDetailsData
        val error = args[7] as String?
        val telemetry = args[8] as TelemetryData
        val deviceInfo = args[9] as DeviceInfo?
        val activeTab = args[10] as DashboardTab
        @Suppress("UNCHECKED_CAST")
        val allNodes = args[11] as List<VlessConfig>
        @Suppress("UNCHECKED_CAST")
        val nodePings = args[12] as Map<String, Long>
        val updateStatus = args[13] as UpdateCheckResult
        val downloadState = args[14] as DownloadState

        val server = activeConfig ?: localConfig
        val combinedError = when {
            vpnState is VpnState.Error -> vpnState.message
            else -> error
        }

        MainUiState(
            vpnState = vpnState,
            configuredServer = server,
            isImportDialogOpen = isDialogOpen,
            isExpertPanelOpen = isExpertOpen,
            isDetailsDialogOpen = isDetailsOpen,
            serverDetails = serverDetails,
            errorMessage = combinedError,
            telemetry = telemetry,
            deviceInfo = deviceInfo,
            activeTab = activeTab,
            allNodes = allNodes,
            nodePings = nodePings,
            updateStatus = updateStatus,
            downloadState = downloadState
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainUiState()
    )

    fun selectTab(tab: DashboardTab) {
        _activeTab.value = tab
    }

    fun openImportDialog() {
        _errorMessage.value = null
        VpnStateManager.clearError()
        _isImportDialogOpen.value = true
    }

    fun closeImportDialog() {
        _isImportDialogOpen.value = false
    }

    fun openExpertPanel() {
        _isExpertPanelOpen.value = true
    }

    fun closeExpertPanel() {
        _isExpertPanelOpen.value = false
    }

    fun openServerDetails() {
        val server = uiState.value.configuredServer ?: return
        _isDetailsDialogOpen.value = true
        _serverDetails.value = ServerDetailsData(host = server.server, isLoading = true)

        viewModelScope.launch {
            val details = ServerDetailsHelper.fetchDetails(server.server)
            _serverDetails.value = details
        }
    }

    fun closeServerDetails() {
        _isDetailsDialogOpen.value = false
    }

    fun checkForUpdates() {
        viewModelScope.launch {
            _updateStatus.value = _updateStatus.value.copy(isChecking = true, errorMessage = null)
            val result = AppUpdateChecker.checkForUpdates("1.0")
            _updateStatus.value = result
        }
    }

    fun downloadAndInstallUpdate(context: Context) {
        val url = uiState.value.updateStatus.downloadUrl
        viewModelScope.launch {
            AppUpdateDownloader.downloadAndInstallApk(context, url)
        }
    }

    fun importVless(rawUrl: String): Boolean {
        return try {
            val parsed = VlessParser.parse(rawUrl)
            _localConfig.value = parsed
            _errorMessage.value = null
            VpnStateManager.clearError()
            _isImportDialogOpen.value = false

            viewModelScope.launch {
                repository.saveVlessUrl(parsed.rawUrl)
                pingServer(parsed.server, parsed.port)
            }
            true
        } catch (e: Exception) {
            _errorMessage.value = e.message ?: "Invalid VLESS URL"
            false
        }
    }

    fun pingCurrentServer() {
        val server = uiState.value.configuredServer ?: return
        pingServer(server.server, server.port)
    }

    fun pingServer(host: String, port: Int) {
        viewModelScope.launch {
            _isPinging.value = true
            val ms = TelemetryManager.measureServerPing(host, port)
            _nodePings.value = _nodePings.value + ("$host:$port" to ms)
            _isPinging.value = false
        }
    }

    fun pingAllNodes() {
        viewModelScope.launch {
            _isPinging.value = true
            val nodes = _allNodes.value
            val pingsMap = mutableMapOf<String, Long>()
            for (node in nodes) {
                val ms = TelemetryManager.measureServerPing(node.server, node.port)
                pingsMap["${node.server}:${node.port}"] = ms
            }
            _nodePings.value = pingsMap
            _isPinging.value = false
        }
    }

    fun selectNode(config: VlessConfig) {
        _localConfig.value = config
        viewModelScope.launch {
            repository.setActiveNode(config.rawUrl)
            pingServer(config.server, config.port)
        }
    }

    fun deleteNode(config: VlessConfig) {
        viewModelScope.launch {
            repository.deleteNode(config.rawUrl)
        }
    }

    fun copyConfigToClipboard(context: Context) {
        val server = uiState.value.configuredServer ?: return
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("VLESS Config", server.rawUrl)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "VLESS URL copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    fun onConnectClicked(
        context: Context,
        onRequireVpnPermission: (Intent) -> Unit
    ) {
        val currentServer = uiState.value.configuredServer
        if (currentServer == null) {
            _errorMessage.value = "No server configured. Paste a VLESS URL to begin."
            return
        }

        val state = uiState.value.vpnState
        when (state) {
            is VpnState.Connected, is VpnState.Connecting -> {
                V2rayVpnService.stopVpn(context)
            }
            is VpnState.Disconnected, is VpnState.Error -> {
                _errorMessage.value = null
                VpnStateManager.clearError()
                val prepareIntent = VpnService.prepare(context)
                if (prepareIntent != null) {
                    onRequireVpnPermission(prepareIntent)
                } else {
                    startVpnTunnel(context)
                }
            }
            is VpnState.Disconnecting -> {
                // Ignore while disconnecting
            }
        }
    }

    fun onVpnPermissionGranted(context: Context) {
        startVpnTunnel(context)
    }

    fun onVpnPermissionDenied() {
        VpnStateManager.setError("VPN permission denied. Permission is required to create secure tunnel.")
    }

    private fun startVpnTunnel(context: Context) {
        val server = uiState.value.configuredServer ?: return
        V2rayVpnService.startVpn(context, server.rawUrl)
    }

    fun clearError() {
        _errorMessage.value = null
        VpnStateManager.clearError()
    }
}
