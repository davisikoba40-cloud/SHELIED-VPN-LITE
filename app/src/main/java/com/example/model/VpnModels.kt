package com.example.model

enum class VpnState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    DISCONNECTING
}

enum class ServerCategory(val label: String, val iconName: String) {
    ALL("All Servers", "Public"),
    FASTEST("Fastest ⚡", "Bolt"),
    STREAMING("Streaming 🎬", "Movie"),
    P2P("P2P / Torrent ⚡", "Bolt"),
    DOUBLE_VPN("Double VPN 🔒", "Security"),
    FAVORITES("Favorites ⭐", "Star")
}

enum class VpnProtocol(val displayName: String, val description: String, val port: Int) {
    WIREGUARD("WireGuard", "Next-gen cryptography, ultra-fast throughput & instant reconnect", 51820),
    OPENVPN_UDP("OpenVPN (UDP)", "Industry standard high-speed tunneling with AES-256-GCM", 1194),
    OPENVPN_TCP("OpenVPN (TCP)", "Rock-solid reliability through strict firewalls and proxies", 443),
    STEALTH("Stealth Shield", "Shadowsocks obfuscation to bypass deep packet inspection (DPI)", 8388)
}

enum class DnsProvider(val title: String, val primaryIp: String, val secondaryIp: String, val description: String) {
    CLOUDFLARE("Cloudflare Private DNS", "1.1.1.1", "1.0.0.1", "Fastest global resolver with no browsing logs"),
    QUAD9("Quad9 Threat Blocker", "9.9.9.9", "149.112.112.112", "Real-time malicious domain and phishing protection"),
    ADGUARD("AdGuard DNS", "94.140.14.14", "94.140.15.15", "Blocks ads, trackers, telemetry, and banners"),
    GOOGLE("Google Public DNS", "8.8.8.8", "8.8.4.4", "Reliable and globally distributed DNS servers"),
    CUSTOM("Custom DNS", "1.1.1.1", "", "Specify your own upstream DNS resolver")
}

enum class TunnelMode(val title: String, val description: String) {
    SMART_DNS_SHIELD(
        "Smart Defense Tunnel (High Speed)",
        "Zero-leak encrypted DNS, threat filtering & unblocked web browsing for all apps without speed degradation"
    ),
    FULL_DEVICE_CLOAK(
        "Full Device IP Cloak",
        "Deep packet tunnel intercepting all device IPv4 routes through virtual interface"
    )
}

data class RealIpInfo(
    val ip: String = "Detecting...",
    val city: String = "Detecting location...",
    val region: String = "",
    val country: String = "Global",
    val isp: String = "Detecting carrier...",
    val isLoaded: Boolean = false
)

data class ServerLocation(
    val id: String,
    val country: String,
    val city: String,
    val flagEmoji: String,
    val pingMs: Int,
    val loadPercentage: Int,
    val ipAddress: String,
    val categories: List<ServerCategory> = listOf(ServerCategory.ALL),
    val isFavorite: Boolean = false,
    val streamingServices: List<String> = emptyList(),
    val doubleHopCity: String? = null
)

data class ConnectionStats(
    val durationSeconds: Long = 0L,
    val downloadSpeedKbps: Float = 0f,
    val uploadSpeedKbps: Float = 0f,
    val totalDownloadedMb: Float = 0f,
    val totalUploadedMb: Float = 0f,
    val virtualIp: String = "198.51.100.42",
    val realIp: String = "Detecting...",
    val packetLossPercent: Float = 0.0f,
    val cipherSuite: String = "ChaCha20-Poly1305 (256-bit)"
)

data class VpnSettings(
    val killSwitchEnabled: Boolean = true,
    val threatShieldEnabled: Boolean = true,
    val autoConnectWifi: Boolean = true,
    val splitTunnelingEnabled: Boolean = false,
    val protocol: VpnProtocol = VpnProtocol.WIREGUARD,
    val dnsProvider: DnsProvider = DnsProvider.CLOUDFLARE,
    val customDnsIp: String = "1.1.1.1",
    val mtuSize: Int = 1420,
    val bypassLocalLan: Boolean = true,
    val tunnelMode: TunnelMode = TunnelMode.SMART_DNS_SHIELD
)

data class SplitApp(
    val packageName: String,
    val appName: String,
    val isBypassed: Boolean = false,
    val category: String = "App"
)

enum class SpeedTestPhase {
    IDLE,
    PING,
    DOWNLOAD,
    UPLOAD,
    COMPLETED
}

data class SpeedTestState(
    val phase: SpeedTestPhase = SpeedTestPhase.IDLE,
    val currentSpeedMbps: Float = 0f,
    val progress: Float = 0f,
    val pingMs: Int = 0,
    val jitterMs: Int = 0,
    val downloadMbps: Float = 0f,
    val uploadMbps: Float = 0f,
    val isRunning: Boolean = false,
    val statusMessage: String = "Ready to test"
)

enum class AuditStatus {
    SECURE,
    WARNING,
    INFO
}

data class SecurityAuditItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val status: AuditStatus,
    val detail: String
)

data class VpnLogEntry(
    val id: Long = System.currentTimeMillis(),
    val timestamp: String,
    val tag: String,
    val message: String
)
