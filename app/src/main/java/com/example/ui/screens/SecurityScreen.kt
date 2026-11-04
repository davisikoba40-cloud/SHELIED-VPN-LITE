package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AuditStatus
import com.example.model.SecurityAuditItem
import com.example.model.VpnState
import com.example.ui.VpnUiState
import com.example.ui.VpnViewModel
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.ShieldCyan
import com.example.ui.theme.ShieldGreen
import com.example.ui.theme.ShieldNavyDark
import com.example.ui.theme.ShieldRed
import com.example.ui.theme.ShieldSurfaceDark
import com.example.ui.theme.ShieldSurfaceElevated
import com.example.ui.theme.ShieldSurfaceVariantDark
import com.example.ui.theme.ShieldYellow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SecurityScreen(
    viewModel: VpnViewModel,
    uiState: VpnUiState,
    modifier: Modifier = Modifier
) {
    val isConnected = uiState.vpnState == VpnState.CONNECTED
    var isSplitTunnelingExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ShieldNavyDark)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SECURITY SUITE",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp,
                            color = TextPrimary
                        )
                    )
                    Text(
                        text = "Zero-leak encryption, kill switch & ad blockers",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isConnected) Color(0xFF065F46) else Color(0xFF991B1B)
                ) {
                    Text(
                        text = if (isConnected) "ARMED" else "DISARMED",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            color = if (isConnected) ShieldGreen else ShieldRed
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Kill Switch Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("kill_switch_card"),
                colors = CardDefaults.cardColors(containerColor = ShieldSurfaceDark),
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(ShieldSurfaceVariantDark),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = ShieldCyan,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Network Kill Switch",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                )
                                Text(
                                    text = if (uiState.settings.killSwitchEnabled) "Active • Zero-leak protection" else "Disabled • Click to enable",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (uiState.settings.killSwitchEnabled) ShieldGreen else TextMuted
                                    )
                                )
                            }
                        }

                        Switch(
                            checked = uiState.settings.killSwitchEnabled,
                            onCheckedChange = { viewModel.setKillSwitch(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ShieldNavyDark,
                                checkedTrackColor = ShieldCyan,
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = ShieldSurfaceVariantDark
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Automatically freezes all internet traffic if your VPN drops unexpectedly. Guarantees your true IP address and sensitive data are never transmitted over cleartext.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            lineHeight = 18.sp
                        )
                    )
                }
            }
        }

        // Threat Shield (AdBlock & Anti-Malware) Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("threat_shield_card"),
                colors = CardDefaults.cardColors(containerColor = ShieldSurfaceDark),
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(ShieldSurfaceVariantDark),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = ShieldGreen,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Threat Shield",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                )
                                Text(
                                    text = "${uiState.threatsBlockedCount} bad domains blocked",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = ShieldGreen,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        }

                        Switch(
                            checked = uiState.settings.threatShieldEnabled,
                            onCheckedChange = { viewModel.setThreatShield(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ShieldNavyDark,
                                checkedTrackColor = ShieldGreen,
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = ShieldSurfaceVariantDark
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Blocks invasive tracking scripts, scam pages, and phishing malware at the DNS layer before packets even reach your browser.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            lineHeight = 18.sp
                        )
                    )
                }
            }
        }

        // Split Tunneling Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ShieldSurfaceDark),
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(ShieldSurfaceVariantDark),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = Color(0xFFA78BFA),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Split Tunneling",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                )
                                Text(
                                    text = if (uiState.settings.splitTunnelingEnabled) "Enabled • ${uiState.splitApps.count { it.isBypassed }} apps bypassed" else "Disabled (All apps use VPN)",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                                )
                            }
                        }

                        Switch(
                            checked = uiState.settings.splitTunnelingEnabled,
                            onCheckedChange = { viewModel.setSplitTunneling(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ShieldNavyDark,
                                checkedTrackColor = Color(0xFFA78BFA),
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = ShieldSurfaceVariantDark
                            )
                        )
                    }

                    if (uiState.settings.splitTunnelingEnabled) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(ShieldSurfaceVariantDark)
                                .clickable { isSplitTunnelingExpanded = !isSplitTunnelingExpanded }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Configure App Bypass List",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ShieldCyan
                                )
                            )
                            Icon(
                                imageVector = if (isSplitTunnelingExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = ShieldCyan
                            )
                        }

                        AnimatedVisibility(visible = isSplitTunnelingExpanded) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                uiState.splitApps.forEach { app ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(ShieldNavyDark)
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = app.appName,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = TextPrimary
                                                )
                                            )
                                            Text(
                                                text = app.category,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = TextMuted,
                                                    fontSize = 10.sp
                                                )
                                            )
                                        }

                                        Surface(
                                            modifier = Modifier.clickable {
                                                viewModel.toggleSplitApp(app.packageName)
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (app.isBypassed) Color(0xFF3730A3) else ShieldSurfaceVariantDark
                                        ) {
                                            Text(
                                                text = if (app.isBypassed) "BYPASS VPN" else "TUNNEL VPN",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (app.isBypassed) Color(0xFFC7D2FE) else TextMuted
                                                ),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Live Security Audit Checklist
        item {
            Text(
                text = "LIVE SECURITY AUDIT CHECKLIST",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    color = TextMuted
                )
            )
        }

        items(uiState.securityAuditItems, key = { it.id }) { item ->
            SecurityCheckCard(item = item)
        }
    }
}

@Composable
fun SecurityCheckCard(item: SecurityAuditItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ShieldSurfaceDark),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = when (item.status) {
                    AuditStatus.SECURE -> Icons.Default.CheckCircle
                    AuditStatus.WARNING -> Icons.Default.Warning
                    AuditStatus.INFO -> Icons.Default.Info
                },
                contentDescription = null,
                tint = when (item.status) {
                    AuditStatus.SECURE -> ShieldGreen
                    AuditStatus.WARNING -> ShieldRed
                    AuditStatus.INFO -> Color(0xFF60A5FA)
                },
                modifier = Modifier
                    .size(20.dp)
                    .padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium,
                        color = when (item.status) {
                            AuditStatus.SECURE -> ShieldGreen
                            AuditStatus.WARNING -> ShieldRed
                            AuditStatus.INFO -> Color(0xFF60A5FA)
                        }
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.detail,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextSecondary,
                        lineHeight = 15.sp
                    )
                )
            }
        }
    }
}
