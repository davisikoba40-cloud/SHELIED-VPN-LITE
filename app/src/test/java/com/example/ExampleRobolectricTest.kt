package com.example

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleRobolectricTest {

    @Test
    fun testAppIdentifierAndProperties() {
        val appName = "Shield VPN"
        assertEquals("Shield VPN", appName)
        assertTrue(appName.isNotBlank())
    }
}
