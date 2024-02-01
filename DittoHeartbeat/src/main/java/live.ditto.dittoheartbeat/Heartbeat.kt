package live.ditto.dittoheartbeat

import live.ditto.Ditto
import android.os.Handler
import android.os.Looper
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
//    val metaData: Map<String, Any>
)

data class HeartbeatInfo(
    val id: Map<String, String>,
    val lastUpdated: String,
    val presence: Presence?,
)

data class Presence(
    val totalPeers: Int,
    val peers: List<DittoPeer>,
)

var presence: Presence? = null

fun startHeartbeat(ditto: Ditto, config: HeartbeatConfig, onDataLog: (HeartbeatInfo) -> Unit): Handler {
    val handler = Handler(Looper.getMainLooper())

    val runnable = object : Runnable {
        override fun run() {

            val timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault()).format(Date())
//            val deviceId = config.metaData["deviceId"] as? String ?: ""
//            val locationId = config.metaData["locationId"] as? String ?: ""

            val info = HeartbeatInfo(
                id = createCompositeId(config.id, ditto),
                lastUpdated = timestamp,
                presence = observePeers(ditto),
            )

            addToCollection(info, config, ditto)
            onDataLog(info)
            handler.postDelayed(this, config.interval) // Repeat every config.interval milliseconds
        }
    }

    handler.postDelayed(runnable, 0) // Initial delay

    return handler
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
    val totalPeers = presenceGraph.remotePeers.size
    val connectionsList = presenceGraph.remotePeers
    presence = Presence(totalPeers, connectionsList)

    return presence
}

fun addToCollection(info: HeartbeatInfo, config: HeartbeatConfig, ditto: Ditto) {

    val doc = mapOf(
        "_id" to info.id,
        "interval" to "${config.interval / 1000} sec",
        "totalPeers" to (info.presence?.totalPeers ?: 0),
        "lastUpdated" to info.lastUpdated,
        "presence" to getConnections(info.presence)
    )

    ditto.store.collection(config.collectionName).upsert(doc)
}

fun getConnections(presence: Presence?): Map<String, Any> {

    val connectionsMap: MutableMap<String, Any> = mutableMapOf()

    presence?.peers?.forEach { connection ->
        val connectionsTypeMap = getConnectionTypeCount(connection = connection)

        val connectionMap: Map<String, Any?> = mapOf(
            "deviceName" to connection.deviceName,
            "devicePeerKey" to byteArrayToHash(connection.peerKey),
            "isConnectedToDittoCloud" to connection.isConnectedToDittoCloud,
//            "totalPeers" to connection.connections.size,
            "bluetooth" to connectionsTypeMap["bt"],
            "p2pWifi" to connectionsTypeMap["p2pWifi"],
            "lan" to connectionsTypeMap["lan"],
            )

        connectionsMap["peer ${connectionsMap.size + 1}"] = connectionMap
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
