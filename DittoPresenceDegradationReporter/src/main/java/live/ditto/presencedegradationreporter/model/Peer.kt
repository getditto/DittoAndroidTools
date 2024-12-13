package live.ditto.presencedegradationreporter.model

import live.ditto.DittoDocument
import live.ditto.presencedegradationreporter.usecase.GetDateFromTimestampUseCase

data class Peer(
    val name: String,
    val transportInfo: PeerTransportInfo,
    val connected: Boolean,
    val lastSeen: Long,
    val peerKeyString: String,
) {
    val lastSeenFormatted = GetDateFromTimestampUseCase().invoke(lastSeen)
}

fun Peer.toMap() = mapOf(
    "_id" to name,
    "bluetoothConnections" to transportInfo.bluetoothConnections.toString(),
    "lanConnections" to transportInfo.lanConnections.toString(),
    "p2pConnections" to transportInfo.p2pConnections.toString(),
    "cloudConnections" to transportInfo.cloudConnections.toString(),
    "connected" to connected.toString(),
    "lastSeen" to lastSeen.toString(),
    "key" to key,
)

fun DittoDocument.toPeer() = Peer(
    name = this["_id"].stringValue,
    transportInfo = PeerTransportInfo(
        bluetoothConnections = this["bluetoothConnections"].stringValue.toInt(),
        lanConnections = this["lanConnections"].stringValue.toInt(),
        p2pConnections = this["p2pConnections"].stringValue.toInt(),
        cloudConnections = this["cloudConnections"].stringValue.toInt(),
    ),
    connected = this["connected"].stringValue.toBoolean(),
    lastSeen = this["lastSeen"].stringValue.toLong(),
    key = this["key"].stringValue,
)
