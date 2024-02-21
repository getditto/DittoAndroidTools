import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import live.ditto.DittoDocument
import live.ditto.DittoLiveQuery
import live.ditto.DittoLiveQueryEvent
import live.ditto.DittoPendingCursorOperation

/**
 * Builds a flow that emits changes from a managed [DittoLiveQuery].
 * @param bufferCapacity Buffer capacity
 */
fun DittoPendingCursorOperation.observeLocalAsFlow(
    bufferCapacity: Int = Channel.UNLIMITED
): Flow<Pair<List<DittoDocument>, DittoLiveQueryEvent>> = callbackFlow {
    val liveQuery = observeLocal { documents, event -> trySendBlocking(Pair(documents, event)) }
    awaitClose { liveQuery.close() }
}.buffer(bufferCapacity)