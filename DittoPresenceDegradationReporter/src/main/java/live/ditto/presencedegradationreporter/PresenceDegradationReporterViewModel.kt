package live.ditto.presencedegradationreporter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import live.ditto.Ditto
import live.ditto.presencedegradationreporter.model.Peer
import live.ditto.presencedegradationreporter.model.Settings
import live.ditto.presencedegradationreporter.repositories.PeersRepository
import live.ditto.presencedegradationreporter.usecase.OnPeersFormSaveUseCase
import live.ditto.presencedegradationreporter.usecase.OnPresenceUpdateUseCase
import live.ditto.presencedegradationreporter.usecase.PresenceFlowUseCase

class PresenceDegradationReporterViewModel(
    ditto: Ditto,
    presenceFlowUseCase: PresenceFlowUseCase = PresenceFlowUseCase(ditto = ditto),
    private val repository: PeersRepository = PeersRepository(ditto = ditto),
    private val onPresenceUpdateUseCase: OnPresenceUpdateUseCase = OnPresenceUpdateUseCase(
        repository = repository
    ),
    private val onPeersFormSaveUseCase: OnPeersFormSaveUseCase = OnPeersFormSaveUseCase(
        repository = repository
    ),
) : ViewModel() {
    private var _state = MutableStateFlow(PresenceDegradationReporterUiState(isLoading = true))
    val state = _state.asStateFlow()

    init {
        repository.settingsFlow
            .onEach(::onSettingsUpdate)
            .flowOn(Dispatchers.Default)
            .launchIn(viewModelScope)

        repository.localPeerFlow
            .filterNotNull()
            .onEach(::onLocalPeerUpdate)
            .flowOn(Dispatchers.Default)
            .launchIn(viewModelScope)

        repository.remotePeersFlow
            .onEach(::onRemotePeersUpdate)
            .flowOn(Dispatchers.Default)
            .launchIn(viewModelScope)

        presenceFlowUseCase()
            .onEach(::onPresenceUpdate)
            .flowOn(Dispatchers.Default)
            .launchIn(viewModelScope)
    }

    fun onPeerFormSave(expectedPeers: Int, reportApiEnabled: Boolean) {
        viewModelScope.launch {
            onPeersFormSaveUseCase(
                settings = state.value.settings,
                expectedPeers = expectedPeers,
                reportApiEnabled = reportApiEnabled
            )
        }
    }

    fun onDashboardChangeClick() {
        _state.update {
            it.copy(
                shouldRenderPeersForm = true,
            )
        }
    }

    private fun onSettingsUpdate(
        settings: Settings,
    ) {
        val shouldRenderPeersForm = settings.expectedPeers == 0

        _state.update {
            it.copy(
                settings = settings,
                isLoading = false,
                shouldRenderPeersForm = shouldRenderPeersForm,
            )
        }
    }

    private fun onLocalPeerUpdate(peer: Peer) {
        _state.update {
            it.copy(localPeer = peer)
        }
    }

    private fun onRemotePeersUpdate(peers: List<Peer>) {
        _state.update {
            it.copy(remotePeers = peers)
        }
    }

    private fun onPresenceUpdate(pair: Pair<Peer, List<Peer>>) {
        val (localPeer, remotePeers) = pair

        viewModelScope.launch {
            onPresenceUpdateUseCase(
                localPeer = localPeer,
                remotePeers = remotePeers,
                settings = state.value.settings,
            )
        }
    }
}
