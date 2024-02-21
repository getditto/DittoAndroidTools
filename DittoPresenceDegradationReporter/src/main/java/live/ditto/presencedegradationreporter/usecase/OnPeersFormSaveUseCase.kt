package live.ditto.presencedegradationreporter.usecase

import live.ditto.presencedegradationreporter.model.Settings
import live.ditto.presencedegradationreporter.repositories.PeersRepository

class OnPeersFormSaveUseCase (
    private val repository: PeersRepository
) {
    suspend operator fun invoke(
        settings: Settings,
        expectedPeers: Int,
        reportApiEnabled: Boolean
    ) {
        repository.upsertSettings(
            settings.copy(
                expectedPeers = expectedPeers,
                reportApiEnabled = reportApiEnabled,
                sessionStartedAt = System.currentTimeMillis(),
                hasSeenExpectedPeers = false,
            )
        )

        repository.removeRemotePeers()
    }
}