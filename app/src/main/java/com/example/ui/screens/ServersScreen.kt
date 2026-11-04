package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.model.ServerCategory
import com.example.model.ServerLocation
import com.example.model.VpnState
import com.example.ui.VpnUiState
import com.example.ui.VpnViewModel
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.ShieldCyan
import com.example.ui.theme.ShieldGreen
import com.example.ui.theme.ShieldNavyDark
import com.example.ui.theme.ShieldRed
import com.example.ui.theme.ShieldSurfaceDark
import com.example.ui.theme.ShieldSurfaceVariantDark
import com.example.ui.theme.ShieldYellow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ServersScreen(
    viewModel: VpnViewModel,
    uiState: VpnUiState,
    context: Context,
    vpnLauncher: ActivityResultLauncher<Intent>?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ShieldNavyDark)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Title Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "GLOBAL NODES",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp,
                        color = TextPrimary
                    )
                )
                Text(
                    text = "${uiState.filteredServers.size} secure servers available",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = ShieldSurfaceVariantDark
            ) {
                Text(
                    text = "10 Gbps MESH",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = ShieldCyan
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search bar
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("server_search_input"),
            placeholder = { Text("Search country, city, or IP...", color = TextMuted) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = TextMuted
                )
            },
            trailingIcon = {
                if (uiState.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = TextMuted
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = ShieldSurfaceDark,
                unfocusedContainerColor = ShieldSurfaceDark,
                focusedBorderColor = ShieldCyan,
                unfocusedBorderColor = BorderSubtle,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Category Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ServerCategory.values().forEach { category ->
                val isSelected = uiState.selectedCategory == category
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.selectCategory(category) },
                    label = {
                        Text(
                            text = category.label,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = ShieldSurfaceDark,
                        labelColor = TextSecondary,
                        selectedContainerColor = ShieldCyan,
                        selectedLabelColor = ShieldNavyDark
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = BorderSubtle,
                        selectedBorderColor = ShieldCyan
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Server List
        if (uiState.filteredServers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No servers found matching \"${uiState.searchQuery}\"",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(uiState.filteredServers, key = { it.id }) { server ->
                    ServerListItem(
                        server = server,
                        isSelected = uiState.selectedServer.id == server.id,
                        isConnected = uiState.selectedServer.id == server.id && uiState.vpnState == VpnState.CONNECTED,
                        onSelect = {
                            viewModel.selectServer(server, context, vpnLauncher)
                        },
                        onToggleFavorite = {
                            viewModel.toggleFavorite(server)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ServerListItem(
    server: ServerLocation,
    isSelected: Boolean,
    isConnected: Boolean,
    onSelect: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onSelect)
            .testTag("server_item_${server.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) ShieldSurfaceVariantDark else ShieldSurfaceDark
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isConnected) ShieldGreen else if (isSelected) ShieldCyan else BorderSubtle
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Flag and Details
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = server.flagEmoji, fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = server.country,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            if (server.doubleHopCity != null) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF312E81)
                                ) {
                                    Text(
                                        text = "MULTI-HOP",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFFA5B4FC)
                                        ),
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = server.city,
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "• IP: ${server.ipAddress}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }

                // Ping Badge, Favorite Star & Select Status
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Ping pill
                    val pingColor = when {
                        server.pingMs < 35 -> ShieldGreen
                        server.pingMs < 75 -> ShieldYellow
                        else -> ShieldRed
                    }
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ShieldNavyDark)
                            .padding(horizontal = 7.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(pingColor)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${server.pingMs}ms",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = pingColor
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Favorite Button
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (server.isFavorite) Icons.Default.Star else Icons.Outlined.StarOutline,
                            contentDescription = "Favorite",
                            tint = if (server.isFavorite) ShieldYellow else TextMuted
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Connect / Active state
                    if (isConnected) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(ShieldGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Active",
                                tint = ShieldNavyDark,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(ShieldCyan),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = ShieldNavyDark,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Server Load Bar & Tags
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Load ${server.loadPercentage}%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    LinearProgressIndicator(
                        progress = { server.loadPercentage / 100f },
                        modifier = Modifier
                            .width(80.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = if (server.loadPercentage < 50) ShieldGreen else ShieldYellow,
                        trackColor = ShieldNavyDark
                    )
                }

                if (server.streamingServices.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        server.streamingServices.take(3).forEach { service ->
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = ShieldNavyDark
                            ) {
                                Text(
                                    text = service,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextSecondary
                                    ),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                } else if (server.doubleHopCity != null) {
                    Text(
                        text = "Routes via ${server.doubleHopCity}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            color = Color(0xFFA5B4FC)
                        )
                    )
                }
            }
        }
    }
}
