package live.ditto.presencedegradationreporter.model

data class PeerTransportInfo(
    val bluetoothConnections: Int,
    val lanConnections: Int,
    val p2pConnections: Int,
    val cloudConnections: Int,
)