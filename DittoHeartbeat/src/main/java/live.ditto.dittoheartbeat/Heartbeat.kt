package live.ditto.dittoheartbeat

import live.ditto.Ditto
import android.os.Handler
import android.os.Looper
import androidx.compose.material3.Text
import live.ditto.DittoConnectionType
import live.ditto.DittoPeer
import live.ditto.DittoPresenceObserver
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class HeartbeatConfig(
    val interval: Long,
    val collectionName: String,
    val metaData: Map<String, Any>
)

data class HeartbeatInfo(
    val id: Map<String, String>,
    val lastUpdated: String,
    val presence: Presence?,
)

data class Presence(
    val totalConnections: Int,
    val connections: List<DittoPeer>,
)
fun startHeartbeat(ditto: Ditto, config: HeartbeatConfig, onDataLog: (HeartbeatInfo) -> Unit): Handler {
    val handler = Handler(Looper.getMainLooper())

    val runnable = object : Runnable {
        override fun run() {

            val timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault()).format(Date())
            val deviceId = config.metaData["deviceId"] as? String ?: ""
            val locationId = config.metaData["locationId"] as? String ?: ""

            val info = HeartbeatInfo(
                id = mapOf("deviceId" to deviceId, "locationId" to locationId),
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



var presence: Presence? = null
fun observePeers(ditto: Ditto): Presence? {
    val presenceGraph = ditto.presence.graph
    val totalConnections = presenceGraph.remotePeers.size
    val connectionsList = presenceGraph.remotePeers
    presence = Presence(totalConnections, connectionsList)

    return presence
}

fun addToCollection(info: HeartbeatInfo, config: HeartbeatConfig, ditto: Ditto) {

    val doc = mapOf(
        "_id" to mapOf(
            "deviceId" to config.metaData["deviceId"],
            "locationId" to config.metaData["locationId"]
        ),
        "interval" to "${config.interval / 1000} sec",
        "totalConnections" to (info.presence?.totalConnections ?: 0),
        "lastUpdated" to info.lastUpdated,
        "presence" to getConnections(info.presence)
    )

    ditto.store.collection(config.collectionName).upsert(doc)
}

fun getConnections(presence: Presence?): Map<String, Any> {

    val connectionsMap: MutableMap<String, Any> = mutableMapOf()

    presence?.connections?.forEach { connection ->
        val connectionsTypeMap = getConnectionTypeCount(connection = connection)

        val connectionMap: Map<String, Any?> = mapOf(
            "deviceName" to connection.deviceName,
            "isConnectedToDittoCloud" to connection.isConnectedToDittoCloud,
            "totalConnections" to connection.connections.size,
            "bluetooth" to connectionsTypeMap["bt"],
            "p2pWifi" to connectionsTypeMap["p2pWifi"],
            "lan" to connectionsTypeMap["lan"],
            )

        connectionsMap["connection ${connectionsMap.size + 1}"] = connectionMap
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









// Usage Example:

// val config = HeartbeatConfig(
//     interval = 30000,
//     collectionName = "devices",
//     metadata = mapOf(
//         "locationId" to "...",
//         "deviceId" to "..."
//     )
// )

//val handler = startHeartbeat(dittoInstance, config) { info ->
//    // Custom logic for onDataLog
//     DataDog.log(info)
//}
//
// To stop the Heartbeat
//handler.removeCallbacksAndMessages(null)