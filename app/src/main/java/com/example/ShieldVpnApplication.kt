package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class ShieldVpnApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID_VPN,
                "Shield VPN Active Connection",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows active VPN connection status, metrics, and security controls"
                setShowBadge(false)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID_VPN = "shield_vpn_active_channel"
        const val NOTIFICATION_ID = 1001
    }
}
