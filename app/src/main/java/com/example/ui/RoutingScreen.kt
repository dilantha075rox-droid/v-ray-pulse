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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberBgDark
import com.example.ui.theme.CyberBgDeep
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberSurfaceLight
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary
import com.example.ui.theme.CyberTextTertiary

@Composable
fun RoutingScreen(
    modifier: Modifier = Modifier
) {
    var selectedMode by remember { mutableStateOf("GLOBAL") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBgDark)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = "ROUTING RULES",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CyberTextPrimary,
                letterSpacing = 1.5.sp
            )
            Text(
                text = "Configure traffic proxy rules for Xray core",
                fontSize = 12.sp,
                color = CyberTextSecondary
            )
        }

        RoutingOptionCard(
            title = "GLOBAL PROXY",
            subtitle = "Route all network traffic through VLESS proxy",
            icon = Icons.Default.Public,
            isSelected = selectedMode == "GLOBAL",
            onSelect = { selectedMode = "GLOBAL" }
        )

        RoutingOptionCard(
            title = "BYPASS LOCAL / LAN",
            subtitle = "Bypass private IP ranges and local network devices",
            icon = Icons.Default.AltRoute,
            isSelected = selectedMode == "BYPASS_LAN",
            onSelect = { selectedMode = "BYPASS_LAN" }
        )

        RoutingOptionCard(
            title = "BLOCK ADS & MALWARE",
            subtitle = "Use GeoSite adblock rules on outbound proxy tunnel",
            icon = Icons.Default.Block,
            isSelected = selectedMode == "ADBLOCK",
            onSelect = { selectedMode = "ADBLOCK" }
        )
    }
}

@Composable
fun RoutingOptionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) CyberSurfaceLight else CyberBgDeep)
            .border(
                width = 1.dp,
                color = if (isSelected) CyberCyan else CyberCardBorder,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (isSelected) CyberCyan else CyberTextTertiary,
                    modifier = Modifier.size(24.dp)
                )

                Column {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) CyberCyan else CyberTextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = CyberTextSecondary
                    )
                }
            }

            RadioButton(
                selected = isSelected,
                onClick = onSelect,
                colors = RadioButtonDefaults.colors(selectedColor = CyberCyan)
            )
        }
    }
}
