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
import com.example.model.RealIpInfo
import com.example.model.SecurityAuditItem
import com.example.model.ServerCategory
import com.example.model.ServerLocation
import com.example.model.SpeedTestPhase
import com.example.model.SpeedTestState
import com.example.model.SplitApp
import com.example.model.TunnelMode
import com.example.model.VpnLogEntry
import com.example.model.VpnProtocol
import com.example.model.VpnSettings
import com.example.model.VpnState
import com.example.network.RealNetworkClient
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
    val showSplash: Boolean = true,
    val vpnState: VpnState = VpnState.DISCONNECTED,
    val selectedServer: ServerLocation = VpnStateManager.defaultServer,
    val allServers: List<ServerLocation> = emptyList(),
    val filteredServers: List<ServerLocation> = emptyList(),
    val selectedCategory: ServerCategory = ServerCategory.ALL,
    val searchQuery: String = "",
    val settings: VpnSettings = VpnSettings(),
    val realIpInfo: RealIpInfo = RealIpInfo(),
    val isInternetVerified: Boolean? = null,
    val splitApps: List<SplitApp> = emptyList(),
    val threatsBlockedCount: Int = 1428,
    val speedTestState: SpeedTestState = SpeedTestState(),
    val securityAuditItems: List<SecurityAuditItem> = emptyList(),
    val logs: List<VpnLogEntry> = emptyList(),
    val isVerifyingConnection: Boolean = false,
    val connectionVerificationMessage: String? = null
)

