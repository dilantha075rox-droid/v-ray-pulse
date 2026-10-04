package com.example.ui

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.radar.CellularRadarData
import com.example.ui.theme.CyberBgDeep
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberSurfaceLight
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary
import com.example.ui.theme.CyberTextTertiary

@Composable
fun CellularRadarSectionCard(
    radarData: CellularRadarData,
    onMasterToggleChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CyberBgDeep)
            .border(1.dp, CyberCyan.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row (Matches Screenshot 1: ISP & CELLULAR RF RADAR, LIVE HW PROBE, Master Switch)
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
                        imageVector = Icons.Default.Radar,
                        contentDescription = "Radar",
                        tint = CyberCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "ISP & CELLULAR RF RADAR",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CyberCyan,
                        letterSpacing = 1.2.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(CyberSurfaceLight)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "LIVE HW PROBE",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberCyan,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Switch(
                        checked = radarData.isMasterOn,
                        onCheckedChange = onMasterToggleChange,
                        modifier = Modifier.size(width = 36.dp, height = 20.dp),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CyberEmerald,
                            checkedTrackColor = CyberEmerald.copy(alpha = 0.3f),
                            uncheckedThumbColor = CyberTextTertiary,
                            uncheckedTrackColor = CyberSurfaceLight
                        )
                    )
                }
            }

            AnimatedVisibility(visible = radarData.isMasterOn) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // MOBILE OPERATOR / ISP Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(alpha = 0.4f))
                            .border(1.dp, CyberCardBorder, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "MOBILE OPERATOR / ISP",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyberTextTertiary,
                                    letterSpacing = 0.8.sp
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SignalCellularAlt,
                                        contentDescription = "Signal",
                                        tint = CyberEmerald,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "${radarData.signalDbm} dBm",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = CyberEmerald,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Text(
                                    text = radarData.operatorName,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = CyberTextPrimary
                                )

                                Text(
                                    text = "${radarData.signalRating} (${radarData.signalPercent}%)",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyberEmerald,
                                    letterSpacing = 0.5.sp
                                )
                            }

                            Text(
                                text = radarData.networkType,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberCyan
                            )
                        }
                    }

                    // SERVING CELL (CONNECTED BAND) Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(alpha = 0.4f))
                            .border(1.dp, CyberCardBorder, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "SERVING CELL (CONNECTED BAND)",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyberTextTertiary,
                                    letterSpacing = 0.8.sp
                                )

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(CyberEmerald)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "CONNECTED",
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.Black
                                    )
                                }
                            }

                            Text(
                                text = radarData.servingBand,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = CyberTextPrimary
                            )

                            Text(
                                text = "EARFCN: ${radarData.earfcn}  •  PCI: ${radarData.pci}  •  TAC: ${radarData.tac}",
                                fontSize = 10.5.sp,
                                color = CyberTextSecondary,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // RF Metrics Rows (Matches NetMonster Net RF Parameters)
                    RadarMetricRow(
                        icon = Icons.Default.CompassCalibration,
                        label = "TOWER DISTANCE",
                        value = "~${radarData.towerDistanceMeters} meters (${String.format(java.util.Locale.US, "%.1f", radarData.towerDistanceMeters / 1000.0)} km) (TA: ${radarData.timingAdvance})"
                    )

                    RadarMetricRow(
                        icon = Icons.Default.CellTower,
                        label = "BASE STATION IDENTITY",
                        value = "eNB: ${radarData.enbId}  •  CID: ${radarData.cellId}  •  CI: ${radarData.ci}"
                    )

                    RadarMetricRow(
                        icon = Icons.Default.SignalCellularAlt,
                        label = "RF SIGNAL METRICS",
                        value = "RSRP: ${radarData.rsrp} dBm  •  RSRQ: ${radarData.rsrq} dB  •  RSSI: ${radarData.rssi} dBm  •  SNR: ${radarData.sinr} dB"
                    )

                    // AVAILABLE BANDS (NEIGHBOR CELLS & CA)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "WHICH BANDS AVAILABLE (NEIGHBOR CELLS & CA)",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberTextTertiary,
                            letterSpacing = 0.8.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            radarData.availableBands.forEach { band ->
                                val isActive = band == radarData.activeBand
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isActive) CyberEmerald.copy(alpha = 0.15f) else CyberSurfaceLight)
                                        .border(
                                            1.dp,
                                            if (isActive) CyberEmerald else CyberCardBorder,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = band,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isActive) CyberEmerald else CyberTextPrimary
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Carrier Aggregation:",
                                fontSize = 11.sp,
                                color = CyberTextSecondary
                            )
                            Text(
                                text = radarData.carrierAggregation,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = CyberCyan
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Embedded ISP PORT ACCESSIBILITY Inside RF Radar Section Card
                    PortAccessibilityCard(
                        radarData = radarData
                    )
                }
            }
        }
    }
}

@Composable
fun PortAccessibilityCard(
    radarData: CellularRadarData,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.Black.copy(alpha = 0.35f))
            .border(1.dp, CyberCardBorder, RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header Row (Matches Screenshot 2: ISP PORT ACCESSIBILITY, TLS 1.3 READY)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ISP PORT ACCESSIBILITY",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = CyberCyan,
                    letterSpacing = 1.1.sp
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(CyberSurfaceLight)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "TLS 1.3 READY",
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberEmerald,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Ports List Rows (Matches Screenshot 2)
            radarData.portStatusList.forEach { port ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = port.name,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = CyberTextPrimary
                    )

                    Text(
                        text = port.statusText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (port.isOpen) CyberEmerald else Color.Red,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

@Composable
fun RadarMetricRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = CyberTextSecondary,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = label,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                color = CyberTextSecondary,
                letterSpacing = 0.5.sp
            )
        }

        Text(
            text = value,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            color = CyberTextPrimary,
            fontFamily = FontFamily.Monospace
        )
    }
}
