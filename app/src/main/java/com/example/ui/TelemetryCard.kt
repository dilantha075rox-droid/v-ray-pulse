package com.example.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TelemetryData
import com.example.ui.theme.CyberBgDeep
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceLight
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary
import com.example.ui.theme.CyberTextTertiary
import kotlin.math.sin

val CyberPurple = Color(0xFFA855F7)

@Composable
fun LiveTelemetryCard(
    telemetry: TelemetryData,
    isConnected: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CyberBgDeep)
            .border(1.dp, CyberCardBorder, RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LIVE NETWORK TELEMETRY",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = CyberCyan,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = "PORT: ${telemetry.port} ${telemetry.proxyType}",
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberTextTertiary,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.5.sp
                )
            }

            // Speed Indicators Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Download Speed
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "↓ ",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = CyberCyan
                        )
                        Text(
                            text = "DOWNLOAD",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberTextSecondary,
                            letterSpacing = 0.8.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = if (isConnected) telemetry.formattedDownSpeed else "0.00 KB/s",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CyberCyan,
                        letterSpacing = 0.5.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Upload Speed
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "↑ ",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = CyberEmerald
                        )
                        Text(
                            text = "UPLOAD",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberTextSecondary,
                            letterSpacing = 0.8.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = if (isConnected) telemetry.formattedUpSpeed else "0.00 KB/s",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CyberEmerald,
                        letterSpacing = 0.5.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Compact Real-time Wave Canvas
            TelemetryWaveCanvas(
                downSpeed = telemetry.downSpeedBytes,
                upSpeed = telemetry.upSpeedBytes,
                isConnected = isConnected,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
            )

            // Compact Stat Cards Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                TelemetryStatBox(
                    icon = Icons.Default.Timer,
                    label = "SESSION UPTIME",
                    value = if (isConnected) telemetry.formattedUptime else "00:00",
                    accentColor = CyberCyan,
                    modifier = Modifier.weight(1f)
                )

                TelemetryStatBox(
                    icon = Icons.Default.CloudDownload,
                    label = "TOTAL DOWNLOAD",
                    value = if (isConnected) telemetry.formattedTotalDown else "0 B",
                    accentColor = CyberCyan,
                    modifier = Modifier.weight(1.1f)
                )

                TelemetryStatBox(
                    icon = Icons.Default.CloudUpload,
                    label = "TOTAL UPLOAD",
                    value = if (isConnected) telemetry.formattedTotalUp else "0 B",
                    accentColor = CyberEmerald,
                    modifier = Modifier.weight(1.1f)
                )
            }
        }
    }
}

@Composable
fun TelemetryWaveCanvas(
    downSpeed: Long,
    upSpeed: Long,
    isConnected: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave_phase")

    val phaseShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(CyberSurface)
    ) {
        val width = size.width
        val height = size.height
        val centerY = height / 2f

        if (width <= 0f || height <= 0f) return@Canvas

        val downAmp = if (isConnected) {
            val normalized = (downSpeed / (1024f * 1024f)).coerceIn(0.1f, 1f)
            (8.dp.toPx() + normalized * 10.dp.toPx())
        } else {
            3.dp.toPx()
        }

        val upAmp = if (isConnected) {
            val normalized = (upSpeed / (1024f * 512f)).coerceIn(0.1f, 1f)
            (6.dp.toPx() + normalized * 8.dp.toPx())
        } else {
            2.dp.toPx()
        }

        // Center line
        drawLine(
            color = CyberCardBorder.copy(alpha = 0.4f),
            start = Offset(0f, centerY),
            end = Offset(width, centerY),
            strokeWidth = 1.dp.toPx()
        )

        // Wave 1: Download (Cyan Wave)
        val pathDown = Path()
        val step = 10f
        var x = 0f
        var first = true

        while (x <= width) {
            val angle = (x / width) * 4 * Math.PI + phaseShift
            val y = centerY + sin(angle).toFloat() * downAmp

            if (first) {
                pathDown.moveTo(x, y)
                first = false
            } else {
                pathDown.lineTo(x, y)
            }
            x += step
        }

        drawPath(
            path = pathDown,
            color = CyberCyan.copy(alpha = if (isConnected) 0.9f else 0.35f),
            style = Stroke(
                width = if (isConnected) 2.dp.toPx() else 1.2.dp.toPx()
            )
        )

        // Wave 2: Upload (Purple Wave)
        val pathUp = Path()
        x = 0f
        first = true
        val phaseOffsetUp = phaseShift + (Math.PI / 2).toFloat()

        while (x <= width) {
            val angle = (x / width) * 5 * Math.PI - phaseOffsetUp
            val y = centerY + sin(angle).toFloat() * upAmp

            if (first) {
                pathUp.moveTo(x, y)
                first = false
            } else {
                pathUp.lineTo(x, y)
            }
            x += step
        }

        drawPath(
            path = pathUp,
            color = CyberPurple.copy(alpha = if (isConnected) 0.85f else 0.3f),
            style = Stroke(
                width = if (isConnected) 1.8.dp.toPx() else 1.dp.toPx()
            )
        )
    }
}

@Composable
fun TelemetryStatBox(
    icon: ImageVector,
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(CyberSurfaceLight)
            .border(1.dp, CyberCardBorder, RoundedCornerShape(10.dp))
            .padding(6.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = accentColor,
                    modifier = Modifier.size(10.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = label,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberTextTertiary,
                    letterSpacing = 0.2.sp,
                    maxLines = 1
                )
            }

            Text(
                text = value,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CyberTextPrimary,
                fontFamily = FontFamily.Monospace,
                maxLines = 1
            )
        }
    }
}
