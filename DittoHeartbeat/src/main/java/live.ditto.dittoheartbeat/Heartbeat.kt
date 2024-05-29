package live.ditto.dittoheartbeat

import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import live.ditto.Ditto
import live.ditto.DittoConnectionType
import live.ditto.DittoPeer
import live.ditto.DittoSyncSubscription
import live.ditto.dittohealthmetrics.HealthMetric
import live.ditto.dittohealthmetrics.HealthMetricProvider
import org.joda.time.DateTime
import java.util.concurrent.atomic.AtomicBoolean

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
    val peerKey: String,

    /**
     * The current state of any `HealthMetric`s tracked by the Heartbeat Tool.
     */
    var healthMetrics: MutableMap<String, HealthMetric> = mutableMapOf()
)

var heartbeatSubscription: DittoSyncSubscription? = null
lateinit var info: DittoHeartbeatInfo

private fun updateHealthMetrics(config: DittoHeartbeatConfig) {
    val newHealthMetrics = mutableMapOf<String, HealthMetric>()
    config.healthMetricProviders?.forEach { provider ->
        newHealthMetrics[provider.metricName] = provider.getCurrentState()
    }
    info.healthMetrics = newHealthMetrics
}

@RequiresApi(Build.VERSION_CODES.O)
fun startHeartbeat(ditto: Ditto, config: DittoHeartbeatConfig): Flow<DittoHeartbeatInfo> = flow {
    val cancelable = AtomicBoolean(false)

    if (heartbeatSubscription == null) {
        heartbeatSubscription = ditto.sync.registerSubscription("SELECT * FROM $HEARTBEAT_COLLECTION_COLLECTION_NAME")
    }

    try {
        while (!cancelable.get()) {
            delay((config.secondsInterval * 1000L))
            val timestamp = DateTime().toISOString()
            val presenceData = observePeers(ditto)

            info =
                DittoHeartbeatInfo(
                    id = config.id,
                    lastUpdated = timestamp,
                    presenceSnapshotDirectlyConnectedPeers = getConnections(presenceData, ditto) ,
                    metaData = config.metaData,
                    presenceSnapshotDirectlyConnectedPeersCount = presenceData.size,
                    secondsInterval = config.secondsInterval,
                    sdk = ditto.sdkVersion,
                    schema = HEARTBEAT_COLLECTION_SCHEMA_VALUE,
                    peerKey = ditto.presence.graph.localPeer.peerKeyString
                )

            updateHealthMetrics(config)

            addToCollection(info, config, ditto)
            emit(info)
        }
    } finally {
        heartbeatSubscription!!.close()
    }
}

fun observePeers(ditto: Ditto): List<DittoPeer> {
    val presenceGraph = ditto.presence.graph
    return presenceGraph.remotePeers
}

fun addToCollection(info: DittoHeartbeatInfo, config: DittoHeartbeatConfig, ditto: Ditto) {
    if(!config.publishToDittoCollection) return
    val metaData = config.metaData ?: emptyMap()
    val doc = mapOf(
        "_id" to info.id,
        "secondsInterval" to info.secondsInterval,
        "presenceSnapshotDirectlyConnectedPeersCount" to (info.presenceSnapshotDirectlyConnectedPeersCount),
        "lastUpdated" to info.lastUpdated,
        "presenceSnapshotDirectlyConnectedPeers" to info.presenceSnapshotDirectlyConnectedPeers,
        "metaData" to metaData,
        "sdk" to info.sdk,
        "_schema" to info.schema,
        "peerKey" to info.peerKey
    )

    ditto.store.collection(HEARTBEAT_COLLECTION_COLLECTION_NAME).upsert(doc)
}

@RequiresApi(Build.VERSION_CODES.O)
fun getConnections(presenceSnapshotDirectlyConnectedPeers: List<DittoPeer>?, ditto: Ditto): Map<String, Any> {

    val connectionsMap: MutableMap<String, Any> = mutableMapOf()

    presenceSnapshotDirectlyConnectedPeers?.forEach { connection ->
        val connectionsTypeMap = getConnectionTypeCount(dittoPeer = connection)

        val connectionMap: Map<String, Any?> = mapOf(
            "deviceName" to connection.deviceName,
            "sdk" to ditto.sdkVersion,
            "isConnectedToDittoCloud" to connection.isConnectedToDittoCloud,
            "bluetooth" to connectionsTypeMap["bt"],
            "p2pWifi" to connectionsTypeMap["p2pWifi"],
            "lan" to connectionsTypeMap["lan"],
            )

        connectionsMap[connection.peerKeyString] = connectionMap
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

