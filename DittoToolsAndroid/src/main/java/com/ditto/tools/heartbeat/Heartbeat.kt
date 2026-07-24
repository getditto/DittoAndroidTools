package com.ditto.tools.heartbeat

import android.os.Build
import androidx.annotation.RequiresApi
import com.ditto.kotlin.Ditto
import com.ditto.kotlin.DittoConnectionType
import com.ditto.kotlin.DittoPeer
import com.ditto.kotlin.serialization.DittoCborSerializable
import com.ditto.kotlin.serialization.toDittoCbor
import com.ditto.tools.healthmetrics.HealthMetric
import com.ditto.tools.healthmetrics.HealthMetricProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.joda.time.DateTime
import java.util.concurrent.atomic.AtomicBoolean

// SDK v4 exposed this connection state as `isConnectedToDittoCloud`; SDK v5
// exposes the same state as `isConnectedToDittoServer`. Heartbeat documents are
// exchanged between independently versioned tools, so the stored key remains
// `isConnectedToDittoCloud`. Changing it would make existing heartbeat readers
// miss the connection state.
internal const val IS_CONNECTED_TO_DITTO_CLOUD_WIRE_KEY = "isConnectedToDittoCloud"

data class DittoHeartbeatConfig(
    val id: String,
    val secondsInterval: Int,
    val metaData: Map<String, Any>? = null,
    val healthMetricProviders: List<HealthMetricProvider>?,
    val publishToDittoCollection: Boolean = true // Toggle to avoid publishing
)

data class DittoHeartbeatInfo(
    val id: String,
    val lastUpdated: String,
    val metaData: Map<String, Any>?,
    val secondsInterval: Int,
    val presenceSnapshotDirectlyConnectedPeersCount: Int,
    val presenceSnapshotDirectlyConnectedPeers: Map<String, Any>,
    val sdk: String,
    val schema: String,
    val peerKeyString: String,
    /**
     * The current state of any `HealthMetric`s tracked by the Heartbeat Tool.
     */
    var healthMetrics: MutableMap<String, HealthMetric> = mutableMapOf()
)

private fun updateHealthMetrics(info: DittoHeartbeatInfo, config: DittoHeartbeatConfig) {
    val newHealthMetrics = mutableMapOf<String, HealthMetric>()
    config.healthMetricProviders?.forEach { provider ->
        newHealthMetrics[provider.metricName] = provider.getCurrentState()
    }
    info.healthMetrics = newHealthMetrics
}

@RequiresApi(Build.VERSION_CODES.O)
fun startHeartbeat(ditto: Ditto, config: DittoHeartbeatConfig): Flow<DittoHeartbeatInfo> = flow {
    val cancelable = AtomicBoolean(false)

    while (!cancelable.get()) {
        val timestamp = DateTime().toISOString()
        val presenceData = observePeers(ditto)

        val info = DittoHeartbeatInfo(
                id = config.id,
                lastUpdated = timestamp,
                presenceSnapshotDirectlyConnectedPeers = getConnections(presenceData, ditto),
                metaData = config.metaData,
                presenceSnapshotDirectlyConnectedPeersCount = presenceData.size,
                secondsInterval = config.secondsInterval,
                sdk = Ditto.VERSION,
                schema = HEARTBEAT_COLLECTION_SCHEMA_VALUE,
                peerKeyString = ditto.presence.graph.localPeer.peerKey
            )

        updateHealthMetrics(info, config)

        addToCollection(info, config, ditto)
        emit(info)
        delay((config.secondsInterval * 1000L))
    }
}

fun observePeers(ditto: Ditto): List<DittoPeer> {
    val presenceGraph = ditto.presence.graph
    return presenceGraph.remotePeers
}

