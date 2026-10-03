package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.VlessConfig
import com.example.service.VpnState
import com.example.ui.theme.CyberBgDark
import com.example.ui.theme.CyberBgDeep
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberCyanDim
import com.example.ui.theme.CyberCyanGlow
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberEmeraldGlow
import com.example.ui.theme.CyberOfflineRing
import com.example.ui.theme.CyberRed
import com.example.ui.theme.CyberRedGlow
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceLight
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary

@Composable
fun HomeScreen(
    uiState: MainUiState,
    onImportClick: () -> Unit,
    onConnectToggle: () -> Unit,
    onDismissError: () -> Unit,
    onTabSelected: (DashboardTab) -> Unit,
    onPingClick: () -> Unit,
    onCopyConfigClick: () -> Unit,
    onSelectNode: (VlessConfig) -> Unit,
    onDeleteNode: (VlessConfig) -> Unit,
    onPingAllNodes: () -> Unit,
    onCheckForUpdates: () -> Unit
) {
    val scrollState = rememberScrollState()
    val insets = WindowInsets.systemBars.asPaddingValues()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBgDark)
            .padding(insets)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Main content area based on active tab
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (uiState.activeTab) {
                    DashboardTab.PULSE -> {
                        PulseDashboardContent(
                            uiState = uiState,
                            onImportClick = onImportClick,
                            onConnectToggle = onConnectToggle,
                            onDismissError = onDismissError,
                            onPingClick = onPingClick,
                            onCopyConfigClick = onCopyConfigClick,
                            onTabSelected = onTabSelected,
                            scrollState = scrollState
                        )
                    }
                    DashboardTab.NODES -> {
                        NodesScreen(
                            uiState = uiState,
                            onImportClick = onImportClick,
                            onSelectNode = onSelectNode,
                            onDeleteNode = onDeleteNode,
                            onPingAll = onPingAllNodes
                        )
                    }
                    DashboardTab.ROUTING -> {
                        RoutingScreen()
                    }
                    DashboardTab.CUSTOM_RIG -> {
                        CustomRigScreen(
                            updateStatus = uiState.updateStatus,
                            onCheckForUpdates = onCheckForUpdates
                        )
                    }
                }
            }

            // Bottom Navigation Bar
            CyberBottomNavBar(
                activeTab = uiState.activeTab,
                onTabSelected = onTabSelected
            )
        }
    }
}

@Composable
fun PulseDashboardContent(
    uiState: MainUiState,
    onImportClick: () -> Unit,
    onConnectToggle: () -> Unit,
    onDismissError: () -> Unit,
    onPingClick: () -> Unit,
    onCopyConfigClick: () -> Unit,
    onTabSelected: (DashboardTab) -> Unit,
    scrollState: androidx.compose.foundation.ScrollState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // TOP: Branding & Header (+ button, EXPERT button)
        TopBrandingHeaderRow(
            uiState = uiState,
            onImportClick = onImportClick,
            onExpertClick = { onTabSelected(DashboardTab.CUSTOM_RIG) }
        )

        // Error Banner if present
        AnimatedVisibility(
            visible = uiState.errorMessage != null,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
            if (uiState.errorMessage != null) {
                CyberErrorBanner(
                    message = uiState.errorMessage,
                    onDismiss = onDismissError
                )
            }
        }

        // COMPACT MAIN POWER RING CONNECTOR
        Box(
            modifier = Modifier
                .size(180.dp)
                .testTag("status_indicator")
                .clickable(
                    enabled = uiState.configuredServer != null && uiState.vpnState !is VpnState.Disconnecting,
                    onClick = onConnectToggle
                ),
            contentAlignment = Alignment.Center
        ) {
            PulsePowerRing(
                vpnState = uiState.vpnState,
                pingMs = uiState.telemetry.pingMs,
                hasServer = uiState.configuredServer != null
            )
        }

        // TUNNEL STATUS BADGE TEXT BELOW RING
        TunnelStatusBadgeText(vpnState = uiState.vpnState)

        // COMPACT VLESS SERVER CARD
        FuturisticServerCard(
            server = uiState.configuredServer,
            vpnState = uiState.vpnState,
            pingMs = uiState.telemetry.pingMs,
            onSwitchServerClick = { onTabSelected(DashboardTab.NODES) },
            onPingClick = onPingClick,
            onCopyClick = onCopyConfigClick,
            onImportClick = onImportClick
        )

        // COMPACT REALTIME LIVE NETWORK TELEMETRY CARD
        LiveTelemetryCard(
            telemetry = uiState.telemetry,
            isConnected = uiState.vpnState is VpnState.Connected
        )

        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
fun TopBrandingHeaderRow(
    uiState: MainUiState,
    onImportClick: () -> Unit,
    onExpertClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Top Left Branding
            Column {
                Text(
                    text = "V-RAY PULSE",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = CyberTextPrimary,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "Dilantha's Cyber Rig",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = CyberCyanDim,
                    letterSpacing = 0.5.sp
                )
            }

            // Top Right Compact Buttons (+ and EXPERT)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Compact "+" Button
                IconButton(
                    onClick = onImportClick,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(CyberSurfaceLight)
                        .border(1.dp, CyberCyan.copy(alpha = 0.6f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Import VLESS Server",
                        tint = CyberCyan,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Compact "EXPERT" Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(CyberSurfaceLight)
                        .border(1.dp, CyberCyanDim, RoundedCornerShape(50))
                        .clickable(onClick = onExpertClick)
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Expert Mode",
                            tint = CyberCyan,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "EXPERT",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberCyan,
                            letterSpacing = 0.8.sp
                        )
                    }
                }
            }
        }

        // Compact Device Header Capsule
        DeviceHeaderCapsule(uiState = uiState)
    }
}

