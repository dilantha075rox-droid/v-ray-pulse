package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberBgDark
import com.example.ui.theme.CyberBgDeep
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberSurfaceLight
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary
import com.example.ui.theme.CyberTextTertiary
import com.example.util.DownloadState
import com.example.util.UpdateCheckResult

@Composable
fun CustomRigScreen(
    updateStatus: UpdateCheckResult = UpdateCheckResult(),
    downloadState: DownloadState = DownloadState.Idle,
    onCheckForUpdates: () -> Unit = {},
    onDownloadAndInstall: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var allowInsecure by remember { mutableStateOf(false) }
    var enableMux by remember { mutableStateOf(true) }
    var enableFragment by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBgDark)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = "EXPERT RIG PANEL",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CyberTextPrimary,
                letterSpacing = 1.5.sp
            )
            Text(
                text = "Dilanthar's core Xray tuning & inbound configuration",
                fontSize = 12.sp,
                color = CyberTextSecondary
            )
        }

        // Port Summary Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CyberBgDeep)
                .border(1.dp, CyberCardBorder, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "LOCAL INBOUND PORTS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberCyan,
                    letterSpacing = 1.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    RigSettingItem(label = "SOCKS5 Port", value = "10808", modifier = Modifier.weight(1f))
                    RigSettingItem(label = "HTTP Proxy Port", value = "10809", modifier = Modifier.weight(1f))
                    RigSettingItem(label = "MTU", value = "1500", modifier = Modifier.weight(1f))
                }
            }
        }

        // Toggle Switches (Future features added above bottom)
        ToggleSettingCard(
            title = "MUX Multiplexing",
            subtitle = "Reuse TCP connections to reduce handshake latency",
            checked = enableMux,
            onCheckedChange = { enableMux = it }
        )

        ToggleSettingCard(
            title = "TLS / REALITY Insecure",
            subtitle = "Skip certificate validation (Use with caution)",
            checked = allowInsecure,
            onCheckedChange = { allowInsecure = it }
        )

        ToggleSettingCard(
            title = "TLS Fragmenting",
            subtitle = "Split ClientHello packets to evade SNI detection",
            checked = enableFragment,
            onCheckedChange = { enableFragment = it }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // BOTTOM SECTION: App Updates Card (In-app direct download & install)
        AppUpdateSectionCard(
            updateStatus = updateStatus,
            downloadState = downloadState,
            onCheckForUpdates = onCheckForUpdates,
            onDownloadAndInstall = onDownloadAndInstall
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun AppUpdateSectionCard(
    updateStatus: UpdateCheckResult,
    downloadState: DownloadState,
    onCheckForUpdates: () -> Unit,
    onDownloadAndInstall: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CyberBgDeep)
            .border(1.dp, CyberCyan.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SystemUpdate,
                        contentDescription = "Updates",
                        tint = CyberCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "SOFTWARE & APP UPDATES",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CyberCyan,
                        letterSpacing = 1.2.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(CyberSurfaceLight)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "GITHUB IN-APP UPDATE",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberEmerald,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Version info row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "CURRENT VERSION",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberTextTertiary
                    )
                    Text(
                        text = "v1.0 (Build 1)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CyberTextPrimary,
                        fontFamily = FontFamily.Monospace
                    )
                }

                if (updateStatus.lastCheckedText.isNotBlank()) {
                    Text(
                        text = updateStatus.lastCheckedText,
                        fontSize = 11.sp,
                        color = CyberTextSecondary,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Status notification box if checked
            if (updateStatus.isUpToDate && !updateStatus.isChecking && downloadState is DownloadState.Idle) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(CyberEmerald.copy(alpha = 0.12f))
                        .border(1.dp, CyberEmerald.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Up to date",
                            tint = CyberEmerald,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "V-RAY PULSE is fully updated to the latest version.",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = CyberTextPrimary
                        )
                    }
                }
            } else if (updateStatus.updateAvailable && downloadState is DownloadState.Idle) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(CyberCyan.copy(alpha = 0.15f))
                        .border(1.dp, CyberCyan, RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "🚀 New Update Available: ${updateStatus.latestVersion}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberCyan
                        )
                        if (updateStatus.releaseNotes.isNotBlank()) {
                            Text(
                                text = updateStatus.releaseNotes,
                                fontSize = 11.sp,
                                color = CyberTextSecondary
                            )
                        }
                    }
                }
            }

            // Real-time In-App Download Progress Indicator
            if (downloadState is DownloadState.Downloading) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Downloading APK update in-app...",
                            fontSize = 11.sp,
                            color = CyberCyan,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${downloadState.progressPercent}%",
                            fontSize = 11.sp,
                            color = CyberCyan,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    LinearProgressIndicator(
                        progress = { downloadState.progressPercent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = CyberCyan,
                        trackColor = CyberSurfaceLight
                    )
                }
            }

            // Action Button: CHECK FOR UPDATES or IN-APP DOWNLOAD & INSTALL
            Button(
                onClick = {
                    if (updateStatus.updateAvailable) {
                        onDownloadAndInstall()
                    } else {
                        onCheckForUpdates()
                    }
                },
                enabled = downloadState !is DownloadState.Downloading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyberCyan,
                    contentColor = Color.Black
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (updateStatus.isChecking) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.Black,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CHECKING GITHUB...",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else if (downloadState is DownloadState.Downloading) {
                        Text(
                            text = "DOWNLOADING UPDATE...",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else if (updateStatus.updateAvailable) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = "Download & Install",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "DOWNLOAD & INSTALL UPDATE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Check for updates",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "CHECK FOR UPDATES",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RigSettingItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(text = label.uppercase(), fontSize = 10.sp, color = CyberTextSecondary)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = CyberTextPrimary,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun ToggleSettingCard(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CyberBgDeep)
            .border(1.dp, CyberCardBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CyberTextPrimary)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = subtitle, fontSize = 11.sp, color = CyberTextSecondary)
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = CyberEmerald,
                    checkedTrackColor = CyberEmerald.copy(alpha = 0.3f),
                    uncheckedThumbColor = CyberTextTertiary,
                    uncheckedTrackColor = CyberSurfaceLight
                )
            )
        }
    }
}