private fun anyToCbor(value: Any?): DittoCborSerializable = when (value) {
    is String -> value.toDittoCbor()
    is Int -> value.toDittoCbor()
    is Long -> value.toDittoCbor()
    is Double -> value.toDittoCbor()
    is Float -> value.toDittoCbor()
    is Boolean -> value.toDittoCbor()
    is Map<*, *> -> {
        @Suppress("UNCHECKED_CAST")
        val map = value as Map<String, Any?>
        map.mapValues { (_, v) -> anyToCbor(v) }.toDittoCbor()
    }
    is List<*> -> value.map { anyToCbor(it) }.toDittoCbor()
    null -> DittoCborSerializable.NullValue()
    else -> value.toString().toDittoCbor()
}

suspend fun addToCollection(info: DittoHeartbeatInfo, config: DittoHeartbeatConfig, ditto: Ditto) {
    if (!config.publishToDittoCollection) return
    val metaData = config.metaData ?: emptyMap()
    val doc = mapOf(
        "_id" to info.id.toDittoCbor(),
        "secondsInterval" to info.secondsInterval.toDittoCbor(),
        "presenceSnapshotDirectlyConnectedPeersCount" to info.presenceSnapshotDirectlyConnectedPeersCount.toDittoCbor(),
        "presenceSnapshotDirectlyConnectedPeers" to info.presenceSnapshotDirectlyConnectedPeers.mapValues { (_, v) -> anyToCbor(v) }.toDittoCbor(),
        "lastUpdated" to info.lastUpdated.toDittoCbor(),
        "metaData" to metaData.mapValues { (_, v) -> anyToCbor(v) }.toDittoCbor(),
        "sdk" to info.sdk.toDittoCbor(),
        "_schema" to info.schema.toDittoCbor(),
        "peerKey" to info.peerKeyString.toDittoCbor(),
    ).toDittoCbor()

    ditto.store.execute(
        "INSERT INTO $HEARTBEAT_COLLECTION_COLLECTION_NAME DOCUMENTS (:doc) ON ID CONFLICT DO UPDATE",
        mapOf("doc" to doc).toDittoCbor()
    )
}

@RequiresApi(Build.VERSION_CODES.O)
fun getConnections(
    presenceSnapshotDirectlyConnectedPeers: List<DittoPeer>?,
    ditto: Ditto
): Map<String, Any> {

    val connectionsMap: MutableMap<String, Any> = mutableMapOf()

    presenceSnapshotDirectlyConnectedPeers?.forEach { peer ->
        val connectionTypes = countConnectionTypes(peer.connections.map { it.connectionType.name })

        val connectionMap: Map<String, Any?> = mapOf(
            "deviceName" to peer.deviceName,
            "sdk" to Ditto.VERSION,
            // Read the SDK v5 property and store it under the stable heartbeat
            // document key declared above.
            IS_CONNECTED_TO_DITTO_CLOUD_WIRE_KEY to peer.isConnectedToDittoServer,
            "bluetooth" to (connectionTypes[DittoConnectionType.Bluetooth.name] ?: 0),
            "p2pWifi" to (connectionTypes[DittoConnectionType.P2PWiFi.name] ?: 0),
            // The SDK enum calls LAN `AccessPoint`. WebSocket is a separate
            // transport in the open-ended connectionTypes map.
            "lan" to (connectionTypes[DittoConnectionType.AccessPoint.name] ?: 0),
            "connectionTypes" to connectionTypes,
        )

        connectionsMap[peer.peerKey] = connectionMap
    }

    return connectionsMap
}

fun getConnectionTypeCount(dittoPeer: DittoPeer): Map<String, Int> {
    val connectionTypes = countConnectionTypes(dittoPeer.connections.map { it.connectionType.name })
    return mapOf(
        "bt" to (connectionTypes[DittoConnectionType.Bluetooth.name] ?: 0),
        "p2pWifi" to (connectionTypes[DittoConnectionType.P2PWiFi.name] ?: 0),
        "lan" to (connectionTypes[DittoConnectionType.AccessPoint.name] ?: 0),
    )
}

internal fun countConnectionTypes(names: Iterable<String>): Map<String, Int> =
    names.groupingBy { it }.eachCount()
