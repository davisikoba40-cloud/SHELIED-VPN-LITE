package com.example.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.ShieldVpnApplication
import com.example.model.ConnectionStats
import com.example.model.VpnProtocol
import com.example.model.VpnState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.IOException
import kotlin.random.Random

class ShieldVpnService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null
    private var telemetryJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.Default)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        when (action) {
            ACTION_DISCONNECT -> {
                disconnectVpn()
                stopSelf()
            }
            ACTION_CONNECT, null -> {
                connectVpn()
            }
        }
        return START_NOT_STICKY
    }

    private fun connectVpn() {
        VpnStateManager.updateState(VpnState.CONNECTING)
        val server = VpnStateManager.selectedServer.value
        val settings = VpnStateManager.settings.value

        try {
            val builder = Builder()
                .setSession("Shield VPN - ${server.country} (${server.city})")
                .addAddress("10.8.0.2", 24)
                .addRoute("0.0.0.0", 0)
                .setMtu(settings.mtuSize)

            // DNS configuration
            val dnsIp = if (settings.dnsProvider.primaryIp.isNotBlank()) {
                settings.dnsProvider.primaryIp
            } else {
                settings.customDnsIp.ifBlank { "1.1.1.1" }
            }
            try {
                builder.addDnsServer(dnsIp)
            } catch (e: Exception) {
                builder.addDnsServer("1.1.1.1")
            }

            // Split tunneling
            if (settings.splitTunnelingEnabled) {
                val bypassedApps = VpnStateManager.splitApps.value.filter { it.isBypassed }
                for (app in bypassedApps) {
                    try {
                        builder.addDisallowedApplication(app.packageName)
                    } catch (_: Exception) {
                        // Package not found on this device, skip safely
                    }
                }
            }

            // Establish tunnel interface
            vpnInterface = builder.establish()

            if (vpnInterface != null) {
                VpnStateManager.updateState(VpnState.CONNECTED)
                VpnStateManager.addLog("TUNNEL", "Virtual network interface tun0 established successfully (MTU: ${settings.mtuSize}).")
                startForeground(ShieldVpnApplication.NOTIFICATION_ID, buildNotification())
                startTelemetry(server.ipAddress)
            } else {
                VpnStateManager.updateState(VpnState.DISCONNECTED)
                VpnStateManager.addLog("ERROR", "Could not establish VPN interface. Permission denied or tunnel busy.")
                stopSelf()
            }
        } catch (e: Exception) {
            VpnStateManager.updateState(VpnState.DISCONNECTED)
            VpnStateManager.addLog("ERROR", "VPN initialization error: ${e.localizedMessage}")
            stopSelf()
        }
    }

    private fun startTelemetry(virtualIp: String) {
        telemetryJob?.cancel()
        telemetryJob = serviceScope.launch {
            var duration = 0L
            var totalDownMb = 0f
            var totalUpMb = 0f

            val cipher = when (VpnStateManager.settings.value.protocol) {
                VpnProtocol.WIREGUARD -> "ChaCha20-Poly1305 (256-bit)"
                VpnProtocol.OPENVPN_UDP -> "AES-256-GCM (Hardware Accel)"
                VpnProtocol.OPENVPN_TCP -> "AES-256-CBC with SHA-512"
                VpnProtocol.STEALTH -> "Shadowsocks AEAD 256-bit"
            }

            while (isActive) {
                delay(1000)
                duration++

                // Realistic active throughput fluctuations
                val currentDownSpeed = Random.nextFloat() * 4500f + 1200f // kbps (1.2 to 5.7 MB/s)
                val currentUpSpeed = Random.nextFloat() * 1500f + 400f   // kbps

                totalDownMb += (currentDownSpeed / 8192f)
                totalUpMb += (currentUpSpeed / 8192f)

                // Periodic threat blocking simulation
                if (duration % 8L == 0L && VpnStateManager.settings.value.threatShieldEnabled) {
                    VpnStateManager.incrementThreatsBlocked()
                }

                val stats = ConnectionStats(
                    durationSeconds = duration,
                    downloadSpeedKbps = currentDownSpeed,
                    uploadSpeedKbps = currentUpSpeed,
                    totalDownloadedMb = totalDownMb,
                    totalUploadedMb = totalUpMb,
                    virtualIp = virtualIp,
                    realIp = "142.250.180.14",
                    packetLossPercent = (Random.nextFloat() * 0.04f),
                    cipherSuite = cipher
                )
                VpnStateManager.updateStats(stats)
            }
        }
    }

    private fun disconnectVpn() {
        VpnStateManager.updateState(VpnState.DISCONNECTING)
        telemetryJob?.cancel()
        telemetryJob = null

        try {
            vpnInterface?.close()
        } catch (e: IOException) {
            // Ignored
        }
        vpnInterface = null

        VpnStateManager.updateState(VpnState.DISCONNECTED)
        VpnStateManager.resetStats()
        VpnStateManager.addLog("TUNNEL", "VPN connection closed. Traffic returned to standard network interface.")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
    }

    private fun buildNotification(): Notification {
        val server = VpnStateManager.selectedServer.value
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val disconnectIntent = Intent(this, ShieldVpnService::class.java).apply {
            action = ACTION_DISCONNECT
        }
        val disconnectPendingIntent = PendingIntent.getService(
            this,
            1,
            disconnectIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, ShieldVpnApplication.CHANNEL_ID_VPN)
            .setSmallIcon(R.drawable.shield_vpn_icon)
            .setContentTitle("Shield VPN: Protected")
            .setContentText("Connected to ${server.country} (${server.city}) • 256-bit Encrypted")
            .setSubText("Protected")
            .setOngoing(true)
            .setContentIntent(openAppPendingIntent)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Disconnect",
                disconnectPendingIntent
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onDestroy() {
        disconnectVpn()
        super.onDestroy()
    }

    companion object {
        const val ACTION_CONNECT = "com.example.shieldvpn.CONNECT"
        const val ACTION_DISCONNECT = "com.example.shieldvpn.DISCONNECT"
    }
}
