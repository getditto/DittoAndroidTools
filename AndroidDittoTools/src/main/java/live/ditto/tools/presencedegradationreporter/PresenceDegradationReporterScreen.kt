package live.ditto.tools.presencedegradationreporter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import live.ditto.Ditto
import live.ditto.tools.presencedegradationreporter.components.Dashboard
import live.ditto.tools.presencedegradationreporter.components.Loading
import live.ditto.tools.presencedegradationreporter.components.PeersForm
import live.ditto.tools.presencedegradationreporter.theme.PresenceDegradationReporterTheme

@Composable
fun PresenceDegradationReporterScreen(
    ditto: Ditto,
    viewModel: PresenceDegradationReporterViewModel = PresenceDegradationReporterViewModel(
        ditto = ditto,
    ),
) {
    val state by viewModel.state.collectAsState()

    PresenceDegradationReporterTheme {
        PresenceDegradationReporterScreen(
            state = state,
            onSave = { expectedPeers, reportApiEnabled ->
                viewModel.onPeerFormSave(expectedPeers, reportApiEnabled)
            },
            onDashboardChangeClick = {
                viewModel.onDashboardChangeClick()
            }
        )
    }
}

@Composable
private fun PresenceDegradationReporterScreen(
    state: PresenceDegradationReporterUiState,
    onSave: (expectedPeers: Int, reportApiEnabled: Boolean) -> Unit,
    onDashboardChangeClick: () -> Unit,
) {
    if (state.isLoading) return Loading()

    if (state.shouldRenderPeersForm) return PeersForm(
        expectedPeers = state.settings.expectedPeers,
        reportApiEnabled = state.settings.reportApiEnabled,
        onSave = onSave
    )

    Dashboard(
        hasSeenExpectedPeers = state.settings.hasSeenExpectedPeers,
        expectedPeers = state.settings.expectedPeers,
        onChangeClick = onDashboardChangeClick,
        localPeer = state.localPeer,
        remotePeers = state.remotePeers,
        reportApiEnabled = state.settings.reportApiEnabled,
        sessionStartedAt = state.settings.sessionStartedAtFormatted,
    )
}

@Preview
@Composable
private fun ScreenPortraitPreview() {
    PresenceDegradationReporterTheme {
        PresenceDegradationReporterScreen(
            state = PresenceDegradationReporterUiState(
                isLoading = false,
            ),
            onSave = { a, b -> },
            onDashboardChangeClick = {}
        )
    }
}
