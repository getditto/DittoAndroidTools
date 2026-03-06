package live.ditto.tools.peerslist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ditto.kotlin.Ditto
import com.ditto.kotlin.DittoPeer
import com.ditto.kotlin.DittoPresenceGraph
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PeerListUiState(
    val remotePeers: List<DittoPeer> = emptyList(),
    val localPeer: DittoPeer,
    val isPaused: Boolean = false
)

class PeerListViewModel(val ditto: Ditto) : ViewModel() {
    private var _uiState = MutableStateFlow(
        PeerListUiState(
            remotePeers = ditto.presence.graph.remotePeers,
            localPeer = ditto.presence.graph.localPeer
        )
    )

    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            ditto.presence.observe().collect { graph: DittoPresenceGraph ->
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
}
