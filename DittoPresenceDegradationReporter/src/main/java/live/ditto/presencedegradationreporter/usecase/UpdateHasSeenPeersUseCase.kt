package live.ditto.presencedegradationreporter.usecase

import kotlinx.coroutines.flow.firstOrNull
import live.ditto.presencedegradationreporter.model.Settings
import live.ditto.presencedegradationreporter.repositories.PeersRepository

class UpdateHasSeenPeersUseCase(
    private val repository: PeersRepository
) {
    suspend operator fun invoke(
        settings: Settings,
    ) {
        if (settings.expectedPeers == 0) return
        if (settings.hasSeenExpectedPeers) return

        val peers = repository.remotePeersFlow.firstOrNull()?.size ?: 0
        if (peers < settings.expectedPeers) return

        repository.upsertSettings(
            settings = settings.copy(hasSeenExpectedPeers = true)
        )
    }
}