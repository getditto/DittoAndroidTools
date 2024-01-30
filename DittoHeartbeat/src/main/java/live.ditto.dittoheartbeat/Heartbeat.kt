package live.ditto.dittoheartbeat

import live.ditto.Ditto
import android.os.Handler
import android.os.Looper
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
//    val diskSpace: String
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
//                diskSpace = /* Logic to get disk space from disk utility */
            )

            onDataLog(info)
            handler.postDelayed(this, config.interval) // Repeat every config.interval milliseconds
        }
    }

    handler.postDelayed(runnable, 0) // Initial delay

    return handler
}


var peersObserver: DittoPresenceObserver? = null

var presence: Presence? = null
fun observePeers(ditto: Ditto): Presence? {
//    peersObserver = ditto.presence.observe { graph ->
//        presence = Presence(totalConnections = graph.localPeer.connections.size, connections = graph.remotePeers)
//    }
//
//    return presence

    val presenceGraph = ditto.presence.graph
    val totalConnections = presenceGraph.remotePeers.size
    val connectionsList = presenceGraph.remotePeers
    presence = Presence(totalConnections, connectionsList)

    return presence
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