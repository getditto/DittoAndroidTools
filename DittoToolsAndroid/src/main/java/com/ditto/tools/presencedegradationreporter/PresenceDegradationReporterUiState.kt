package com.ditto.tools.presencedegradationreporter

import com.ditto.tools.presencedegradationreporter.model.Peer
import com.ditto.tools.presencedegradationreporter.model.Settings


data class PresenceDegradationReporterUiState(
    val isLoading: Boolean = true,

    val shouldRenderPeersForm: Boolean = false,

    val settings: Settings = Settings(),
    val localPeer: Peer? = null,
    val remotePeers: List<Peer> = emptyList(),
)