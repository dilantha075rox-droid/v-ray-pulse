package com.example.ui

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.optimization.BatteryProfile
import com.example.optimization.OptimizationManager
import com.example.optimization.UpdateRate
import com.example.ui.theme.CyberBgDark
import com.example.ui.theme.CyberBgDeep
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberCyanDim
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberSurfaceLight
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary
import com.example.ui.theme.CyberTextTertiary
import kotlinx.coroutines.launch

@Composable
fun OptimizationScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBgDark)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        OptimizationScreenContent(onBackClick = onBackClick)
    }
}

@Composable
fun OptimizationScreenContent(
    onBackClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val settings by OptimizationManager.settings.collectAsState()
    val batteryStatus = remember(settings) { OptimizationManager.getSystemBatteryStatus(context) }
    val isVivo = remember { OptimizationManager.isVivoDevice() }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Screen Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBackClick != null) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = CyberCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
            }

            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "Optimization",
                        tint = CyberCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "OPTIMIZATION",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CyberTextPrimary,
                        letterSpacing = 1.2.sp
                    )
                }
                Text(
                    text = "Reduce unnecessary battery & background activity while keeping VPN stable.",
                    fontSize = 11.sp,
                    color = CyberTextSecondary
                )
            }
        }

        // Master Toggle Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(CyberBgDeep)
                .border(
                    1.dp,
                    if (settings.masterEnabled) CyberCyan else CyberCardBorder,
                    RoundedCornerShape(14.dp)
                )
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "MASTER OPTIMIZATION TOGGLE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (settings.masterEnabled) CyberCyan else CyberTextSecondary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (settings.masterEnabled) "Optional optimizations active" else "Optimizations disabled (VPN remains 100% active)",
                        fontSize = 10.5.sp,
                        color = CyberTextSecondary
                    )
                }

                Switch(
                    checked = settings.masterEnabled,
                    onCheckedChange = { enabled ->
                        scope.launch { OptimizationManager.updateMaster(enabled, context) }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = CyberEmerald,
                        checkedTrackColor = CyberEmerald.copy(alpha = 0.3f),
                        uncheckedThumbColor = CyberTextTertiary,
                        uncheckedTrackColor = CyberSurfaceLight
                    )
                )
            }
        }

        // Battery Profile Selector
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(CyberBgDeep)
                .border(1.dp, CyberCardBorder, RoundedCornerShape(14.dp))
                .padding(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "BATTERY PROFILE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberCyan,
                    letterSpacing = 1.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    BatteryProfileOption(
                        label = "MAXIMUM",
                        isSelected = settings.profile == BatteryProfile.MAXIMUM,
                        onSelect = {
                            scope.launch { OptimizationManager.applyProfile(BatteryProfile.MAXIMUM, context) }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    BatteryProfileOption(
                        label = "BALANCED",
                        isSelected = settings.profile == BatteryProfile.BALANCED,
                        onSelect = {
                            scope.launch { OptimizationManager.applyProfile(BatteryProfile.BALANCED, context) }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    BatteryProfileOption(
                        label = "PERFORMANCE",
                        isSelected = settings.profile == BatteryProfile.PERFORMANCE,
                        onSelect = {
                            scope.launch { OptimizationManager.applyProfile(BatteryProfile.PERFORMANCE, context) }
                        },
                        modifier = Modifier.weight(1.1f)
                    )
                }

                Text(
                    text = when (settings.profile) {
                        BatteryProfile.MAXIMUM -> "Maximum battery saving. Reduces UI refresh rate, animations, and non-essential calculations."
                        BatteryProfile.BALANCED -> "Recommended everyday mode. Fully responsive VPN with efficient background management."
                        BatteryProfile.PERFORMANCE -> "Frequent UI updates & live monitoring while the app is foregrounded."
                    },
                    fontSize = 10.5.sp,
                    color = CyberTextSecondary
                )
            }
        }

        // Screen-Off Optimization Toggle
        OptToggleCard(
            title = "SCREEN-OFF OPTIMIZATION",
            subtitle = "Reduce non-essential app activity while screen is off. Locking phone NEVER disconnects VPN.",
            checked = settings.screenOffOpt,
            enabled = settings.masterEnabled,
            onCheckedChange = {
                scope.launch { OptimizationManager.updateSetting(context, screenOff = it) }
            }
        )

        // Background App Optimization Toggle
        OptToggleCard(
            title = "BACKGROUND OPTIMIZATION",
            subtitle = "Pause UI refreshes when V-RAY PULSE is not visible. VPN tunnel continues normally.",
            checked = settings.backgroundOpt,
            enabled = settings.masterEnabled,
            onCheckedChange = {
                scope.launch { OptimizationManager.updateSetting(context, background = it) }
            }
        )

        // Live Traffic Monitor Toggle
        OptToggleCard(
            title = "LIVE TRAFFIC MONITOR",
            subtitle = "Show live upload/download speed information on dashboard.",
            checked = settings.liveTrafficMonitor,
            enabled = settings.masterEnabled,
            onCheckedChange = {
                scope.launch { OptimizationManager.updateSetting(context, trafficMonitor = it) }
            }
        )

        // Dashboard Update Rate Selector
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(CyberBgDeep)
                .border(1.dp, CyberCardBorder, RoundedCornerShape(14.dp))
                .padding(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "DASHBOARD UPDATE RATE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberCyan,
                    letterSpacing = 1.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    RateOption(
                        label = "LOW (~3s)",
                        isSelected = settings.updateRate == UpdateRate.LOW,
                        onSelect = {
                            scope.launch { OptimizationManager.updateSetting(context, updateRate = UpdateRate.LOW) }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    RateOption(
                        label = "BALANCED",
                        isSelected = settings.updateRate == UpdateRate.BALANCED,
                        onSelect = {
                            scope.launch { OptimizationManager.updateSetting(context, updateRate = UpdateRate.BALANCED) }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    RateOption(
                        label = "HIGH",
                        isSelected = settings.updateRate == UpdateRate.HIGH,
                        onSelect = {
                            scope.launch { OptimizationManager.updateSetting(context, updateRate = UpdateRate.HIGH) }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // UI Animations Toggle
        OptToggleCard(
            title = "UI ANIMATIONS",
            subtitle = "Enable decorative radar & wave animations on main dashboard.",
            checked = settings.uiAnimations,
            enabled = settings.masterEnabled,
            onCheckedChange = {
                scope.launch { OptimizationManager.updateSetting(context, animations = it) }
            }
        )

        // CPU Optimization Toggle
        OptToggleCard(
            title = "CPU OPTIMIZATION",
            subtitle = "Avoid unnecessary loops, polling, and object allocations.",
            checked = settings.cpuOptimization,
            enabled = settings.masterEnabled,
            onCheckedChange = {
                scope.launch { OptimizationManager.updateSetting(context, cpu = it) }
            }
        )

        // Memory Optimization Toggle
        OptToggleCard(
            title = "MEMORY OPTIMIZATION",
            subtitle = "Efficient resource management and lifecycle cleanup.",
            checked = settings.memoryOptimization,
            enabled = settings.masterEnabled,
            onCheckedChange = {
                scope.launch { OptimizationManager.updateSetting(context, memory = it) }
            }
        )

        // Android System Battery Status Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(CyberBgDeep)
                .border(1.dp, CyberCardBorder, RoundedCornerShape(14.dp))
                .padding(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.BatteryChargingFull,
                            contentDescription = "Battery",
                            tint = CyberEmerald,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "ANDROID BATTERY STATUS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberCyan
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(CyberSurfaceLight)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = batteryStatus,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberEmerald,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                OutlinedButton(
                    onClick = {
                        try {
                            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            try {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.parse("package:${context.packageName}")
                                }
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(CyberCyanDim)
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberCyan)
                ) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings", modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "OPEN BATTERY SETTINGS", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Vivo / OriginOS Informational Guidance Card
        if (isVivo) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(CyberBgDeep)
                    .border(1.dp, CyberCyan.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhoneAndroid,
                            contentDescription = "Vivo",
                            tint = CyberCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "VIVO / ORIGINOS DETECTED",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberCyan
                        )
                    }

                    Text(
                        text = "For best long-running VPN stability, review V-RAY PULSE background and battery permissions in OriginOS system settings.",
                        fontSize = 10.5.sp,
                        color = CyberTextSecondary
                    )

                    Button(
                        onClick = {
                            try {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.parse("package:${context.packageName}")
                                }
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "REVIEW ORIGINOS PERMISSIONS", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun BatteryProfileOption(
    label: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) CyberCyan else CyberSurfaceLight)
            .border(1.dp, if (isSelected) CyberCyan else CyberCardBorder, RoundedCornerShape(8.dp))
            .clickable(onClick = onSelect)
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (isSelected) Color.Black else CyberTextSecondary
        )
    }
}

@Composable
fun RateOption(
    label: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) CyberCyan else CyberSurfaceLight)
            .border(1.dp, if (isSelected) CyberCyan else CyberCardBorder, RoundedCornerShape(8.dp))
            .clickable(onClick = onSelect)
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.Black else CyberTextSecondary
        )
    }
}

@Composable
fun OptToggleCard(
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CyberBgDeep)
            .border(1.dp, CyberCardBorder, RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (enabled) CyberTextPrimary else CyberTextTertiary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 10.5.sp,
                    color = CyberTextSecondary
                )
            }

            Switch(
                checked = checked && enabled,
                onCheckedChange = onCheckedChange,
                enabled = enabled,
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
