package com.example.service

import com.example.model.ConnectionStats
import com.example.model.RealIpInfo
import com.example.model.ServerCategory
import com.example.model.ServerLocation
import com.example.model.SplitApp
import com.example.model.VpnLogEntry
import com.example.model.VpnSettings
import com.example.model.VpnState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object VpnStateManager {
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    val defaultServer = ServerLocation(
        id = "us_ny",
        country = "United States",
        city = "New York #1",
        flagEmoji = "🇺🇸",
        pingMs = 18,
        loadPercentage = 34,
        ipAddress = "198.51.100.42",
        categories = listOf(ServerCategory.ALL, ServerCategory.STREAMING, ServerCategory.FASTEST),
        streamingServices = listOf("Netflix", "Disney+", "Hulu", "Max")
    )

    private val _vpnState = MutableStateFlow(VpnState.DISCONNECTED)
    val vpnState: StateFlow<VpnState> = _vpnState.asStateFlow()

    private val _selectedServer = MutableStateFlow(defaultServer)
    val selectedServer: StateFlow<ServerLocation> = _selectedServer.asStateFlow()

    private val _stats = MutableStateFlow(ConnectionStats())
    val stats: StateFlow<ConnectionStats> = _stats.asStateFlow()

    private val _settings = MutableStateFlow(VpnSettings())
    val settings: StateFlow<VpnSettings> = _settings.asStateFlow()

    private val _realIpInfo = MutableStateFlow(RealIpInfo())
    val realIpInfo: StateFlow<RealIpInfo> = _realIpInfo.asStateFlow()

    private val _isInternetVerified = MutableStateFlow<Boolean?>(null)
    val isInternetVerified: StateFlow<Boolean?> = _isInternetVerified.asStateFlow()

    private val _threatsBlocked = MutableStateFlow(1428)
    val threatsBlocked: StateFlow<Int> = _threatsBlocked.asStateFlow()

    private val _logs = MutableStateFlow<List<VpnLogEntry>>(
        listOf(
            VpnLogEntry(
                timestamp = timeFormat.format(Date()),
                tag = "CORE",
                message = "Shield VPN security engine initialized. Powered by Mobiles."
            )
        )
    )
    val logs: StateFlow<List<VpnLogEntry>> = _logs.asStateFlow()

    private val _splitApps = MutableStateFlow<List<SplitApp>>(
        listOf(
            SplitApp("com.google.android.youtube", "YouTube", isBypassed = false, category = "Streaming"),
            SplitApp("com.netflix.mediaclient", "Netflix", isBypassed = false, category = "Streaming"),
            SplitApp("com.android.chrome", "Google Chrome", isBypassed = false, category = "Browsers"),
            SplitApp("org.mozilla.firefox", "Firefox Browser", isBypassed = false, category = "Browsers"),
            SplitApp("org.telegram.messenger", "Telegram", isBypassed = false, category = "Messaging"),
            SplitApp("com.whatsapp", "WhatsApp", isBypassed = false, category = "Messaging"),
            SplitApp("com.spotify.music", "Spotify", isBypassed = false, category = "Audio"),
            SplitApp("com.bankofamerica.mobilebanking", "Bank of America", isBypassed = true, category = "Finance / Bypass"),
            SplitApp("com.chase.sig.android", "Chase Mobile", isBypassed = true, category = "Finance / Bypass"),
            SplitApp("com.paypal.android.p2pmobile", "PayPal", isBypassed = true, category = "Finance / Bypass")
        )
    )
    val splitApps: StateFlow<List<SplitApp>> = _splitApps.asStateFlow()

    fun updateState(newState: VpnState) {
        _vpnState.value = newState
        addLog("STATE", "VPN state changed to: $newState")
    }

    fun selectServer(server: ServerLocation) {
        _selectedServer.value = server
        addLog("SERVER", "Selected server node: ${server.country} (${server.city}) - IP: ${server.ipAddress}")
    }

    fun updateStats(newStats: ConnectionStats) {
        _stats.value = newStats
    }

    fun updateRealIpInfo(info: RealIpInfo) {
        _realIpInfo.value = info
        addLog("NETWORK", "Detected Physical Network IP: ${info.ip} (${info.isp}, ${info.country})")
    }

    fun setInternetVerified(verified: Boolean?) {
        _isInternetVerified.value = verified
        if (verified != null) {
            addLog("VERIFY", if (verified) "Internet connectivity verified active through tunnel" else "Internet verification check failed")
        }
    }

    fun updateSettings(newSettings: VpnSettings) {
        _settings.value = newSettings
        addLog("CONFIG", "Updated VPN settings: Protocol=${newSettings.protocol.displayName}, Mode=${newSettings.tunnelMode.title}, KillSwitch=${newSettings.killSwitchEnabled}")
    }

    fun incrementThreatsBlocked() {
        _threatsBlocked.value += 1
    }

    fun toggleSplitApp(packageName: String) {
        val currentList = _splitApps.value.toMutableList()
        val index = currentList.indexOfFirst { it.packageName == packageName }
        if (index != -1) {
            val app = currentList[index]
            val updated = app.copy(isBypassed = !app.isBypassed)
            currentList[index] = updated
            _splitApps.value = currentList
            addLog("SPLIT", "App ${app.appName} bypass toggled: ${updated.isBypassed}")
        }
    }

    fun addLog(tag: String, message: String) {
        val entry = VpnLogEntry(
            timestamp = timeFormat.format(Date()),
            tag = tag,
            message = message
        )
        val current = _logs.value.toMutableList()
        current.add(0, entry)
        if (current.size > 100) {
            _logs.value = current.take(100)
        } else {
            _logs.value = current
        }
    }

    fun resetStats() {
        _stats.value = ConnectionStats()
    }
}
