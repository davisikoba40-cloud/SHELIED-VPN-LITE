package com.example

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.NavTab
import com.example.ui.VpnViewModel
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SecurityScreen
import com.example.ui.screens.ServersScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SpeedTestScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ShieldCyan
import com.example.ui.theme.ShieldNavyDark
import com.example.ui.theme.ShieldSurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                ShieldVpnApp()
            }
        }
    }
}

@Composable
fun ShieldVpnApp(
    viewModel: VpnViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // Launcher for Android VpnService permission request
    val vpnPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val isGranted = result.resultCode == Activity.RESULT_OK
        viewModel.onVpnPermissionResult(isGranted, context)
    }

    Crossfade(
        targetState = uiState.showSplash,
        label = "SplashTransition"
    ) { isSplash ->
        if (isSplash) {
            SplashScreen(
                onSplashFinished = { viewModel.dismissSplash() }
            )
        } else {
            // Hardware Back Button returns to Home Shield Tab
            BackHandler(enabled = uiState.currentTab != NavTab.SHIELD) {
                viewModel.selectTab(NavTab.SHIELD)
            }

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = ShieldNavyDark,
                bottomBar = {
                    NavigationBar(
                        modifier = Modifier
                            .windowInsetsPadding(WindowInsets.navigationBars)
                            .testTag("main_bottom_nav"),
                        containerColor = ShieldSurfaceDark,
                        tonalElevation = 8.dp
                    ) {
                        // Tab 1: Shield
                        NavigationBarItem(
                            selected = uiState.currentTab == NavTab.SHIELD,
                            onClick = { viewModel.selectTab(NavTab.SHIELD) },
                            icon = {
                                Icon(
                                    imageVector = if (uiState.currentTab == NavTab.SHIELD) Icons.Default.Shield else Icons.Outlined.Shield,
                                    contentDescription = "Shield"
                                )
                            },
                            label = { Text("Shield", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = ShieldNavyDark,
                                indicatorColor = ShieldCyan,
                                selectedTextColor = ShieldCyan,
                                unselectedIconColor = TextMuted,
                                unselectedTextColor = TextMuted
                            ),
                            modifier = Modifier.testTag("nav_tab_shield")
                        )

                        // Tab 2: Servers
                        NavigationBarItem(
                            selected = uiState.currentTab == NavTab.SERVERS,
                            onClick = { viewModel.selectTab(NavTab.SERVERS) },
                            icon = {
                                Icon(
                                    imageVector = if (uiState.currentTab == NavTab.SERVERS) Icons.Default.Public else Icons.Outlined.Public,
                                    contentDescription = "Servers"
                                )
                            },
                            label = { Text("Servers", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = ShieldNavyDark,
                                indicatorColor = ShieldCyan,
                                selectedTextColor = ShieldCyan,
                                unselectedIconColor = TextMuted,
                                unselectedTextColor = TextMuted
                            ),
                            modifier = Modifier.testTag("nav_tab_servers")
                        )

                        // Tab 3: Speed Test
                        NavigationBarItem(
                            selected = uiState.currentTab == NavTab.SPEED_TEST,
                            onClick = { viewModel.selectTab(NavTab.SPEED_TEST) },
                            icon = {
                                Icon(
                                    imageVector = if (uiState.currentTab == NavTab.SPEED_TEST) Icons.Default.Speed else Icons.Outlined.Speed,
                                    contentDescription = "Speed Test"
                                )
                            },
                            label = { Text("Speed", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = ShieldNavyDark,
                                indicatorColor = ShieldCyan,
                                selectedTextColor = ShieldCyan,
                                unselectedIconColor = TextMuted,
                                unselectedTextColor = TextMuted
                            ),
                            modifier = Modifier.testTag("nav_tab_speed")
                        )

                        // Tab 4: Security
                        NavigationBarItem(
                            selected = uiState.currentTab == NavTab.SECURITY,
                            onClick = { viewModel.selectTab(NavTab.SECURITY) },
                            icon = {
                                Icon(
                                    imageVector = if (uiState.currentTab == NavTab.SECURITY) Icons.Default.Security else Icons.Outlined.Security,
                                    contentDescription = "Security"
                                )
                            },
                            label = { Text("Security", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = ShieldNavyDark,
                                indicatorColor = ShieldCyan,
                                selectedTextColor = ShieldCyan,
                                unselectedIconColor = TextMuted,
                                unselectedTextColor = TextMuted
                            ),
                            modifier = Modifier.testTag("nav_tab_security")
                        )

                        // Tab 5: Settings
                        NavigationBarItem(
                            selected = uiState.currentTab == NavTab.SETTINGS,
                            onClick = { viewModel.selectTab(NavTab.SETTINGS) },
                            icon = {
                                Icon(
                                    imageVector = if (uiState.currentTab == NavTab.SETTINGS) Icons.Default.Tune else Icons.Outlined.Tune,
                                    contentDescription = "Settings"
                                )
                            },
                            label = { Text("Settings", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = ShieldNavyDark,
                                indicatorColor = ShieldCyan,
                                selectedTextColor = ShieldCyan,
                                unselectedIconColor = TextMuted,
                                unselectedTextColor = TextMuted
                            ),
                            modifier = Modifier.testTag("nav_tab_settings")
                        )
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (uiState.currentTab) {
                        NavTab.SHIELD -> HomeScreen(
                            viewModel = viewModel,
                            uiState = uiState,
                            context = context,
                            vpnLauncher = vpnPermissionLauncher,
                            onNavigateTab = { tab -> viewModel.selectTab(tab) }
                        )
                        NavTab.SERVERS -> ServersScreen(
                            viewModel = viewModel,
                            uiState = uiState,
                            context = context,
                            vpnLauncher = vpnPermissionLauncher
                        )
                        NavTab.SPEED_TEST -> SpeedTestScreen(
                            viewModel = viewModel,
                            uiState = uiState
                        )
                        NavTab.SECURITY -> SecurityScreen(
                            viewModel = viewModel,
                            uiState = uiState
                        )
                        NavTab.SETTINGS -> SettingsScreen(
                            viewModel = viewModel,
                            uiState = uiState
                        )
                    }
                }
            }
        }
    }
}
