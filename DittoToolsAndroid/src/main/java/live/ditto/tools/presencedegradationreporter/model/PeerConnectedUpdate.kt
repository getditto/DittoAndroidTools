package live.ditto.tools.presencedegradationreporter.model

import com.ditto.kotlin.serialization.DittoCborSerializable

data class PeerConnectedUpdate(
    val name: String,
    val connected: Boolean,
)

fun PeerConnectedUpdate.toMap() = mapOf(
    "_id" to name,
    "connected" to connected.toString(),
)

fun DittoCborSerializable.Dictionary.toPeerConnectedUpdate() = PeerConnectedUpdate(
    name = this["_id"].stringOrNull ?: "",
    connected = this["connected"].stringOrNull?.toBoolean() ?: false,
)
