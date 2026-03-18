package live.ditto.tools.presencedegradationreporter.usecase

import com.ditto.kotlin.Ditto
import com.ditto.kotlin.DittoConnectionType
import com.ditto.kotlin.DittoPeer
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.map
import live.ditto.tools.presencedegradationreporter.model.Peer
import live.ditto.tools.presencedegradationreporter.model.PeerTransportInfo

class PresenceFlowUseCase(
    private val ditto: Ditto
) {
    operator fun invoke(bufferCapacity: Int = Channel.UNLIMITED) = ditto.presence.observe()
        .map { graph ->
            val seenAt = System.currentTimeMillis()
            val localPeerTransportInfo = resolveTransportInfo(graph.localPeer)

            val localPeer = Peer(
                name = graph.localPeer.deviceName,
                transportInfo = localPeerTransportInfo,
                connected = true,
                lastSeen = seenAt,
                peerKeyString = graph.localPeer.peerKey,
            )

            val remotePeers = graph.remotePeers.map {
                val peerTransportInfo = resolveTransportInfo(it)

                Peer(
                    name = it.deviceName,
                    transportInfo = peerTransportInfo,
                    connected = true,
                    lastSeen = seenAt,
                    peerKeyString = it.peerKey,
                )
            }

            Pair(localPeer, remotePeers)
        }
        .buffer(bufferCapacity)

    private fun resolveTransportInfo(peer: DittoPeer): PeerTransportInfo {
        val lanTypes = setOf(
            DittoConnectionType.AccessPoint,
            DittoConnectionType.WebSocket
        )
        val connections = peer.connections.map { it.connectionType }
        val bluetoothConnections = connections.count { it == DittoConnectionType.Bluetooth }
        val lanConnections = connections.count { it in lanTypes }
        val p2pConnections = connections.count { it == DittoConnectionType.P2PWiFi }
        val cloudConnections = if (peer.isConnectedToDittoServer) 1 else 0

        return PeerTransportInfo(
            bluetoothConnections = bluetoothConnections,
            lanConnections = lanConnections,
            p2pConnections = p2pConnections,
            cloudConnections = cloudConnections
        )
    }
}
