package live.ditto.tools.presencedegradationreporter

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import live.ditto.Ditto
import live.ditto.tools.presencedegradationreporter.model.Peer
import live.ditto.tools.presencedegradationreporter.model.Settings
import live.ditto.tools.presencedegradationreporter.repositories.PeersRepository

data class PresenceDegradationReporterApiState(
    val settings: Settings,
    val localPeer: Peer,
    val remotePeers: List<Peer>
)
fun Ditto.presenceDegradationReporterFlow(
    dispatcherIO: CoroutineDispatcher = Dispatchers.IO
): Flow<PresenceDegradationReporterApiState> {
    val repository = PeersRepository(
        ditto = this,
        dispatcherIO = dispatcherIO,
    )

    return combine(
        repository.settingsFlow,
        repository.localPeerFlow,
        repository.remotePeersFlow,
    ) { settings, localPeer, remotePeers ->
        if (localPeer == null) return@combine null

        PresenceDegradationReporterApiState(
            settings = settings,
            localPeer =  localPeer,
            remotePeers = remotePeers
        )
    }.filterNotNull()
}
