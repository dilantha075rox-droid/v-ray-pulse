package com.example.ui

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import com.example.model.VlessConfig
import com.example.ui.theme.CyberBgDark
import com.example.ui.theme.CyberBgDeep
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberRed
import com.example.ui.theme.CyberSurfaceLight
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary
import com.example.ui.theme.CyberTextTertiary

@Composable
fun NodesScreen(
    uiState: MainUiState,
    onImportClick: () -> Unit,
    onSelectNode: (VlessConfig) -> Unit,
    onDeleteNode: (VlessConfig) -> Unit,
    onPingAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBgDark)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "VLESS NODES",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = CyberTextPrimary,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "${uiState.allNodes.size} / 10 NODES SAVED",
                    fontSize = 11.5.sp,
                    color = CyberTextSecondary
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                IconButton(
                    onClick = onPingAll,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CyberSurfaceLight)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Test all pings",
                        tint = CyberCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Button(
                    onClick = onImportClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add node", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (uiState.allNodes.size >= 10) "MAX 10" else "ADD NODE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        if (uiState.allNodes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Dns,
                        contentDescription = "No nodes",
                        tint = CyberTextTertiary,
                        modifier = Modifier.size(42.dp)
                    )
                    Text(text = "NO SAVED NODES", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = CyberTextSecondary)
                    Text(text = "Import up to 10 VLESS URLs to save and switch nodes.", fontSize = 11.5.sp, color = CyberTextTertiary)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedButton(onClick = onImportClick) {
                        Text(text = "IMPORT VLESS URL", color = CyberCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.allNodes, key = { it.rawUrl }) { node ->
                    val isActive = node.rawUrl == uiState.configuredServer?.rawUrl
                    val pingMs = uiState.nodePings["${node.server}:${node.port}"]

                    NodeCardItem(
                        node = node,
                        isActive = isActive,
                        pingMs = pingMs,
                        onSelect = { onSelectNode(node) },
                        onDelete = { onDeleteNode(node) }
                    )
                }
            }
        }
    }
}

@Composable
fun NodeCardItem(
    node: VlessConfig,
    isActive: Boolean,
    pingMs: Long?,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isActive) CyberSurfaceLight else CyberBgDeep)
            .border(
                width = 1.dp,
                color = if (isActive) CyberEmerald else CyberCardBorder,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onSelect)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (isActive) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Active node",
                            tint = CyberEmerald,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Text(
                        text = node.remarks.ifBlank { node.server },
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isActive) CyberEmerald else CyberTextPrimary,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = "${node.server}:${node.port} • VLESS ${node.displaySecurity}",
                    fontSize = 10.5.sp,
                    color = CyberTextSecondary,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (pingMs != null) {
                    val pingColor = when {
                        pingMs in 0..250 -> CyberEmerald
                        pingMs in 251..500 -> Color(0xFFFFB74D)
                        else -> CyberRed
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(pingColor.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (pingMs >= 0) "${pingMs}ms" else "TIMEOUT",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = pingColor,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete node",
                        tint = CyberTextTertiary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
