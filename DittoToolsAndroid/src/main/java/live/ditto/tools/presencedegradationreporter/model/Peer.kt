package live.ditto.tools.presencedegradationreporter.model

import com.ditto.kotlin.serialization.DittoCborSerializable
import live.ditto.tools.presencedegradationreporter.usecase.GetDateFromTimestampUseCase

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
    "peerKeyString" to peerKeyString,
)

fun DittoCborSerializable.Dictionary.toPeer() = Peer(
    name = this["_id"].stringOrNull ?: "",
    transportInfo = PeerTransportInfo(
        bluetoothConnections = this["bluetoothConnections"].stringOrNull?.toIntOrNull() ?: 0,
        lanConnections = this["lanConnections"].stringOrNull?.toIntOrNull() ?: 0,
        p2pConnections = this["p2pConnections"].stringOrNull?.toIntOrNull() ?: 0,
        cloudConnections = this["cloudConnections"].stringOrNull?.toIntOrNull() ?: 0,
    ),
    connected = this["connected"].stringOrNull?.toBoolean() ?: false,
    lastSeen = this["lastSeen"].stringOrNull?.toLongOrNull() ?: 0L,
    peerKeyString = this["peerKeyString"].stringOrNull ?: "",
)
