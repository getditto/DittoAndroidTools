package live.ditto.tools.presencedegradationreporter.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import live.ditto.tools.R
import live.ditto.tools.presencedegradationreporter.model.Peer
import live.ditto.tools.presencedegradationreporter.model.PeerTransportInfo
import live.ditto.tools.presencedegradationreporter.theme.PresenceDegradationReporterTheme
import live.ditto.tools.presencedegradationreporter.theme.PresenceDegradationReporterTypography
import live.ditto.tools.presencedegradationreporter.theme.screenBackground
import live.ditto.tools.presencedegradationreporter.theme.dashboardError
import live.ditto.tools.presencedegradationreporter.theme.dashboardSuccess

@Composable
fun Dashboard(
    hasSeenExpectedPeers: Boolean,
    expectedPeers: Int,
    reportApiEnabled: Boolean,
    sessionStartedAt: String,
    onChangeClick: () -> Unit,
    localPeer: Peer?,
    remotePeers: List<Peer>,
) {
    val connected = remember(remotePeers) { remotePeers.count { it.connected } }
    val backgroundColor = remember(key1 = remotePeers, key2 = hasSeenExpectedPeers) {
        if (!hasSeenExpectedPeers) return@remember screenBackground
        if (connected < expectedPeers) dashboardError else dashboardSuccess
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = backgroundColor)
            .padding(16.dp)
    ) {
        Header(
            expectedPeers = expectedPeers,
            reportApiEnabled = reportApiEnabled,
            sessionStartedAt = sessionStartedAt,
            onChangeClick = onChangeClick,
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        if (localPeer != null) {
            Text(
                text = stringResource(R.string.local_device),
                style = PresenceDegradationReporterTypography.titleLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            PeerItem(peer = localPeer)
        }

        if (remotePeers.isNotEmpty()) {
            Text(
                text = stringResource(R.string.remote_devices, connected, remotePeers.size),
                style = PresenceDegradationReporterTypography.titleLarge,
                modifier = Modifier.padding(top = 8.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(items = remotePeers) { peer ->
                    PeerItem(peer = peer)
                }
            }
        }
    }
}

@Composable
private fun Header(
    expectedPeers: Int,
    reportApiEnabled: Boolean,
    sessionStartedAt: String,
    onChangeClick: () -> Unit
) {
    val reportApiEnabledValue = if (reportApiEnabled) {
        stringResource(R.string.enabled)
    } else {
        stringResource(R.string.disabled)
    }

    Column {
        Column {
            HeaderField(
                message = stringResource(R.string.expected_minimum_peers),
                value = expectedPeers.toString()
            )
            Spacer(modifier = Modifier.height(2.dp))
            HeaderField(
                message = stringResource(R.string.report_api),
                value = reportApiEnabledValue
            )
        }

        HeaderField(
            message = stringResource(R.string.session_started_at),
            value = sessionStartedAt
        )

        OutlinedButton(
            onClick = onChangeClick,
            shape = RoundedCornerShape(10),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color.Black
            )
        ) {
            Text(text = stringResource(R.string.new_session))
        }
    }
}

@Composable
private fun HeaderField(
    message: String,
    value: String
) {
    Row {
        Text(
            text = message,
            style = PresenceDegradationReporterTypography.bodyLarge
        )

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = value,
            style = PresenceDegradationReporterTypography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview
@Composable
private fun DashboardPreview() {
    PresenceDegradationReporterTheme {
        Dashboard(
            hasSeenExpectedPeers = true,
            expectedPeers = 1,
            reportApiEnabled = true,
            sessionStartedAt = "01.02.2020 12:00",
            onChangeClick = {},
            localPeer = Peer(
                name = "A",
                transportInfo = PeerTransportInfo(
                    bluetoothConnections = 1,
                    lanConnections = 2,
                    p2pConnections = 2,
                    cloudConnections = 1
                ),
                connected = true,
                lastSeen = 0L,
                key = "Key123",
            ),
            remotePeers = listOf(
                Peer(
                    name = "A",
                    transportInfo = PeerTransportInfo(
                        bluetoothConnections = 1,
                        lanConnections = 2,
                        p2pConnections = 2,
                        cloudConnections = 1
                    ),
                    connected = false,
                    lastSeen = 0L,
                    key = "Key123",
                ),
                Peer(
                    name = "B",
                    transportInfo = PeerTransportInfo(
                        bluetoothConnections = 1,
                        lanConnections = 2,
                        p2pConnections = 2,
                        cloudConnections = 1
                    ),
                    connected = true,
                    lastSeen = 0L,
                    key = "Key123",
                )
            )
        )
    }
}