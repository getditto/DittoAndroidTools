package live.ditto.dittomeshhealthtest

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import live.ditto.Ditto
import live.ditto.DittoPeer
import live.ditto.DittoStoreObserver
import live.ditto.DittoSyncSubscription
import java.util.Calendar
import kotlin.coroutines.coroutineContext

class MeshHealthTestViewModel(
    ditto: Ditto
) : ViewModel() {

    private var localStoreObserver: DittoStoreObserver? = null
    private var observerSubscription: DittoSyncSubscription? = null
    private val ditto = ditto

    val VENUE_ID = "Zans house"
    val SCHEMA_VERSION = 1

    private var _state = MutableStateFlow(MeshHealthTestUIState())
    val state = _state.asStateFlow()
    var testState: TestState = TestState.OBSERVING

    init {
        observePresence(ditto)
        subscribeAsObserver(ditto)
    }

    fun onStartHealthCheck() {

        testState = TestState.TEST_RUNNING

        val myPeerKey = ditto.presence.graph.localPeer.peerKeyString
        val nowTimestamp = Calendar.getInstance().timeInMillis
        val testUUID = java.util.UUID.randomUUID().toString()

        val allPeers = ditto.presence.graph.remotePeers
        allPeers.forEach { peer ->

            viewModelScope.launch {
                // Create document
                val newHealthCheckForPeer = mapOf(
                    "newDocument" to mapOf(
                        "testId" to testUUID,
                        "venueId" to VENUE_ID,
                        "schemaVersion" to SCHEMA_VERSION,
                        "originatorPeerKey" to myPeerKey,
                        "originatingTimestamp" to nowTimestamp,
                        "targetAck" to false,
                        "targetPeerKey" to peer.peerKeyString
                    )
                )

                ditto.store.execute(
                    """
                    INSERT INTO dittoTools_meshHealthTest
                    DOCUMENTS (:newDocument)
                """, newHealthCheckForPeer
                )
            }
        }
    }

    private fun observePresence(ditto: Ditto) {
        ditto.presence.observe { graph ->
            _state.update { it.copy(remotePeers = graph.remotePeers) }
        }
    }

    private fun subscribeAsObserver(ditto: Ditto) {

        val myPeerKey = ditto.presence.graph.localPeer.peerKeyString
        val venueId = "Zans house"
        val schemaVersion = 1
        val lastMidnight = getLastMidnightTimestamp()

        observerSubscription = ditto.sync.registerSubscription(
            """
                    SELECT * 
                    FROM dittoTools_meshHealthTest
                    WHERE venueId = '${venueId}' AND schemaVersion = '${schemaVersion}' AND timestamp > $lastMidnight  
                """.trimIndent()
        )

        localStoreObserver = ditto.store.registerObserver(
            """
                    SELECT * 
                    FROM dittoTools_meshHealthTest
                    WHERE venueId = '${venueId}' AND schemaVersion = '${schemaVersion}' AND timestamp > $lastMidnight
                """.trimIndent(),
            changeHandler = { result ->
                println(">>>>>>> ITEMS " + result.items.size)
                result.items.forEach {
                    println(it.value.toString())
                }
            }

            //AND targetPeerKey = '${myPeerKey}' AND NOT targetAck = true

        )

    }

    private fun getLastMidnightTimestamp(): Long {

        // Get instance of calendar
        val calendar = Calendar.getInstance()

        // Set the time to the start of today
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        // Return the timestamp in milliseconds
        return calendar.timeInMillis
    }

    override fun onCleared() {
        super.onCleared()

        localStoreObserver?.close()
        observerSubscription?.close()
    }

}
