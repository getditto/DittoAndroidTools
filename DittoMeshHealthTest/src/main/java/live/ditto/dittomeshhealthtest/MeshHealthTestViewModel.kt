package live.ditto.dittomeshhealthtest

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import live.ditto.Ditto

class MeshHealthTestViewModel(
    ditto: Ditto
): ViewModel() {
        private var _state = MutableStateFlow(MeshHealthTestUIState())
        val state = _state.asStateFlow()

        init {
            observePresence(ditto)
        }

        fun observePresence(ditto: Ditto){
            ditto.presence.observe { graph ->
                _state.update { it.copy(remotePeers = graph.remotePeers) }
            }
        }

}
