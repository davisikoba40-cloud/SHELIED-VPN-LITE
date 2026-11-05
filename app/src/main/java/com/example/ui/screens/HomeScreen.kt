package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.VpnState
import com.example.ui.NavTab
import com.example.ui.VpnUiState
import com.example.ui.VpnViewModel
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.ShieldCyan
import com.example.ui.theme.ShieldCyanGlow
import com.example.ui.theme.ShieldGreen
import com.example.ui.theme.ShieldGreenGlow
import com.example.ui.theme.ShieldNavyDark
import com.example.ui.theme.ShieldRed
import com.example.ui.theme.ShieldSurfaceDark
import com.example.ui.theme.ShieldSurfaceElevated
import com.example.ui.theme.ShieldSurfaceVariantDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun HomeScreen(
    viewModel: VpnViewModel,
    uiState: VpnUiState,
    context: Context,
    vpnLauncher: ActivityResultLauncher<Intent>?,
    onNavigateTab: (NavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val stats by viewModel.stats.collectAsState()

    val isConnected = uiState.vpnState == VpnState.CONNECTED
    val isConnecting = uiState.vpnState == VpnState.CONNECTING

    // Connect Button Color Animation
    val ringColor by animateColorAsState(
        targetValue = when (uiState.vpnState) {
            VpnState.CONNECTED -> ShieldGreen
            VpnState.CONNECTING -> ShieldCyan
            VpnState.DISCONNECTING -> ShieldRed
            VpnState.DISCONNECTED -> ShieldCyan
        },
        animationSpec = tween(durationMillis = 400),
        label = "RingColor"
    )

    // Pulse animation when connected or connecting
    val infiniteTransition = rememberInfiniteTransition(label = "PulseTransition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isConnected || isConnecting) 1.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ShieldNavyDark)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Header Brand
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Brush.linearGradient(listOf(ShieldCyan, Color(0xFF0284C7)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Shield VPN Logo",
                            tint = ShieldNavyDark,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "SHIELD VPN",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp,
                                color = TextPrimary
                            )
                        )
                        Text(
                            text = if (isConnected) "ACTIVE TUNNEL • ZERO LEAKS" else "POWERED BY MOBILES",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isConnected) ShieldGreen else ShieldCyan
                            )
                        )
                    }
                }

                // Security pill badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isConnected) ShieldGreenGlow else ShieldSurfaceVariantDark,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isConnected) ShieldGreen.copy(alpha = 0.5f) else BorderSubtle
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isConnected) ShieldGreen else ShieldRed)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isConnected) "SECURED" else "UNPROTECTED",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                color = if (isConnected) ShieldGreen else ShieldRed
                            )
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        // Central Power Connect Button
        item {
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .testTag("vpn_power_button"),
                contentAlignment = Alignment.Center
            ) {
                // Background Outer Pulse Ring
                if (isConnected || isConnecting) {
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(if (isConnected) ShieldGreenGlow else ShieldCyanGlow)
                    )
                }

                // Outer decorative border ring
                Box(
                    modifier = Modifier
                        .size(185.dp)
                        .clip(CircleShape)
                        .background(ShieldSurfaceDark)
                        .border(
                            width = 3.dp,
                            brush = Brush.sweepGradient(
                                listOf(
                                    ringColor,
                                    ringColor.copy(alpha = 0.2f),
                                    ringColor
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Inner Interactive Action Button
                    Box(
                        modifier = Modifier
                            .size(145.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        if (isConnected) Color(0xFF065F46) else Color(0xFF1E293B),
                                        ShieldNavyDark
                                    )
                                )
                            )
                            .clickable {
                                viewModel.toggleQuickConnect(context, vpnLauncher)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PowerSettingsNew,
                                contentDescription = "Connect or Disconnect VPN",
                                tint = ringColor,
                                modifier = Modifier.size(50.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = when (uiState.vpnState) {
                                    VpnState.CONNECTED -> "CONNECTED"
                                    VpnState.CONNECTING -> "CONNECTING"
                                    VpnState.DISCONNECTING -> "STOPPING"
                                    VpnState.DISCONNECTED -> "CONNECT"
                                },
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.5.sp,
                                    color = TextPrimary
                                )
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(14.dp)) }

        // Status Subtitle
        item {
            Text(
                text = when (uiState.vpnState) {
                    VpnState.CONNECTED -> "Encrypted tunnel active via ${uiState.selectedServer.country} • Full Internet Online"
                    VpnState.CONNECTING -> "Establishing secure 256-bit handshake..."
                    VpnState.DISCONNECTING -> "Restoring normal carrier connection..."
                    VpnState.DISCONNECTED -> "Physical IP ${uiState.realIpInfo.ip} visible to websites • Click to protect"
                },
                style = MaterialTheme.typography.bodySmall.copy(
                    color = if (isConnected) ShieldGreen else TextSecondary,
                    fontWeight = FontWeight.Medium
                )
            )
        }

        item { Spacer(modifier = Modifier.height(18.dp)) }

        // Live Internet Verification Banner & Button (Fixes "does not work like other VPNs, not demo")
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ShieldSurfaceDark),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NetworkCheck,
                            contentDescription = null,
                            tint = if (isConnected) ShieldGreen else ShieldCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Tunnel Connectivity Status",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            Text(
                                text = if (isConnected) "Internet active • DNS resolved via ${uiState.settings.dnsProvider.title}"
                                else "Direct connection • Tap test to verify live reachability",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isConnected) ShieldGreen else TextMuted
                                )
                            )
                        }
                    }

                    Button(
                        onClick = { viewModel.verifyLiveConnection() },
                        enabled = !uiState.isVerifyingConnection,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ShieldSurfaceVariantDark,
                            contentColor = ShieldCyan
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        if (uiState.isVerifyingConnection) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                color = ShieldCyan,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "TEST NET",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        }

        // Verification feedback message
        item {
            AnimatedVisibility(visible = uiState.connectionVerificationMessage != null) {
                uiState.connectionVerificationMessage?.let { msg ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF064E3B)
                    ) {
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF6EE7B7),
                                fontWeight = FontWeight.SemiBold
                            ),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(14.dp)) }

        // Selected Server Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { onNavigateTab(NavTab.SERVERS) }
                    .testTag("server_selector_card"),
                colors = CardDefaults.cardColors(containerColor = ShieldSurfaceDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = uiState.selectedServer.flagEmoji,
                            fontSize = 32.sp
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = uiState.selectedServer.country,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = uiState.selectedServer.city,
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(ShieldGreen)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${uiState.selectedServer.pingMs} ms",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = ShieldGreen,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = ShieldSurfaceVariantDark
                        ) {
                            Text(
                                text = "CHANGE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ShieldCyan
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Select Server",
                            tint = TextMuted
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        // Live Real-Time Network Telemetry Bar
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ShieldSurfaceDark),
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LIVE NETWORK THROUGHPUT",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp,
                                color = TextMuted
                            )
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (isConnected) ShieldGreen else TextMuted,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = uiState.settings.protocol.displayName,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isConnected) ShieldGreen else TextMuted
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Download Speed
                        MetricItem(
                            icon = Icons.Default.ArrowDownward,
                            iconColor = ShieldCyan,
                            title = "Download",
                            value = if (isConnected) formatSpeed(stats.downloadSpeedKbps) else "0.0 KB/s"
                        )

                        // Upload Speed
                        MetricItem(
                            icon = Icons.Default.ArrowUpward,
                            iconColor = Color(0xFF60A5FA),
                            title = "Upload",
                            value = if (isConnected) formatSpeed(stats.uploadSpeedKbps) else "0.0 KB/s"
                        )

                        // Session Timer
                        MetricItem(
                            icon = Icons.Default.Timer,
                            iconColor = Color(0xFFFBBF24),
                            title = "Duration",
                            value = formatDuration(stats.durationSeconds)
                        )

                        // Total Data
                        MetricItem(
                            icon = Icons.Default.Public,
                            iconColor = ShieldGreen,
                            title = "Data Used",
                            value = String.format("%.1f MB", stats.totalDownloadedMb + stats.totalUploadedMb)
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        // Real IP & Network Details Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ShieldSurfaceElevated),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isConnected) "VIRTUAL CLOAKED ENDPOINT" else "REAL PHYSICAL NETWORK IP",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = TextMuted
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isConnected) uiState.selectedServer.ipAddress else uiState.realIpInfo.ip,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isConnected) ShieldCyan else ShieldRed
                            )
                        )
                        Text(
                            text = if (isConnected) "${uiState.selectedServer.city} • 256-Bit Encrypted"
                            else "${uiState.realIpInfo.city}, ${uiState.realIpInfo.country} • ${uiState.realIpInfo.isp}",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.refreshRealIp() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Real IP",
                            tint = ShieldCyan
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        // Quick Security Status Chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SecurityChip(
                    title = "Kill Switch",
                    isActive = uiState.settings.killSwitchEnabled,
                    onClick = { viewModel.setKillSwitch(!uiState.settings.killSwitchEnabled) },
                    modifier = Modifier.weight(1f)
                )

                SecurityChip(
                    title = "Threat Shield",
                    isActive = uiState.settings.threatShieldEnabled,
                    badgeText = "${uiState.threatsBlockedCount} blocked",
                    onClick = { viewModel.setThreatShield(!uiState.settings.threatShieldEnabled) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }

        // Powered by Mobiles Footer Badge
        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = ShieldSurfaceDark,
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PhoneAndroid,
                        contentDescription = null,
                        tint = ShieldCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "POWERED BY MOBILES",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            color = TextSecondary
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun MetricItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    title: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextMuted,
                    fontSize = 10.sp
                )
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        )
    }
}

@Composable
fun SecurityChip(
    title: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badgeText: String? = null
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = if (isActive) ShieldSurfaceVariantDark else ShieldSurfaceDark,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isActive) ShieldCyan.copy(alpha = 0.4f) else BorderSubtle
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isActive) TextPrimary else TextMuted
                    )
                )
                if (badgeText != null) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            color = if (isActive) ShieldCyan else TextMuted
                        )
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (isActive) ShieldCyan else TextMuted)
            )
        }
    }
}

private fun formatSpeed(kbps: Float): String {
    return if (kbps >= 1000f) {
        String.format("%.1f MB/s", kbps / 8000f)
    } else {
        String.format("%.0f KB/s", kbps)
    }
}

private fun formatDuration(seconds: Long): String {
    val hrs = seconds / 3600
    val mins = (seconds % 3600) / 60
    val secs = seconds % 60
    return if (hrs > 0) {
        String.format("%02d:%02d:%02d", hrs, mins, secs)
    } else {
        String.format("%02d:%02d", mins, secs)
    }
}
