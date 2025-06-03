package live.ditto.tools.peerslist

import live.ditto.Ditto
import live.ditto.DittoPeer
import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import live.ditto.DittoPresenceGraph

class PeerListViewStateHolder(val ditto: Ditto, scope: CoroutineScope) {
    var remotePeers by mutableStateOf(listOf<DittoPeer>())
        private set
    var localPeer: DittoPeer = ditto.presence.graph.localPeer
    var isPaused by mutableStateOf(false)
        private set

    init {
        scope.launch {
            ditto.presenceAsFlow().collect {
                if (!isPaused) {
                    remotePeers = it.remotePeers
                    localPeer = it.localPeer
                }
            }
        }
    }

    fun togglePause() {
        isPaused = !isPaused
    }

    /// We are wrapping the observer as a flow which we will scope to the composable that holds this StateHolder
    private fun Ditto.presenceAsFlow(): Flow<DittoPresenceGraph> = callbackFlow {
        val observer = this@presenceAsFlow.presence.observe { graph ->
            trySend(graph).isSuccess
        }
        awaitClose { observer.close() }
    }
}