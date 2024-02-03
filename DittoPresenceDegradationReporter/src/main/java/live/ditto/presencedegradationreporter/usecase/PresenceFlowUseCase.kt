package live.ditto.presencedegradationreporter.usecase

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import live.ditto.Ditto
import live.ditto.DittoConnectionType
import live.ditto.DittoPeer
import live.ditto.presencedegradationreporter.model.Peer
import live.ditto.presencedegradationreporter.model.PeerTransportInfo


class PresenceFlowUseCase(
    private val ditto: Ditto
) {
    operator fun invoke(bufferCapacity: Int = Channel.UNLIMITED) = callbackFlow {
        val observer = ditto.presence.observe { graph ->
            val seenAt = System.currentTimeMillis()
            val localPeerTransportInfo = resolveTransportInfo(graph.localPeer)

            val localPeer = Peer(
                name = graph.localPeer.deviceName,
                transportInfo = localPeerTransportInfo,
                connected = true,
                lastSeen = seenAt
            )

            val remotePeers = graph.remotePeers.map {
                val peerTransportInfo = resolveTransportInfo(it)

                Peer(
                    name = it.deviceName,
                    transportInfo = peerTransportInfo,
                    connected = true,
                    lastSeen = seenAt
                )
            }

            trySendBlocking(Pair(localPeer, remotePeers))
        }

        awaitClose { observer.close() }
    }.buffer(bufferCapacity)

    private fun resolveTransportInfo(peer: DittoPeer): PeerTransportInfo {
        val lanSet = setOf(
            DittoConnectionType.AccessPoint,
            DittoConnectionType.WebSocket
        )
        val connections = peer.connections.map { it.connectionType }
        val bluetoothConnections = connections.count { it == DittoConnectionType.Bluetooth }
        val lanConnections = connections.count { it in lanSet }
        val p2pConnections = connections.count { it == DittoConnectionType.P2PWiFi }
        val cloudConnections = if (peer.isConnectedToDittoCloud) 1 else 0

        return PeerTransportInfo(
            bluetoothConnections = bluetoothConnections,
            lanConnections = lanConnections,
            p2pConnections = p2pConnections,
            cloudConnections = cloudConnections
        )
    }
}