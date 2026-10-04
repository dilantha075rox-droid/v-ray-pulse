package com.example

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.HomeScreen
import com.example.ui.ImportVlessDialog
import com.example.ui.MainViewModel
import com.example.ui.theme.CyberBgDark
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onStart() {
        super.onStart()
        com.example.optimization.OptimizationManager.setAppForegroundState(true)
    }

    override fun onStop() {
        super.onStop()
        com.example.optimization.OptimizationManager.setAppForegroundState(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                // VPN permission launcher
                val vpnPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) { result ->
                    if (result.resultCode == Activity.RESULT_OK) {
                        viewModel.onVpnPermissionGranted(this)
                    } else {
                        viewModel.onVpnPermissionDenied()
                    }
                }

                // Cellular RF & Location permission launcher
                val cellularPermissionsLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { _ ->
                    viewModel.refreshRadar(this@MainActivity)
                }

                LaunchedEffect(Unit) {
                    val permissions = mutableListOf<String>()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                    if (ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                    if (ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.READ_PHONE_STATE
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        permissions.add(Manifest.permission.READ_PHONE_STATE)
                    }

                    if (permissions.isNotEmpty()) {
                        cellularPermissionsLauncher.launch(permissions.toTypedArray())
                    } else {
                        viewModel.refreshRadar(this@MainActivity)
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = CyberBgDark
                ) { _ ->
                    HomeScreen(
                        uiState = uiState,
                        onImportClick = { viewModel.openImportDialog() },
                        onConnectToggle = {
                            viewModel.onConnectClicked(this@MainActivity) { vpnIntent ->
                                vpnPermissionLauncher.launch(vpnIntent)
                            }
                        },
                        onDismissError = { viewModel.clearError() },
                        onTabSelected = { tab -> viewModel.selectTab(tab) },
                        onPingClick = { viewModel.pingCurrentServer() },
                        onCopyConfigClick = { viewModel.copyConfigToClipboard(this@MainActivity) },
                        onDetailsClick = { viewModel.openServerDetails() },
                        onCloseDetailsDialog = { viewModel.closeServerDetails() },
                        onSelectNode = { node -> viewModel.selectNode(node) },
                        onDeleteNode = { node -> viewModel.deleteNode(node) },
                        onPingAllNodes = { viewModel.pingAllNodes() },
                        onCheckForUpdates = { viewModel.checkForUpdates() },
                        onDownloadAndInstall = { viewModel.downloadAndInstallUpdate(this@MainActivity) },
                        onRadarMasterToggle = { enabled -> viewModel.setRadarMasterToggle(this@MainActivity, enabled) },
                        onExpertClick = { viewModel.selectTab(com.example.ui.DashboardTab.EXPERT) },
                        onCloseExpertPanel = { viewModel.closeExpertPanel() }
                    )

                    if (uiState.isImportDialogOpen) {
                        ImportVlessDialog(
                            onDismiss = { viewModel.closeImportDialog() },
                            onImport = { url -> viewModel.importVless(url) }
                        )
                    }
                }
            }
        }
    }
}