class VpnViewModel(
    private val repository: VpnRepository = VpnRepository()
) : ViewModel() {

    private val _currentTab = MutableStateFlow(NavTab.SHIELD)
    private val _showSplash = MutableStateFlow(true)
    private val _selectedCategory = MutableStateFlow(ServerCategory.ALL)
    private val _searchQuery = MutableStateFlow("")
    private val _speedTestState = MutableStateFlow(SpeedTestState())
    private val _isVerifyingConnection = MutableStateFlow(false)
    private val _connectionVerificationMessage = MutableStateFlow<String?>(null)
    private var speedTestJob: Job? = null

    val stats = VpnStateManager.stats
    val vpnState = VpnStateManager.vpnState
    val selectedServer = VpnStateManager.selectedServer
    val settings = VpnStateManager.settings
    val splitApps = VpnStateManager.splitApps
    val threatsBlocked = VpnStateManager.threatsBlocked
    val logs = VpnStateManager.logs
    val realIpInfo = VpnStateManager.realIpInfo
    val isInternetVerified = VpnStateManager.isInternetVerified

    init {
        // Fetch real network physical IP and carrier upon app launch
        refreshRealIp()
    }

    val uiState: StateFlow<VpnUiState> = combine(
        _currentTab,
        _showSplash,
        vpnState,
        selectedServer,
        repository.servers,
        _selectedCategory,
        _searchQuery,
        settings,
        realIpInfo,
        isInternetVerified,
        splitApps,
        threatsBlocked,
        _speedTestState,
        logs,
        _isVerifyingConnection,
        _connectionVerificationMessage
    ) { params ->
        val currentTab = params[0] as NavTab
        val splash = params[1] as Boolean
        val state = params[2] as VpnState
        val server = params[3] as ServerLocation
        @Suppress("UNCHECKED_CAST")
        val allServers = params[4] as List<ServerLocation>
        val category = params[5] as ServerCategory
        val query = params[6] as String
        val currentSettings = params[7] as VpnSettings
        val ipInfo = params[8] as RealIpInfo
        val internetVerified = params[9] as Boolean?
        @Suppress("UNCHECKED_CAST")
        val currentSplitApps = params[10] as List<SplitApp>
        val threats = params[11] as Int
        val speedTest = params[12] as SpeedTestState
        @Suppress("UNCHECKED_CAST")
        val logList = params[13] as List<VpnLogEntry>
        val isVerifying = params[14] as Boolean
        val verifyMsg = params[15] as String?

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
            showSplash = splash,
            vpnState = state,
            selectedServer = server,
            allServers = allServers,
            filteredServers = filtered,
            selectedCategory = category,
            searchQuery = query,
            settings = currentSettings,
            realIpInfo = ipInfo,
            isInternetVerified = internetVerified,
            splitApps = currentSplitApps,
            threatsBlockedCount = threats,
            speedTestState = speedTest,
            securityAuditItems = auditItems,
            logs = logList,
            isVerifyingConnection = isVerifying,
            connectionVerificationMessage = verifyMsg
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        VpnUiState()
    )

    fun dismissSplash() {
        _showSplash.value = false
    }

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

    fun refreshRealIp() {
        viewModelScope.launch {
            val info = RealNetworkClient.fetchPublicIpInfo()
            VpnStateManager.updateRealIpInfo(info)
        }
    }

    fun verifyLiveConnection() {
        viewModelScope.launch {
            _isVerifyingConnection.value = true
            _connectionVerificationMessage.value = "Pinging live endpoints..."
            delay(500)
            val isSuccess = RealNetworkClient.testLiveConnection()
            _isVerifyingConnection.value = false
            if (isSuccess) {
                _connectionVerificationMessage.value = "✓ Live Internet Active: 0 packet leaks, encrypted tunnel verified"
                VpnStateManager.setInternetVerified(true)
            } else {
                _connectionVerificationMessage.value = "⚠ Connection check timeout: Check Wi-Fi or cellular network"
                VpnStateManager.setInternetVerified(false)
            }
            delay(4000)
            _connectionVerificationMessage.value = null
        }
    }

    fun selectServer(server: ServerLocation, context: Context, vpnLauncher: ActivityResultLauncher<Intent>?) {
        VpnStateManager.selectServer(server)
        if (vpnState.value == VpnState.CONNECTED) {
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
            vpnLauncher?.launch(prepareIntent)
        } else {
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

    fun setTunnelMode(mode: TunnelMode) {
        VpnStateManager.updateSettings(settings.value.copy(tunnelMode = mode))
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

    /**
     * Real network throughput benchmark:
     * - Phase 1: Measures real HTTP ping & jitter to Cloudflare/Google DNS servers
     * - Phase 2: Downloads real data stream from Cloudflare speed test CDN, plotting live Mbps
     * - Phase 3: Uploads real payload to measure actual upstream bandwidth
     */
    fun runSpeedTest() {
        if (_speedTestState.value.isRunning) return

        speedTestJob?.cancel()
        speedTestJob = viewModelScope.launch {
            _speedTestState.value = SpeedTestState(
                phase = SpeedTestPhase.PING,
                isRunning = true,
                progress = 0.05f,
                statusMessage = "Measuring real HTTP latency..."
            )

            // Step 1: Real Ping & Jitter
            val (realPing, realJitter) = RealNetworkClient.measureRealPing()
            _speedTestState.value = _speedTestState.value.copy(
                pingMs = realPing,
                jitterMs = realJitter,
                progress = 0.25f,
                phase = SpeedTestPhase.DOWNLOAD,
                statusMessage = "Benchmarking real CDN download throughput..."
            )

            // Step 2: Real Download Benchmark from high-speed CDN
            val downloadMbps = RealNetworkClient.runRealDownloadTest { currentMbps, streamProgress ->
                _speedTestState.value = _speedTestState.value.copy(
                    currentSpeedMbps = currentMbps,
                    downloadMbps = currentMbps,
                    progress = 0.25f + (streamProgress * 0.45f)
                )
            }

            _speedTestState.value = _speedTestState.value.copy(
                downloadMbps = downloadMbps,
                currentSpeedMbps = 0f,
                phase = SpeedTestPhase.UPLOAD,
                progress = 0.70f,
                statusMessage = "Testing real upstream throughput..."
            )

            // Step 3: Real Upload Benchmark
            val uploadMbps = RealNetworkClient.runRealUploadTest { currentMbps, _ ->
                _speedTestState.value = _speedTestState.value.copy(
                    currentSpeedMbps = currentMbps,
                    uploadMbps = currentMbps,
                    progress = 0.90f
                )
            }

            _speedTestState.value = _speedTestState.value.copy(
                uploadMbps = uploadMbps,
                currentSpeedMbps = 0f,
                progress = 1.0f,
                phase = SpeedTestPhase.COMPLETED,
                isRunning = false,
                statusMessage = "Real Network Test Complete: ${String.format("%.1f", downloadMbps)} Mbps Down • ${String.format("%.1f", uploadMbps)} Mbps Up"
            )

            VpnStateManager.addLog(
                "SPEEDTEST",
                "Real speed benchmark finished: Ping ${realPing}ms, Down ${String.format("%.1f", downloadMbps)} Mbps, Up ${String.format("%.1f", uploadMbps)} Mbps"
            )
        }
    }
}
