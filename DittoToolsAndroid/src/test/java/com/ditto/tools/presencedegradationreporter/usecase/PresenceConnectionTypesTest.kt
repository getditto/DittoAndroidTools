package com.ditto.tools.presencedegradationreporter.usecase

import com.ditto.kotlin.DittoConnectionType
import org.junit.Assert.assertEquals
import org.junit.Test

class PresenceConnectionTypesTest {
    @Test
    fun `lan uses access point enum case`() {
        val connectionTypes = listOf(
            DittoConnectionType.AccessPoint,
            DittoConnectionType.WebSocket,
            DittoConnectionType.AccessPoint,
            DittoConnectionType.Bluetooth,
        )

        assertEquals(2, countLanConnections(connectionTypes))
    }
}
