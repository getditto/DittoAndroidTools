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

    @Test
    fun `heartbeat info retains published peer key property`() {
        val info = DittoHeartbeatInfo(
            id = "heartbeat",
            lastUpdated = "timestamp",
            metaData = null,
            secondsInterval = 30,
            presenceSnapshotDirectlyConnectedPeersCount = 0,
            presenceSnapshotDirectlyConnectedPeers = emptyMap(),
            sdk = "5.0.1",
            schema = "1",
            peerKeyString = "peer-key",
        )

        assertEquals("peer-key", info.peerKeyString)
    }
}
