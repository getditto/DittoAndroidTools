package live.ditto.dittotoolsapp.utils

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import live.ditto.Ditto
import live.ditto.DittoPresenceGraph

/**
 * Observes the presence graph as a cold Flow.
 * The flow will emit the current graph upon collection and any subsequent changes.
 */
fun Ditto.presenceGraphAsFlow(): Flow<DittoPresenceGraph> = callbackFlow {
    val observer = this@presenceGraphAsFlow.presence.observe {
        trySend(it)
    }

    // When the flow is cancelled, stop the observer
    awaitClose {
        observer.close()
    }
}
