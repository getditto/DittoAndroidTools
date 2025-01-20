package live.ditto.tools.presencedegradationreporter.repositories

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import live.ditto.Ditto
import live.ditto.DittoDocument
import live.ditto.tools.presencedegradationreporter.model.Peer
import live.ditto.tools.presencedegradationreporter.model.Settings
import live.ditto.tools.presencedegradationreporter.model.toMap
import live.ditto.tools.presencedegradationreporter.model.toPeer
import live.ditto.tools.presencedegradationreporter.model.toPeerConnectedUpdate
import live.ditto.tools.presencedegradationreporter.model.toSettings
import observeLocalAsFlow

// Prefix "pdr" stands for Presence Degradation Reporter
private const val COLLECTION_SETTINGS = "pdr_settings"
private const val COLLECTION_LOCAL_PEER = "pdr_local_peer"
private const val COLLECTION_REMOTE_PEERS = "pdr_remote_peers"

class PeersRepository(
    private val ditto: Ditto,
    private val dispatcherIO: CoroutineDispatcher = Dispatchers.IO
) {
    val settingsFlow = createFlowFor(COLLECTION_SETTINGS) {
        it.toSettings()
    }.map { it.firstOrNull() ?: Settings() }

    val localPeerFlow = createFlowFor(COLLECTION_LOCAL_PEER) {
        it.toPeer()
    }.map { it.firstOrNull() }

    val remotePeersFlow = createFlowFor(COLLECTION_REMOTE_PEERS) { it.toPeer() }

    suspend fun upsertSettings(settings: Settings) = withContext(dispatcherIO) {
        ditto.store[COLLECTION_SETTINGS].upsert(value = settings.toMap())
    }

    suspend fun upsertPeers(localPeer: Peer, remotePeers: List<Peer>) = withContext(dispatcherIO) {
        ditto.store.write { transaction ->
            val peerCollection = transaction.scoped(COLLECTION_LOCAL_PEER)
            peerCollection.upsert(
                value = localPeer.toMap()
            )

            val remotePeersCollection = transaction.scoped(COLLECTION_REMOTE_PEERS)
            remotePeersCollection.findAll().exec().forEach {
                val peerConnectedUpdate = it.toPeerConnectedUpdate()
                remotePeersCollection.upsert(value = peerConnectedUpdate.copy(connected = false).toMap())
            }

            remotePeers.forEach { peer ->
                remotePeersCollection.upsert(value = peer.toMap())
            }
        }
    }

    private fun <T> createFlowFor(
        collectionName: String,
        mapper: (DittoDocument) -> T
    ) = ditto.store[collectionName]
        .findAll()
        .observeLocalAsFlow()
        .map { pair ->
            pair.first.map { dittoDocument -> mapper(dittoDocument) }
        }

    suspend fun removeRemotePeers() = withContext(dispatcherIO) {
        ditto.store[COLLECTION_REMOTE_PEERS].findAll().remove()
    }
}