package com.ditto.tools.presencedegradationreporter.usecase

import com.ditto.tools.presencedegradationreporter.model.Peer
import com.ditto.tools.presencedegradationreporter.model.Settings
import com.ditto.tools.presencedegradationreporter.repositories.PeersRepository

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