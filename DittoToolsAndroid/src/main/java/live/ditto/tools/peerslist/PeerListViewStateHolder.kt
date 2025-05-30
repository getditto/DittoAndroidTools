package live.ditto.tools.peerslist

import live.ditto.Ditto
import live.ditto.DittoPeer
import androidx.compose.runtime.*

class PeerListViewStateHolder(val ditto: Ditto) {
    var remotePeers by mutableStateOf(listOf<DittoPeer>())
        private set
    var localPeer: DittoPeer = ditto.presence.graph.localPeer
    var isPaused by mutableStateOf(false)
        private set

    init {
        ditto.presence.observe { graph ->
            if (isPaused) {
                return@observe
            }
            remotePeers = graph.remotePeers
            localPeer = graph.localPeer
        }
    }

    fun togglePause() {
        isPaused = !isPaused
    }
}