package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.CyberBgDark
import com.example.ui.theme.CyberBgDeep
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberSurfaceLight
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary
import com.example.ui.theme.CyberTextTertiary
import com.example.util.DownloadState
import com.example.util.UpdateCheckResult

@Composable
fun ExpertPanelDialog(
    updateStatus: UpdateCheckResult,
    downloadState: DownloadState,
    onDismiss: () -> Unit,
    onCheckForUpdates: () -> Unit,
    onDownloadAndInstall: () -> Unit
) {
    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(20.dp))
                .background(CyberBgDark)
                .border(1.dp, CyberCyan.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                .padding(18.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Dialog Header
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
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Expert Panel",
                            tint = CyberCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "EXPERT PANEL",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = CyberTextPrimary,
                                letterSpacing = 1.5.sp
                            )
                            Text(
                                text = "Dilanthar's expert tools & future features",
                                fontSize = 11.sp,
                                color = CyberTextSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = CyberTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Future expert options will sit here at top of Expert Panel

                // App Updates Section (Anchored at the bottom of the Expert Panel)
                AppUpdateSectionCard(
                    updateStatus = updateStatus,
                    downloadState = downloadState,
                    onCheckForUpdates = onCheckForUpdates,
                    onDownloadAndInstall = onDownloadAndInstall
                )
            }
        }
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
