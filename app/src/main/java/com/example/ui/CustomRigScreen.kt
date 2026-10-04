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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

@Composable
fun CustomRigScreen(
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
                text = "CUSTOM RIG TUNING",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CyberTextPrimary,
                letterSpacing = 1.5.sp
            )
            Text(
                text = "Dilanthar's local inbound ports & core engine tuning",
                fontSize = 12.sp,
                color = CyberTextSecondary
            )
        }

        // Local Inbound Ports Panel
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

        // Toggle Switches
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
