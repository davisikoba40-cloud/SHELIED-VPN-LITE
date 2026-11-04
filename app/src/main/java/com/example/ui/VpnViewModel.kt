package com.example.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.VpnService
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.VpnRepository
import com.example.model.DnsProvider
import com.example.model.SecurityAuditItem
import com.example.model.ServerCategory
import com.example.model.ServerLocation
import com.example.model.SpeedTestPhase
import com.example.model.SpeedTestState
import com.example.model.SplitApp
import com.example.model.VpnLogEntry
import com.example.model.VpnProtocol
import com.example.model.VpnSettings
import com.example.model.VpnState
import com.example.service.ShieldVpnService
import com.example.service.VpnStateManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class NavTab {
    SHIELD,
    SERVERS,
    SPEED_TEST,
    SECURITY,
    SETTINGS
}

data class VpnUiState(
    val currentTab: NavTab = NavTab.SHIELD,
    val vpnState: VpnState = VpnState.DISCONNECTED,
    val selectedServer: ServerLocation = VpnStateManager.defaultServer,
    val allServers: List<ServerLocation> = emptyList(),
    val filteredServers: List<ServerLocation> = emptyList(),
    val selectedCategory: ServerCategory = ServerCategory.ALL,
    val searchQuery: String = "",
    val settings: VpnSettings = VpnSettings(),
    val splitApps: List<SplitApp> = emptyList(),
    val threatsBlockedCount: Int = 1428,
    val speedTestState: SpeedTestState = SpeedTestState(),
    val securityAuditItems: List<SecurityAuditItem> = emptyList(),
    val logs: List<VpnLogEntry> = emptyList()
)

