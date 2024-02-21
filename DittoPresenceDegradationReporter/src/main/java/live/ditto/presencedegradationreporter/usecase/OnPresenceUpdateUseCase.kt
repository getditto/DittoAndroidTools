package live.ditto.presencedegradationreporter.usecase

import live.ditto.presencedegradationreporter.model.Peer
import live.ditto.presencedegradationreporter.model.Settings
import live.ditto.presencedegradationreporter.repositories.PeersRepository

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