package com.example.data

import com.example.model.AuditStatus
import com.example.model.SecurityAuditItem
import com.example.model.ServerCategory
import com.example.model.ServerLocation
import com.example.service.VpnStateManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class VpnRepository {

    private val initialServers = listOf(
        ServerLocation(
            id = "us_ny",
            country = "United States",
            city = "New York #1",
            flagEmoji = "🇺🇸",
            pingMs = 16,
            loadPercentage = 32,
            ipAddress = "198.51.100.42",
            categories = listOf(ServerCategory.ALL, ServerCategory.FASTEST, ServerCategory.STREAMING, ServerCategory.P2P),
            isFavorite = true,
            streamingServices = listOf("Netflix", "Disney+", "Hulu", "Max")
        ),
        ServerLocation(
            id = "us_la",
            country = "United States",
            city = "Los Angeles #4",
            flagEmoji = "🇺🇸",
            pingMs = 24,
            loadPercentage = 48,
            ipAddress = "198.51.100.89",
            categories = listOf(ServerCategory.ALL, ServerCategory.STREAMING),
            streamingServices = listOf("Netflix", "Prime Video", "ESPN+")
        ),
        ServerLocation(
            id = "us_mia",
            country = "United States",
            city = "Miami #2",
            flagEmoji = "🇺🇸",
            pingMs = 28,
            loadPercentage = 41,
            ipAddress = "198.51.100.115",
            categories = listOf(ServerCategory.ALL, ServerCategory.P2P)
        ),
        ServerLocation(
            id = "uk_lon",
            country = "United Kingdom",
            city = "London #1",
            flagEmoji = "🇬🇧",
            pingMs = 22,
            loadPercentage = 39,
            ipAddress = "203.0.113.12",
            categories = listOf(ServerCategory.ALL, ServerCategory.FASTEST, ServerCategory.STREAMING, ServerCategory.P2P),
            isFavorite = true,
            streamingServices = listOf("BBC iPlayer", "ITVX", "Channel 4", "Netflix UK")
        ),
        ServerLocation(
            id = "de_fra",
            country = "Germany",
            city = "Frankfurt #3",
            flagEmoji = "🇩🇪",
            pingMs = 19,
            loadPercentage = 27,
            ipAddress = "194.135.210.15",
            categories = listOf(ServerCategory.ALL, ServerCategory.FASTEST, ServerCategory.P2P, ServerCategory.DOUBLE_VPN),
            doubleHopCity = "Zurich (Switzerland)"
        ),
        ServerLocation(
            id = "nl_ams",
            country = "Netherlands",
            city = "Amsterdam #2",
            flagEmoji = "🇳🇱",
            pingMs = 21,
            loadPercentage = 35,
            ipAddress = "185.220.101.5",
            categories = listOf(ServerCategory.ALL, ServerCategory.P2P, ServerCategory.FASTEST),
            isFavorite = true
        ),
        ServerLocation(
            id = "ch_zur",
            country = "Switzerland",
            city = "Zurich #1",
            flagEmoji = "🇨🇭",
            pingMs = 26,
            loadPercentage = 22,
            ipAddress = "193.138.218.7",
            categories = listOf(ServerCategory.ALL, ServerCategory.DOUBLE_VPN),
            doubleHopCity = "Reykjavik (Iceland)"
        ),
        ServerLocation(
            id = "jp_tok",
            country = "Japan",
            city = "Tokyo #2",
            flagEmoji = "🇯🇵",
            pingMs = 45,
            loadPercentage = 54,
            ipAddress = "133.242.18.99",
            categories = listOf(ServerCategory.ALL, ServerCategory.STREAMING),
            streamingServices = listOf("AbemaTV", "TVer", "Netflix JP")
        ),
        ServerLocation(
            id = "sg_sin",
            country = "Singapore",
            city = "Singapore Central",
            flagEmoji = "🇸🇬",
            pingMs = 38,
            loadPercentage = 42,
            ipAddress = "128.199.200.14",
            categories = listOf(ServerCategory.ALL, ServerCategory.FASTEST, ServerCategory.P2P)
        ),
        ServerLocation(
            id = "ca_tor",
            country = "Canada",
            city = "Toronto #1",
            flagEmoji = "🇨🇦",
            pingMs = 31,
            loadPercentage = 33,
            ipAddress = "192.0.2.144",
            categories = listOf(ServerCategory.ALL, ServerCategory.STREAMING),
            streamingServices = listOf("Crave", "CBC Gem")
        ),
        ServerLocation(
            id = "au_syd",
            country = "Australia",
            city = "Sydney #1",
            flagEmoji = "🇦🇺",
            pingMs = 62,
            loadPercentage = 46,
            ipAddress = "139.130.4.5",
            categories = listOf(ServerCategory.ALL, ServerCategory.STREAMING),
            streamingServices = listOf("Stan", "Kayo Sports")
        ),
        ServerLocation(
            id = "fr_par",
            country = "France",
            city = "Paris #2",
            flagEmoji = "🇫🇷",
            pingMs = 25,
            loadPercentage = 38,
            ipAddress = "212.83.150.22",
            categories = listOf(ServerCategory.ALL, ServerCategory.STREAMING)
        ),
        ServerLocation(
            id = "se_sto",
            country = "Sweden",
            city = "Stockholm #1",
            flagEmoji = "🇸🇪",
            pingMs = 29,
            loadPercentage = 24,
            ipAddress = "193.180.12.8",
            categories = listOf(ServerCategory.ALL, ServerCategory.P2P, ServerCategory.DOUBLE_VPN),
            doubleHopCity = "Amsterdam (Netherlands)"
        ),
        ServerLocation(
            id = "is_rey",
            country = "Iceland",
            city = "Reykjavik #1",
            flagEmoji = "🇮🇸",
            pingMs = 42,
            loadPercentage = 18,
            ipAddress = "194.105.224.2",
            categories = listOf(ServerCategory.ALL, ServerCategory.DOUBLE_VPN),
            doubleHopCity = "Frankfurt (Germany)"
        ),
        ServerLocation(
            id = "br_sao",
            country = "Brazil",
            city = "São Paulo #1",
            flagEmoji = "🇧🇷",
            pingMs = 78,
            loadPercentage = 51,
            ipAddress = "177.18.200.10",
            categories = listOf(ServerCategory.ALL, ServerCategory.STREAMING)
        ),
        ServerLocation(
            id = "in_mum",
            country = "India",
            city = "Mumbai (Virtual)",
            flagEmoji = "🇮🇳",
            pingMs = 74,
            loadPercentage = 59,
            ipAddress = "103.21.244.0",
            categories = listOf(ServerCategory.ALL, ServerCategory.STREAMING),
            streamingServices = listOf("Hotstar", "JioCinema")
        ),
        ServerLocation(
            id = "ae_dxb",
            country = "UAE",
            city = "Dubai #1",
            flagEmoji = "🇦🇪",
            pingMs = 68,
            loadPercentage = 63,
            ipAddress = "185.190.140.2",
            categories = listOf(ServerCategory.ALL)
        ),
        ServerLocation(
            id = "kr_seo",
            country = "South Korea",
            city = "Seoul #1",
            flagEmoji = "🇰🇷",
            pingMs = 52,
            loadPercentage = 44,
            ipAddress = "121.134.180.2",
            categories = listOf(ServerCategory.ALL, ServerCategory.STREAMING),
            streamingServices = listOf("Wavve", "TVING")
        )
    )

    private val _servers = MutableStateFlow(initialServers)
    val servers: StateFlow<List<ServerLocation>> = _servers.asStateFlow()

    fun toggleFavorite(serverId: String) {
        val updated = _servers.value.map {
            if (it.id == serverId) {
                val newFav = !it.isFavorite
                val newCats = it.categories.toMutableList()
                if (newFav && !newCats.contains(ServerCategory.FAVORITES)) {
                    newCats.add(ServerCategory.FAVORITES)
                } else if (!newFav) {
                    newCats.remove(ServerCategory.FAVORITES)
                }
                it.copy(isFavorite = newFav, categories = newCats)
            } else {
                it
            }
        }
        _servers.value = updated
    }

    fun getFastestServer(): ServerLocation {
        return _servers.value.minByOrNull { it.pingMs + (it.loadPercentage / 4) } ?: initialServers.first()
    }

    fun getSecurityAuditItems(isConnected: Boolean, virtualIp: String, cipher: String): List<SecurityAuditItem> {
        return if (isConnected) {
            listOf(
                SecurityAuditItem(
                    id = "ip_mask",
                    title = "IP Address Cloaked",
                    subtitle = "Masked with virtual node IP: $virtualIp",
                    status = AuditStatus.SECURE,
                    detail = "Your real physical ISP IP and geolocation are completely hidden from internet sites."
                ),
                SecurityAuditItem(
                    id = "encryption",
                    title = "Military Grade Tunnel",
                    subtitle = cipher,
                    status = AuditStatus.SECURE,
                    detail = "All incoming and outgoing cellular/Wi-Fi traffic is encrypted with authenticated AEAD ciphers."
                ),
                SecurityAuditItem(
                    id = "dns_shield",
                    title = "DNS Leak Protection",
                    subtitle = "${VpnStateManager.settings.value.dnsProvider.title} Active",
                    status = AuditStatus.SECURE,
                    detail = "DNS queries are securely resolved through encrypted private tunnel without leaking to ISP."
                ),
                SecurityAuditItem(
                    id = "kill_switch",
                    title = "Network Kill Switch",
                    subtitle = if (VpnStateManager.settings.value.killSwitchEnabled) "Active • Zero Leak Guarantee" else "Disabled • Click to Enable",
                    status = if (VpnStateManager.settings.value.killSwitchEnabled) AuditStatus.SECURE else AuditStatus.WARNING,
                    detail = "Blocks all unencrypted internet traffic if connection ever drops unexpectedly."
                ),
                SecurityAuditItem(
                    id = "threat_shield",
                    title = "Threat Shield & Cyber Filter",
                    subtitle = if (VpnStateManager.settings.value.threatShieldEnabled) "Enabled • ${VpnStateManager.threatsBlocked.value} Bad Requests Blocked" else "Disabled",
                    status = if (VpnStateManager.settings.value.threatShieldEnabled) AuditStatus.SECURE else AuditStatus.WARNING,
                    detail = "Actively intercepts known tracking scripts, phishing links, and malicious ads."
                )
            )
        } else {
            listOf(
                SecurityAuditItem(
                    id = "ip_mask",
                    title = "Real IP Exposed to Network",
                    subtitle = "Physical IP 142.250.180.14 visible to web",
                    status = AuditStatus.WARNING,
                    detail = "Websites, trackers, and advertisers can log your physical location and device fingerprint."
                ),
                SecurityAuditItem(
                    id = "encryption",
                    title = "Unencrypted Traffic",
                    subtitle = "Wi-Fi & Cellular Data in Cleartext",
                    status = AuditStatus.WARNING,
                    detail = "Data sent over local Wi-Fi can be sniffed or monitored by network administrators."
                ),
                SecurityAuditItem(
                    id = "dns_shield",
                    title = "ISP DNS Logging",
                    subtitle = "Queries routed via local carrier",
                    status = AuditStatus.INFO,
                    detail = "Your internet service provider can see every domain name requested by your device."
                ),
                SecurityAuditItem(
                    id = "kill_switch",
                    title = "Kill Switch Ready",
                    subtitle = "Arm before connecting",
                    status = AuditStatus.INFO,
                    detail = "Shield VPN will lock network traffic if disconnected once turned on."
                )
            )
        }
    }
}
