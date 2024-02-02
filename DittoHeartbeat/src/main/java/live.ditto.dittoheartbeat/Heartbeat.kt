package live.ditto.dittoheartbeat

import live.ditto.Ditto
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import live.ditto.DittoConnectionType
import live.ditto.DittoPeer
import java.math.BigInteger
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class HeartbeatConfig(
    val id: Map<String, String>,
    val interval: Long,
    val collectionName: String,
)

data class HeartbeatInfo(
    val id: Map<String, String>,
    val lastUpdated: String,
    val presence: Presence?,
)

data class Presence(
    val remotePeersCount: Int,
    val peers: List<DittoPeer>,
)

var presence: Presence? = null

fun startHeartbeat(ditto: Ditto, config: HeartbeatConfig): Flow<HeartbeatInfo> = flow {
    while (true) {
        delay(config.interval)
        val timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault()).format(Date())
        val info = HeartbeatInfo(
            id = createCompositeId(config.id, ditto),
            lastUpdated = timestamp,
            presence = observePeers(ditto),
        )
        addToCollection(info, config, ditto)
        emit(info)
    }
}

fun createCompositeId(configId: Map<String, String>, ditto: Ditto): Map<String, String> {
    val compositeId: MutableMap<String, String> = configId.toMutableMap()
    val presenceGraph = ditto.presence.graph
    compositeId["dittoPeerKey"] = byteArrayToHash(presenceGraph.localPeer.peerKey)
    return compositeId
}

fun byteArrayToHash(byteArray: ByteArray): String {
    val md5 = MessageDigest.getInstance("md5")

    return BigInteger(1, md5.digest(byteArray))
        .toString(16)
        .padStart(32, '0')
}

fun observePeers(ditto: Ditto): Presence? {
    val presenceGraph = ditto.presence.graph
    val remotePeersCount = presenceGraph.remotePeers.size
    val connectionsList = presenceGraph.remotePeers
    presence = Presence(remotePeersCount, connectionsList)

    return presence
}
val myCoroutineScope = CoroutineScope(Dispatchers.Main)
fun addToCollection(info: HeartbeatInfo, config: HeartbeatConfig, ditto: Ditto) {

    val doc = mapOf(
        "_id" to info.id,
        "interval" to "${config.interval / 1000} sec",
        "remotePeersCount" to (info.presence?.remotePeersCount ?: 0),
        "lastUpdated" to info.lastUpdated,
        "presence" to getConnections(info.presence)
    )

    val query = "INSERT INTO ${config.collectionName} DOCUMENTS (:doc) ON ID CONFLICT DO UPDATE"

    myCoroutineScope.launch {
        ditto.store.execute(query, mapOf("doc" to doc))
    }
}

fun getConnections(presence: Presence?): Map<String, Any> {

    val connectionsMap: MutableMap<String, Any> = mutableMapOf()

    presence?.peers?.forEach { connection ->
        val connectionsTypeMap = getConnectionTypeCount(connection = connection)

        val connectionMap: Map<String, Any?> = mapOf(
            "deviceName" to connection.deviceName,
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
