package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberBgDeep
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberTextSecondary
import com.example.ui.theme.CyberTextTertiary

@Composable
fun CyberBottomNavBar(
    activeTab: DashboardTab,
    onTabSelected: (DashboardTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(CyberBgDeep)
            .border(
                width = 1.dp,
                color = CyberCardBorder,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            )
            .padding(vertical = 10.dp, horizontal = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem(
                label = "Pulse",
                icon = Icons.Default.Speed,
                isSelected = activeTab == DashboardTab.PULSE,
                onClick = { onTabSelected(DashboardTab.PULSE) }
            )

            NavItem(
                label = "Nodes",
                icon = Icons.Default.Dns,
                isSelected = activeTab == DashboardTab.NODES,
                onClick = { onTabSelected(DashboardTab.NODES) }
            )

            NavItem(
                label = "Routing",
                icon = Icons.Default.AltRoute,
                isSelected = activeTab == DashboardTab.ROUTING,
                onClick = { onTabSelected(DashboardTab.ROUTING) }
            )

            NavItem(
                label = "Custom Rig",
                icon = Icons.Default.Tune,
                isSelected = activeTab == DashboardTab.CUSTOM_RIG,
                onClick = { onTabSelected(DashboardTab.CUSTOM_RIG) }
            )
        }
    }
}

@Composable
fun NavItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val contentColor = if (isSelected) CyberCyan else CyberTextTertiary

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(width = 32.dp, height = 3.dp)
                    .clip(CircleShape)
                    .background(CyberCyan)
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) CyberCyan else CyberTextSecondary
        )
    }
}