class VpnViewModel(
    private val repository: VpnRepository = VpnRepository()
) : ViewModel() {

    private val _currentTab = MutableStateFlow(NavTab.SHIELD)
    private val _selectedCategory = MutableStateFlow(ServerCategory.ALL)
    private val _searchQuery = MutableStateFlow("")
    private val _speedTestState = MutableStateFlow(SpeedTestState())
    private var speedTestJob: Job? = null

    val stats = VpnStateManager.stats
    val vpnState = VpnStateManager.vpnState
    val selectedServer = VpnStateManager.selectedServer
    val settings = VpnStateManager.settings
    val splitApps = VpnStateManager.splitApps
    val threatsBlocked = VpnStateManager.threatsBlocked
    val logs = VpnStateManager.logs

    val uiState: StateFlow<VpnUiState> = combine(
        _currentTab,
        vpnState,
        selectedServer,
        repository.servers,
        _selectedCategory,
        _searchQuery,
        settings,
        splitApps,
        threatsBlocked,
        _speedTestState,
        logs
    ) { params ->
        val currentTab = params[0] as NavTab
        val state = params[1] as VpnState
        val server = params[2] as ServerLocation
        @Suppress("UNCHECKED_CAST")
        val allServers = params[3] as List<ServerLocation>
        val category = params[4] as ServerCategory
        val query = params[5] as String
        val currentSettings = params[6] as VpnSettings
        @Suppress("UNCHECKED_CAST")
        val currentSplitApps = params[7] as List<SplitApp>
        val threats = params[8] as Int
        val speedTest = params[9] as SpeedTestState
        @Suppress("UNCHECKED_CAST")
        val logList = params[10] as List<VpnLogEntry>

        val filtered = allServers.filter { s ->
            val matchesCategory = when (category) {
                ServerCategory.ALL -> true
                ServerCategory.FASTEST -> s.categories.contains(ServerCategory.FASTEST)
                ServerCategory.STREAMING -> s.categories.contains(ServerCategory.STREAMING)
                ServerCategory.P2P -> s.categories.contains(ServerCategory.P2P)
                ServerCategory.DOUBLE_VPN -> s.categories.contains(ServerCategory.DOUBLE_VPN)
                ServerCategory.FAVORITES -> s.isFavorite
            }
            val matchesSearch = query.isBlank() ||
                    s.country.contains(query, ignoreCase = true) ||
                    s.city.contains(query, ignoreCase = true) ||
                    s.ipAddress.contains(query, ignoreCase = true)

            matchesCategory && matchesSearch
        }

        val auditItems = repository.getSecurityAuditItems(
            isConnected = state == VpnState.CONNECTED,
            virtualIp = server.ipAddress,
            cipher = when (currentSettings.protocol) {
                VpnProtocol.WIREGUARD -> "WireGuard (ChaCha20-Poly1305)"
                VpnProtocol.OPENVPN_UDP -> "OpenVPN UDP (AES-256-GCM)"
                VpnProtocol.OPENVPN_TCP -> "OpenVPN TCP (AES-256-CBC)"
                VpnProtocol.STEALTH -> "Stealth Shadowsocks (AEAD 256-bit)"
            }
        )

        VpnUiState(
            currentTab = currentTab,
            vpnState = state,
            selectedServer = server,
            allServers = allServers,
            filteredServers = filtered,
            selectedCategory = category,
            searchQuery = query,
            settings = currentSettings,
            splitApps = currentSplitApps,
            threatsBlockedCount = threats,
            speedTestState = speedTest,
            securityAuditItems = auditItems,
            logs = logList
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        VpnUiState()
    )

    fun selectTab(tab: NavTab) {
        _currentTab.value = tab
    }

    fun selectCategory(category: ServerCategory) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleFavorite(server: ServerLocation) {
        repository.toggleFavorite(server.id)
    }

    fun selectServer(server: ServerLocation, context: Context, vpnLauncher: ActivityResultLauncher<Intent>?) {
        VpnStateManager.selectServer(server)
        if (vpnState.value == VpnState.CONNECTED) {
            // Reconnect to new server node
            disconnectVpn(context)
            viewModelScope.launch {
                delay(600)
                connectVpn(context, vpnLauncher)
            }
        }
    }

    fun toggleQuickConnect(context: Context, vpnLauncher: ActivityResultLauncher<Intent>?) {
        when (vpnState.value) {
            VpnState.CONNECTED, VpnState.CONNECTING -> {
                disconnectVpn(context)
            }
            VpnState.DISCONNECTED, VpnState.DISCONNECTING -> {
                connectVpn(context, vpnLauncher)
            }
        }
    }

    fun connectVpn(context: Context, vpnLauncher: ActivityResultLauncher<Intent>?) {
        val prepareIntent = VpnService.prepare(context)
        if (prepareIntent != null) {
            // Permission needed from user
            vpnLauncher?.launch(prepareIntent)
        } else {
            // Already authorized, start service directly
            startVpnService(context)
        }
    }

    fun onVpnPermissionResult(isGranted: Boolean, context: Context) {
        if (isGranted) {
            startVpnService(context)
        } else {
            VpnStateManager.addLog("PERMISSION", "User declined VPN permission request.")
        }
    }

    private fun startVpnService(context: Context) {
        val intent = Intent(context, ShieldVpnService::class.java).apply {
            action = ShieldVpnService.ACTION_CONNECT
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun disconnectVpn(context: Context) {
        val intent = Intent(context, ShieldVpnService::class.java).apply {
            action = ShieldVpnService.ACTION_DISCONNECT
        }
        context.startService(intent)
    }

    fun setKillSwitch(enabled: Boolean) {
        VpnStateManager.updateSettings(settings.value.copy(killSwitchEnabled = enabled))
    }

    fun setThreatShield(enabled: Boolean) {
        VpnStateManager.updateSettings(settings.value.copy(threatShieldEnabled = enabled))
    }

    fun setAutoConnectWifi(enabled: Boolean) {
        VpnStateManager.updateSettings(settings.value.copy(autoConnectWifi = enabled))
    }

    fun setSplitTunneling(enabled: Boolean) {
        VpnStateManager.updateSettings(settings.value.copy(splitTunnelingEnabled = enabled))
    }

    fun setProtocol(protocol: VpnProtocol) {
        VpnStateManager.updateSettings(settings.value.copy(protocol = protocol))
    }

    fun setDnsProvider(provider: DnsProvider) {
        VpnStateManager.updateSettings(settings.value.copy(dnsProvider = provider))
    }

    fun setCustomDnsIp(ip: String) {
        VpnStateManager.updateSettings(settings.value.copy(customDnsIp = ip))
    }

    fun setMtu(mtu: Int) {
        VpnStateManager.updateSettings(settings.value.copy(mtuSize = mtu))
    }

    fun toggleSplitApp(packageName: String) {
        VpnStateManager.toggleSplitApp(packageName)
    }

    fun runSpeedTest() {
        if (_speedTestState.value.isRunning) return

        speedTestJob?.cancel()
        speedTestJob = viewModelScope.launch {
            val basePing = selectedServer.value.pingMs
            _speedTestState.value = SpeedTestState(
                phase = SpeedTestPhase.PING,
                isRunning = true,
                progress = 0.05f
            )

            // Step 1: Ping & Jitter
            delay(800)
            val finalPing = (basePing + Random.nextInt(-3, 6)).coerceAtLeast(8)
            val jitter = Random.nextInt(1, 4)
            _speedTestState.value = _speedTestState.value.copy(
                pingMs = finalPing,
                jitterMs = jitter,
                progress = 0.25f,
                phase = SpeedTestPhase.DOWNLOAD
            )

            // Step 2: Download test animation
            val targetDownload = (Random.nextFloat() * 45f + 65f) // 65 - 110 Mbps
            for (step in 1..25) {
                delay(80)
                val fraction = step / 25f
                val currentSpeed = (targetDownload * fraction * (0.85f + Random.nextFloat() * 0.3f)).coerceAtMost(targetDownload + 8f)
                _speedTestState.value = _speedTestState.value.copy(
                    currentSpeedMbps = currentSpeed,
                    downloadMbps = currentSpeed,
                    progress = 0.25f + (fraction * 0.4f)
                )
            }
            _speedTestState.value = _speedTestState.value.copy(
                downloadMbps = targetDownload,
                currentSpeedMbps = 0f,
                phase = SpeedTestPhase.UPLOAD,
                progress = 0.65f
            )

            // Step 3: Upload test animation
            val targetUpload = (Random.nextFloat() * 20f + 25f) // 25 - 45 Mbps
            for (step in 1..20) {
                delay(80)
                val fraction = step / 20f
                val currentSpeed = (targetUpload * fraction * (0.85f + Random.nextFloat() * 0.3f)).coerceAtMost(targetUpload + 4f)
                _speedTestState.value = _speedTestState.value.copy(
                    currentSpeedMbps = currentSpeed,
                    uploadMbps = currentSpeed,
                    progress = 0.65f + (fraction * 0.35f)
                )
            }

            _speedTestState.value = _speedTestState.value.copy(
                uploadMbps = targetUpload,
                currentSpeedMbps = 0f,
                progress = 1f,
                phase = SpeedTestPhase.COMPLETED,
                isRunning = false
            )
            VpnStateManager.addLog("SPEEDTEST", "Speed test finished: Ping ${finalPing}ms, Down ${String.format("%.1f", targetDownload)} Mbps, Up ${String.format("%.1f", targetUpload)} Mbps")
        }
    }
}
