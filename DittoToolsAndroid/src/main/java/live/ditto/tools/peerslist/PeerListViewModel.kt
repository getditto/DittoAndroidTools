package live.ditto.tools.peerslist

import live.ditto.Ditto
import live.ditto.DittoPeer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import live.ditto.DittoPresenceGraph

data class PeerListUiState (
    val remotePeers: List<DittoPeer> = emptyList(),
    val localPeer: DittoPeer,
    val isPaused: Boolean = false
)

class PeerListViewModel(val ditto: Ditto): ViewModel() {
    private var _uiState = MutableStateFlow(
        PeerListUiState(
            remotePeers = ditto.presence.graph.remotePeers,
            localPeer = ditto.presence.graph.localPeer
        )
    )

    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            ditto.presenceAsFlow().collect { graph ->
                if (!_uiState.value.isPaused) {
                    _uiState.update {
                        it.copy(
                            remotePeers = graph.remotePeers,
                            localPeer = graph.localPeer
                        )
                    }
                }
            }
        }
    }

    fun togglePause() {
        _uiState.update {
            it.copy(isPaused = !it.isPaused)
        }
    }

    /// We are wrapping the observer as a flow which we will scope to the composable that holds this StateHolder
    private fun Ditto.presenceAsFlow(): Flow<DittoPresenceGraph> = callbackFlow {
        val observer = this@presenceAsFlow.presence.observe { graph ->
            trySend(graph).isSuccess
        }
        awaitClose { observer.close() }
    }
}