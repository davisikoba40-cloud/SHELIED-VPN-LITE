package com.example

import com.example.data.VpnRepository
import com.example.model.ServerCategory
import com.example.model.VpnProtocol
import com.example.model.VpnState
import com.example.service.VpnStateManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testVpnInitialState() {
        assertEquals(VpnState.DISCONNECTED, VpnStateManager.vpnState.value)
        assertNotNull(VpnStateManager.selectedServer.value)
    }

    @Test
    fun testVpnServerList() {
        val repo = VpnRepository()
        val servers = repo.servers.value
        assertTrue(servers.size >= 10)
        val fastest = repo.getFastestServer()
        assertNotNull(fastest)
    }

    @Test
    fun testFavoriteToggle() {
        val repo = VpnRepository()
        val server = repo.servers.value.first()
        val initialFav = server.isFavorite
        repo.toggleFavorite(server.id)
        val updated = repo.servers.value.first { it.id == server.id }
        assertEquals(!initialFav, updated.isFavorite)
    }

    @Test
    fun testSplitTunnelingToggle() {
        val app = VpnStateManager.splitApps.value.first()
        val initialBypass = app.isBypassed
        VpnStateManager.toggleSplitApp(app.packageName)
        val updated = VpnStateManager.splitApps.value.first { it.packageName == app.packageName }
        assertEquals(!initialBypass, updated.isBypassed)
    }
}
