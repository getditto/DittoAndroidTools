package live.ditto.presencedegradationreporter

import live.ditto.presencedegradationreporter.model.Peer
import live.ditto.presencedegradationreporter.model.Settings


data class PresenceDegradationReporterUiState(
    val isLoading: Boolean = true,

    val shouldRenderPeersForm: Boolean = false,

    val settings: Settings = Settings(),
    val localPeer: Peer? = null,
    val remotePeers: List<Peer> = emptyList(),
)