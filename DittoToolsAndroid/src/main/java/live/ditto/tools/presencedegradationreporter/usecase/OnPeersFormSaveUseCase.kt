package live.ditto.tools.presencedegradationreporter.usecase

import live.ditto.tools.presencedegradationreporter.model.Settings
import live.ditto.tools.presencedegradationreporter.repositories.PeersRepository

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