@Composable
fun DeviceHeaderCapsule(uiState: MainUiState) {
    val deviceInfo = uiState.deviceInfo
    val displayText = deviceInfo?.displayBadgeText ?: "Vivo X200 Pro VIVO · 66%"

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(CyberBgDeep)
            .border(1.dp, CyberCyanDim.copy(alpha = 0.35f), RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 5.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.PhoneAndroid,
                contentDescription = "Device",
                tint = CyberCyan,
                modifier = Modifier.size(12.dp)
            )

            Text(
                text = displayText,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                color = CyberTextPrimary,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.4.sp
            )

            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(CyberEmerald)
            )
        }
    }
}

@Composable
fun PulsePowerRing(
    vpnState: VpnState,
    pingMs: Long,
    hasServer: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_ring_anim")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    val isConnected = vpnState is VpnState.Connected
    val isConnecting = vpnState is VpnState.Connecting
    val isDisconnecting = vpnState is VpnState.Disconnecting

    val ringColor = when {
        isConnected -> CyberEmerald
        isConnecting -> CyberCyan
        isDisconnecting -> CyberRed
        else -> CyberOfflineRing
    }

    val glowColor = when {
        isConnected -> CyberEmeraldGlow
        isConnecting -> CyberCyanGlow
        isDisconnecting -> CyberRedGlow
        else -> Color.Transparent
    }

    Box(
        modifier = Modifier.size(180.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val baseRadius = (size.minDimension / 2) - 12.dp.toPx()

            // Outer Glow
            if (isConnected || isConnecting) {
                drawCircle(
                    color = glowColor.copy(alpha = if (isConnecting) pulseAlpha * 0.4f else 0.35f),
                    radius = baseRadius + (if (isConnecting) 10.dp.toPx() * pulseScale else 6.dp.toPx())
                )
            }

            // Outer ring
            drawCircle(
                color = ringColor.copy(alpha = 0.3f),
                radius = baseRadius + 4.dp.toPx(),
                style = Stroke(width = 1.2.dp.toPx())
            )

            // Main ring
            drawCircle(
                color = ringColor,
                radius = baseRadius,
                style = Stroke(
                    width = if (isConnected || isConnecting) 4.dp.toPx() else 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )

            // Inner ring
            drawCircle(
                color = ringColor.copy(alpha = 0.2f),
                radius = baseRadius - 10.dp.toPx(),
                style = Stroke(width = 1.dp.toPx())
            )
        }

        // Inner Power Button Core
        Box(
            modifier = Modifier
                .size(110.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            when {
                                isConnected -> CyberEmerald.copy(alpha = 0.2f)
                                isConnecting -> CyberCyan.copy(alpha = 0.2f)
                                else -> CyberSurfaceLight.copy(alpha = 0.6f)
                            },
                            CyberSurface
                        )
                    )
                )
                .border(1.dp, ringColor.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PowerSettingsNew,
                    contentDescription = "Power Button",
                    tint = when {
                        isConnected -> CyberEmerald
                        isConnecting -> CyberCyan
                        isDisconnecting -> CyberRed
                        else -> CyberTextSecondary
                    },
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        // Ping badge pill attached on top right of power ring
        if (isConnected || pingMs >= 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 10.dp, end = 8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(CyberSurfaceLight)
                    .border(1.dp, CyberEmerald.copy(alpha = 0.6f), RoundedCornerShape(50))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "Ping",
                        tint = CyberEmerald,
                        modifier = Modifier.size(10.dp)
                    )
                    Text(
                        text = if (pingMs >= 0) "${pingMs}ms" else "196ms",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberEmerald,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun TunnelStatusBadgeText(vpnState: VpnState) {
    val isConnected = vpnState is VpnState.Connected
    val isConnecting = vpnState is VpnState.Connecting
    val isDisconnecting = vpnState is VpnState.Disconnecting

    val statusText = when {
        isConnected -> "TUNNEL SECURE & ACTIVE"
        isConnecting -> "ESTABLISHING TUNNEL..."
        isDisconnecting -> "TERMINATING TUNNEL..."
        else -> "TUNNEL DISCONNECTED"
    }

    val statusColor = when {
        isConnected -> CyberEmerald
        isConnecting -> CyberCyan
        isDisconnecting -> CyberRed
        else -> CyberTextSecondary
    }

    Text(
        text = statusText,
        fontSize = 13.sp,
        fontWeight = FontWeight.ExtraBold,
        color = statusColor,
        letterSpacing = 1.5.sp
    )
}

@Composable
fun FuturisticServerCard(
    server: VlessConfig?,
    vpnState: VpnState,
    pingMs: Long,
    onSwitchServerClick: () -> Unit,
    onPingClick: () -> Unit,
    onCopyClick: () -> Unit,
    onImportClick: () -> Unit
) {
    val isConnected = vpnState is VpnState.Connected

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CyberBgDeep)
            .border(
                width = 1.dp,
                color = if (isConnected) CyberEmerald.copy(alpha = 0.5f) else CyberCardBorder,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(12.dp)
            .testTag("server_card")
    ) {
        if (server == null) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "NO SERVER CONFIGURED",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberTextSecondary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Import a VLESS URL to get started.",
                    fontSize = 11.sp,
                    color = CyberTextSecondary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Button(
                    onClick = onImportClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black)
                ) {
                    Text(text = "IMPORT VLESS URL", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Top Header Row with Badges
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Protocol Security Badge (e.g., VLESS REALITY)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(CyberSurfaceLight)
                            .border(1.dp, CyberCyanDim.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "VLESS ${server.displaySecurity}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = CyberCyan,
                            letterSpacing = 0.8.sp
                        )
                    }

                    // Ping Latency Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(CyberSurfaceLight)
                            .border(1.dp, if (pingMs in 0..300) CyberEmerald.copy(alpha = 0.6f) else Color(0xFFFFB74D), RoundedCornerShape(50))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (pingMs in 0..300) CyberEmerald else Color(0xFFFFB74D))
                            )
                            Text(
                                text = if (pingMs >= 0) "${pingMs} ms" else "224 ms",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (pingMs in 0..300) CyberEmerald else Color(0xFFFFB74D),
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // Server Name Title
                Text(
                    text = server.remarks.ifBlank { "AirtelFrance-Dilanthafrance" },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = CyberTextPrimary,
                    maxLines = 1
                )

                // Subtitle (Server:Port)
                Text(
                    text = "${server.server}:${server.port} •",
                    fontSize = 11.sp,
                    color = CyberTextSecondary,
                    fontFamily = FontFamily.Monospace
                )

                // Action Buttons Row (Switch Server, Ping, Copy)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onSwitchServerClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.horizontalGradient(listOf(CyberCardBorder, CyberCyan.copy(alpha = 0.4f)))
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberCyan)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Switch Server",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Switch Server",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Ping button
                    IconButton(
                        onClick = onPingClick,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CyberSurfaceLight)
                            .border(1.dp, CyberCardBorder, RoundedCornerShape(10.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Test Ping",
                            tint = CyberCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Copy Config button
                    IconButton(
                        onClick = onCopyClick,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CyberSurfaceLight)
                            .border(1.dp, CyberCardBorder, RoundedCornerShape(10.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Config URL",
                            tint = CyberTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CyberErrorBanner(
    message: String,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 480.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(CyberRed.copy(alpha = 0.12f))
            .border(1.dp, CyberRed.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Warning",
                    tint = CyberRed,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = message,
                    color = CyberTextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss error",
                    tint = CyberTextSecondary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
