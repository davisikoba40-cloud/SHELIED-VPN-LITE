package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SpeedTestPhase
import com.example.ui.VpnUiState
import com.example.ui.VpnViewModel
import com.example.ui.components.SpeedometerGauge
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.ShieldCyan
import com.example.ui.theme.ShieldGreen
import com.example.ui.theme.ShieldNavyDark
import com.example.ui.theme.ShieldSurfaceDark
import com.example.ui.theme.ShieldSurfaceElevated
import com.example.ui.theme.ShieldSurfaceVariantDark
import com.example.ui.theme.ShieldYellow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SpeedTestScreen(
    viewModel: VpnViewModel,
    uiState: VpnUiState,
    modifier: Modifier = Modifier
) {
    val speedState = uiState.speedTestState

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ShieldNavyDark)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
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
                        text = "REAL SPEED TEST",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp,
                            color = TextPrimary
                        )
                    )
                    Text(
                        text = "Live CDN throughput & HTTP latency benchmark",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ShieldSurfaceVariantDark
                ) {
                    Text(
                        text = "LIVE TEST",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = ShieldGreen
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }

        // Speedometer Gauge
        item {
            val gaugeLabel = when (speedState.phase) {
                SpeedTestPhase.IDLE -> "READY"
                SpeedTestPhase.PING -> "MEASURING HTTP PING..."
                SpeedTestPhase.DOWNLOAD -> "STREAMING CDN CHUNKS"
                SpeedTestPhase.UPLOAD -> "UPLOADING DATASTREAM"
                SpeedTestPhase.COMPLETED -> "REAL BENCHMARK FINISHED"
            }

            SpeedometerGauge(
                currentSpeedMbps = when (speedState.phase) {
                    SpeedTestPhase.DOWNLOAD -> speedState.currentSpeedMbps
                    SpeedTestPhase.UPLOAD -> speedState.currentSpeedMbps
                    SpeedTestPhase.COMPLETED -> speedState.downloadMbps
                    else -> 0f
                },
                maxSpeedMbps = 150f,
                label = gaugeLabel
            )
        }

        // Status text / live updates
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = speedState.statusMessage,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = if (speedState.phase == SpeedTestPhase.COMPLETED) ShieldGreen else ShieldCyan,
                    fontWeight = FontWeight.Medium
                )
            )
        }

        // Progress bar during test
        item {
            if (speedState.isRunning) {
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { speedState.progress },
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = ShieldCyan,
                    trackColor = ShieldSurfaceVariantDark
                )
            }
        }

        item { Spacer(modifier = Modifier.height(22.dp)) }

        // Metrics 4-Box Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SpeedMetricCard(
                    title = "PING (LATENCY)",
                    value = if (speedState.pingMs > 0) "${speedState.pingMs} ms" else "--",
                    icon = Icons.Default.Timer,
                    iconColor = ShieldGreen,
                    modifier = Modifier.weight(1f)
                )

                SpeedMetricCard(
                    title = "JITTER",
                    value = if (speedState.jitterMs > 0) "${speedState.jitterMs} ms" else "--",
                    icon = Icons.Default.NetworkCheck,
                    iconColor = Color(0xFFA78BFA),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item { Spacer(modifier = Modifier.height(10.dp)) }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SpeedMetricCard(
                    title = "DOWNLOAD (CDN)",
                    value = if (speedState.downloadMbps > 0) String.format("%.1f Mbps", speedState.downloadMbps) else "--",
                    icon = Icons.Default.ArrowDownward,
                    iconColor = ShieldCyan,
                    modifier = Modifier.weight(1f)
                )

                SpeedMetricCard(
                    title = "UPLOAD (PEAK)",
                    value = if (speedState.uploadMbps > 0) String.format("%.1f Mbps", speedState.uploadMbps) else "--",
                    icon = Icons.Default.ArrowUpward,
                    iconColor = Color(0xFF60A5FA),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item { Spacer(modifier = Modifier.height(18.dp)) }

        // Current Testing Node Details Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ShieldSurfaceDark),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "NETWORK CARRIER & TEST NODE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            color = TextMuted
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = uiState.selectedServer.flagEmoji, fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "${uiState.selectedServer.country} (${uiState.selectedServer.city})",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                )
                                Text(
                                    text = "Carrier: ${uiState.realIpInfo.isp} • IP: ${uiState.realIpInfo.ip}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ShieldSurfaceVariantDark
                        ) {
                            Text(
                                text = "REAL CDN",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ShieldCyan
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        // Action Button
        item {
            Button(
                onClick = { viewModel.runSpeedTest() },
                enabled = !speedState.isRunning,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("speed_test_action_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ShieldCyan,
                    contentColor = ShieldNavyDark,
                    disabledContainerColor = ShieldSurfaceVariantDark,
                    disabledContentColor = TextMuted
                )
            ) {
                if (speedState.isRunning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = ShieldCyan,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "TESTING REAL NETWORK...",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    )
                } else {
                    Icon(
                        imageVector = if (speedState.phase == SpeedTestPhase.COMPLETED) Icons.Default.Refresh else Icons.Default.PlayArrow,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (speedState.phase == SpeedTestPhase.COMPLETED) "TEST REAL SPEED AGAIN" else "START REAL SPEED TEST",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun SpeedMetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = ShieldSurfaceDark),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = TextPrimary
                )
            )
        }
    }
}
