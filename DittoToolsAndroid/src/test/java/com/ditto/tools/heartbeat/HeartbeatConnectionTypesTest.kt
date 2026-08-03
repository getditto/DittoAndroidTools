package com.ditto.tools.heartbeat

import org.junit.Assert.assertEquals
import org.junit.Test

class HeartbeatConnectionTypesTest {
    @Test
    fun preservesEveryObservedConnectionType() {
        val connectionTypes = countConnectionTypes(
            listOf(
                "Bluetooth",
                "AccessPoint",
                "WebSocket",
                "Multicast",
                "FutureTransport",
                "FutureTransport",
            )
        )
        assertEquals(1, connectionTypes["Bluetooth"])
        assertEquals(1, connectionTypes["AccessPoint"])
        assertEquals(1, connectionTypes["WebSocket"])
        assertEquals(1, connectionTypes["Multicast"])
        assertEquals(2, connectionTypes["FutureTransport"])
    }
}
