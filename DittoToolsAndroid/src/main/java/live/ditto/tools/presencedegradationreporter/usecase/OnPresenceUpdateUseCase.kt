package live.ditto.tools.presencedegradationreporter.usecase

import live.ditto.tools.presencedegradationreporter.model.Peer
import live.ditto.tools.presencedegradationreporter.model.Settings
import live.ditto.tools.presencedegradationreporter.repositories.PeersRepository

class OnPresenceUpdateUseCase(
    private val repository: PeersRepository,
    private val updateHasSeenPeersUseCase: UpdateHasSeenPeersUseCase = UpdateHasSeenPeersUseCase(
        repository = repository
    )
) {
    suspend operator fun invoke(
        settings: Settings,
        localPeer: Peer,
        remotePeers: List<Peer>
    ) {
        repository.upsertPeers(
            localPeer = localPeer,
            remotePeers = remotePeers
        )

        updateHasSeenPeersUseCase(settings = settings)
    }
}