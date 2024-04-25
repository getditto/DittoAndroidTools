package ditto.live.dittopresenceviewer

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import live.ditto.Ditto
import live.ditto.DittoPresenceGraph
import live.ditto.Presence

class PresenceViewModel(
    ditto: Ditto
): ViewModel() {

    private var _graphJson: Flow<String> = ditto.presence.observeAsFlow().map { it.json() }
    val graphJson = _graphJson

}

private fun Presence.observeAsFlow(): Flow<DittoPresenceGraph> = callbackFlow {
    this@observeAsFlow.observe { graph ->
        trySend(graph)
    }
    awaitClose()
}
