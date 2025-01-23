package live.ditto.tools.presencedegradationreporter.model

import live.ditto.DittoDocument

data class PeerConnectedUpdate(
    val name: String,
    val connected: Boolean,
)

fun PeerConnectedUpdate.toMap() = mapOf(
    "_id" to name,
    "connected" to connected.toString(),
)

fun DittoDocument.toPeerConnectedUpdate() = PeerConnectedUpdate(
    name = this["_id"].stringValue,
    connected = this["connected"].stringValue.toBoolean(),
)