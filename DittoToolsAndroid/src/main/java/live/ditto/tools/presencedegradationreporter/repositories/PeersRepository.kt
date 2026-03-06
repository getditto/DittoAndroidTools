package live.ditto.tools.presencedegradationreporter.repositories

import com.ditto.kotlin.Ditto
import com.ditto.kotlin.DittoStoreObserver
import com.ditto.kotlin.serialization.toDittoCbor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import live.ditto.tools.presencedegradationreporter.model.Peer
import live.ditto.tools.presencedegradationreporter.model.Settings
import live.ditto.tools.presencedegradationreporter.model.toMap
import live.ditto.tools.presencedegradationreporter.model.toPeer
import live.ditto.tools.presencedegradationreporter.model.toPeerConnectedUpdate
import live.ditto.tools.presencedegradationreporter.model.toSettings

// Prefix "pdr" stands for Presence Degradation Reporter
private const val COLLECTION_SETTINGS = "pdr_settings"
private const val COLLECTION_LOCAL_PEER = "pdr_local_peer"
private const val COLLECTION_REMOTE_PEERS = "pdr_remote_peers"

class PeersRepository(
    private val ditto: Ditto,
    private val dispatcherIO: CoroutineDispatcher = Dispatchers.IO
) {
    private val _settingsFlow = MutableStateFlow(Settings())
    val settingsFlow: Flow<Settings> = _settingsFlow.asStateFlow()

    private val _localPeerFlow = MutableStateFlow<Peer?>(null)
    val localPeerFlow: Flow<Peer?> = _localPeerFlow.asStateFlow()

    private val _remotePeersFlow = MutableStateFlow<List<Peer>>(emptyList())
    val remotePeersFlow: Flow<List<Peer>> = _remotePeersFlow.asStateFlow()

    private val settingsObserver: DittoStoreObserver = ditto.store.registerObserver(
        "SELECT * FROM $COLLECTION_SETTINGS"
    ) { result ->
        _settingsFlow.value = result.items.firstOrNull()?.value?.toSettings() ?: Settings()
    }

    private val localPeerObserver: DittoStoreObserver = ditto.store.registerObserver(
        "SELECT * FROM $COLLECTION_LOCAL_PEER"
    ) { result ->
        _localPeerFlow.value = result.items.firstOrNull()?.value?.toPeer()
    }

    private val remotePeersObserver: DittoStoreObserver = ditto.store.registerObserver(
        "SELECT * FROM $COLLECTION_REMOTE_PEERS"
    ) { result ->
        _remotePeersFlow.value = result.items.map { it.value.toPeer() }
    }

    suspend fun upsertSettings(settings: Settings) = withContext(dispatcherIO) {
        ditto.store.execute(
            "INSERT INTO $COLLECTION_SETTINGS DOCUMENTS (:doc) ON ID CONFLICT DO UPDATE",
            mapOf("doc" to settings.toMap().toDittoCbor()).toDittoCbor()
        )
    }

    suspend fun upsertPeers(localPeer: Peer, remotePeers: List<Peer>) = withContext(dispatcherIO) {
        ditto.store.execute(
            "INSERT INTO $COLLECTION_LOCAL_PEER DOCUMENTS (:doc) ON ID CONFLICT DO UPDATE",
            mapOf("doc" to localPeer.toMap().toDittoCbor()).toDittoCbor()
        )

        // Mark all existing remote peers as disconnected, then upsert current peers
        val existingResult = ditto.store.execute("SELECT * FROM $COLLECTION_REMOTE_PEERS")
        for (item in existingResult.items) {
            val update = item.value.toPeerConnectedUpdate().copy(connected = false)
            ditto.store.execute(
                "INSERT INTO $COLLECTION_REMOTE_PEERS DOCUMENTS (:doc) ON ID CONFLICT DO UPDATE",
                mapOf("doc" to update.toMap().toDittoCbor()).toDittoCbor()
            )
        }

        for (peer in remotePeers) {
            ditto.store.execute(
                "INSERT INTO $COLLECTION_REMOTE_PEERS DOCUMENTS (:doc) ON ID CONFLICT DO UPDATE",
                mapOf("doc" to peer.toMap().toDittoCbor()).toDittoCbor()
            )
        }
    }

    suspend fun removeRemotePeers() = withContext(dispatcherIO) {
        ditto.store.execute("EVICT FROM $COLLECTION_REMOTE_PEERS WHERE true")
    }

    fun close() {
        settingsObserver.close()
        localPeerObserver.close()
        remotePeersObserver.close()
    }
}
