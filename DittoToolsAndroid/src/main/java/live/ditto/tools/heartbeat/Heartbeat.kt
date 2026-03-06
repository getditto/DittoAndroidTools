package live.ditto.tools.heartbeat

import android.os.Build
import androidx.annotation.RequiresApi
import com.ditto.kotlin.Ditto
import com.ditto.kotlin.DittoConnectionType
import com.ditto.kotlin.DittoPeer
import com.ditto.kotlin.serialization.toDittoCbor
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.joda.time.DateTime
import java.util.concurrent.atomic.AtomicBoolean

data class DittoHeartbeatConfig(
    val id: String,
    val secondsInterval: Int,
    val metaData: Map<String, Any>? = null,
    val healthMetricProviders: List<live.ditto.tools.healthmetrics.HealthMetricProvider>?,
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
    var healthMetrics: MutableMap<String, live.ditto.tools.healthmetrics.HealthMetric> = mutableMapOf()
)

lateinit var info: DittoHeartbeatInfo

private fun updateHealthMetrics(config: DittoHeartbeatConfig) {
    val newHealthMetrics = mutableMapOf<String, live.ditto.tools.healthmetrics.HealthMetric>()
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

        info =
            DittoHeartbeatInfo(
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

        updateHealthMetrics(config)

        addToCollection(info, config, ditto)
        emit(info)
        delay((config.secondsInterval * 1000L))
    }
}

fun observePeers(ditto: Ditto): List<DittoPeer> {
    val presenceGraph = ditto.presence.graph
    return presenceGraph.remotePeers
}

suspend fun addToCollection(info: DittoHeartbeatInfo, config: DittoHeartbeatConfig, ditto: Ditto) {
    if (!config.publishToDittoCollection) return
    val metaData = config.metaData ?: emptyMap()
    val doc = mapOf(
        "_id" to info.id.toDittoCbor(),
        "secondsInterval" to info.secondsInterval.toDittoCbor(),
        "presenceSnapshotDirectlyConnectedPeersCount" to info.presenceSnapshotDirectlyConnectedPeersCount.toDittoCbor(),
        "lastUpdated" to info.lastUpdated.toDittoCbor(),
        "metaData" to metaData.toString().toDittoCbor(),
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
        val connectionsTypeMap = getConnectionTypeCount(dittoPeer = peer)

        val connectionMap: Map<String, Any?> = mapOf(
            "deviceName" to peer.deviceName,
            "sdk" to Ditto.VERSION,
            "isConnectedToDittoCloud" to peer.isConnectedToDittoServer,
            "bluetooth" to connectionsTypeMap["bt"],
            "p2pWifi" to connectionsTypeMap["p2pWifi"],
            "lan" to connectionsTypeMap["lan"],
        )

        connectionsMap[peer.peerKey] = connectionMap
    }

    return connectionsMap
}

fun getConnectionTypeCount(dittoPeer: DittoPeer): Map<String, Int> {
    var bt = 0
    var p2pWifi = 0
    var lan = 0

    dittoPeer.connections.forEach { connection ->
        when (connection.connectionType) {
            DittoConnectionType.Bluetooth -> bt += 1
            DittoConnectionType.P2PWiFi -> p2pWifi += 1
            DittoConnectionType.AccessPoint, DittoConnectionType.WebSocket -> lan += 1
        }
    }
    return mapOf("bt" to bt, "p2pWifi" to p2pWifi, "lan" to lan)
}
