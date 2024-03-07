package live.ditto.dittoheartbeat

import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import live.ditto.Ditto
import live.ditto.DittoConnectionType
import live.ditto.DittoPeer
import live.ditto.DittoSyncSubscription
import org.joda.time.DateTime
import java.util.Base64
import java.util.concurrent.atomic.AtomicBoolean

data class DittoHeartbeatConfig(
    val id: Map<String, String>,
    val secondsInterval: Int,
    val collectionName: String,
    val metaData: Map<String, Any>? = null
)

data class DittoHeartbeatInfo(
    val id: Map<String, String>,
    val lastUpdated: String,
    val metaData: Map<String, Any>?,
    val secondsInterval: Int,
    val remotePeersCount: Int,
    val peerConnections: Map<String, Any>,
    val sdk: String
)

var heartbeatSubscription: DittoSyncSubscription? = null

@RequiresApi(Build.VERSION_CODES.O)
fun startHeartbeat(ditto: Ditto, config: DittoHeartbeatConfig): Flow<DittoHeartbeatInfo> = flow {
    val cancelable = AtomicBoolean(false)

    if (heartbeatSubscription == null) {
        heartbeatSubscription = ditto.sync.registerSubscription("SELECT * FROM ${config.collectionName}")
    }

    try {
        while (!cancelable.get()) {
            delay((config.secondsInterval * 1000L))
            val timestamp = DateTime().toISOString()
            val presenceData = observePeers(ditto)

            val info =
                DittoHeartbeatInfo(
                    id = createCompositeId(config.id, ditto),
                    lastUpdated = timestamp,
                    peerConnections = getConnections(presenceData, ditto) ,
                    metaData = config.metaData,
                    remotePeersCount = presenceData?.size ?: 0,
                    secondsInterval = config.secondsInterval,
                    sdk = ditto.sdkVersion
                )

            addToCollection(info, config, ditto)
            emit(info)
        }
    } finally {
        heartbeatSubscription!!.close()
    }
}

fun createCompositeId(configId: Map<String, String>, ditto: Ditto): Map<String, String> {
    val compositeId: MutableMap<String, String> = configId.toMutableMap()
    val presenceGraph = ditto.presence.graph
    compositeId["pk"] = byteArrayToHash(presenceGraph.localPeer.peerKey)
    return compositeId
}

@RequiresApi(Build.VERSION_CODES.O)
fun byteArrayToHash(byteArray: ByteArray): String {
    val base64String = Base64.getEncoder().encodeToString(byteArray)
    return "pk:$base64String"
}

fun observePeers(ditto: Ditto): List<DittoPeer>? {
    val presenceGraph = ditto.presence.graph
    return presenceGraph.remotePeers
}

val myCoroutineScope = CoroutineScope(Dispatchers.Main)
fun addToCollection(info: DittoHeartbeatInfo, config: DittoHeartbeatConfig, ditto: Ditto) {
    val metaData = config.metaData ?: emptyMap()
    val doc = mapOf(
        "_id" to info.id,
        "secondsInterval" to info.secondsInterval,
        "remotePeersCount" to (info.remotePeersCount),
        "lastUpdated" to info.lastUpdated,
        "peerConnections" to info.peerConnections,
        "metaData" to metaData,
        "sdk" to info.sdk
    )
    val query = "INSERT INTO ${config.collectionName} DOCUMENTS (:doc) ON ID CONFLICT DO UPDATE"
    myCoroutineScope.launch {
        ditto.store.execute(query, mapOf("doc" to doc))
    }
}

fun getConnections(peerConnections: List<DittoPeer>?, ditto: Ditto): Map<String, Any> {

    val connectionsMap: MutableMap<String, Any> = mutableMapOf()

    peerConnections?.forEach { connection ->
        val connectionsTypeMap = getConnectionTypeCount(connection = connection)

        val connectionMap: Map<String, Any?> = mapOf(
            "deviceName" to connection.deviceName,
            "sdk" to ditto.sdkVersion,
            "isConnectedToDittoCloud" to connection.isConnectedToDittoCloud,
            "bluetooth" to connectionsTypeMap["bt"],
            "p2pWifi" to connectionsTypeMap["p2pWifi"],
            "lan" to connectionsTypeMap["lan"],
            )

        connectionsMap[byteArrayToHash(connection.peerKey)] = connectionMap
    }

    return connectionsMap
}

fun getConnectionTypeCount(connection: DittoPeer): Map<String, Int> {
    var bt = 0
    var p2pWifi = 0
    var lan = 0

    connection.connections.forEach { connection ->
        when (connection.connectionType) {
            DittoConnectionType.Bluetooth -> bt += 1
            DittoConnectionType.P2PWiFi -> p2pWifi += 1
            DittoConnectionType.AccessPoint, DittoConnectionType.WebSocket -> lan += 1
        }
    }
    return mapOf("bt" to bt, "p2pWifi" to p2pWifi, "lan" to lan)
}

