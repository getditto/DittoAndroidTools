package live.ditto.tools.presenceviewer

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import live.ditto.Ditto
import live.ditto.DittoPresenceGraph
import live.ditto.Presence


class PresenceViewModel(
    ditto: Ditto,
    coroutineScope: CoroutineScope
) : ViewModel() {

    private var _graphJson: Flow<String> = ditto.presence.observeAsFlow().map { it.json() }
    val graphJson = _graphJson.stateIn(
        coroutineScope,
        SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000L),
        null
    )

}

private fun Presence.observeAsFlow(): Flow<DittoPresenceGraph> = callbackFlow {
    val dittoPresenceObserver = this@observeAsFlow.observe { graph ->
        trySend(graph)
    }
    awaitClose {
        dittoPresenceObserver.close()
    }
}