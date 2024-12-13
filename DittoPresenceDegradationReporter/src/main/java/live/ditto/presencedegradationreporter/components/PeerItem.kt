package live.ditto.presencedegradationreporter.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import live.ditto.presencedegradationreporter.R
import live.ditto.presencedegradationreporter.model.Peer
import live.ditto.presencedegradationreporter.model.PeerTransportInfo
import live.ditto.presencedegradationreporter.theme.PresenceDegradationReporterTheme
import live.ditto.presencedegradationreporter.theme.dashboardCardConnected
import live.ditto.presencedegradationreporter.theme.dashboardCardNotConnected


@Composable
fun PeerItem(peer: Peer) {
    val containerColor = if (peer.connected) dashboardCardConnected else dashboardCardNotConnected

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = peer.name,
                fontWeight = FontWeight.Bold,
            )

            Text(text = stringResource(R.string.key, peer.peerKeyString))

            Text(text = stringResource(R.string.last_seen, peer.lastSeenFormatted))

            Divider(modifier = Modifier.padding(vertical = 4.dp))
            Transports(
                transportInfo = peer.transportInfo,
                isConnected = peer.connected
            )
        }
    }
}

@Composable
private fun Transports(
    transportInfo: PeerTransportInfo,
    isConnected: Boolean,
) {
    val placeHolder = stringResource(R.string.connection_placeholder)
    val bt = if (isConnected) transportInfo.bluetoothConnections else placeHolder
    val lan = if (isConnected) transportInfo.lanConnections else placeHolder
    val p2p = if (isConnected) transportInfo.p2pConnections else placeHolder
    val cloud = if (isConnected) transportInfo.cloudConnections else placeHolder

    Row {
        Text(text = stringResource(R.string.bt, bt))
        Spacer(modifier = Modifier.weight(1f))
        Text(text = stringResource(R.string.lan, lan))
        Spacer(modifier = Modifier.weight(1f))
        Text(text = stringResource(R.string.p2p, p2p))
        Spacer(modifier = Modifier.weight(1f))
        Text(text = stringResource(R.string.cloud, cloud))
    }
}

@Preview
@Composable
private fun PeerItemConnectedPreview() {
    PresenceDegradationReporterTheme {
        PeerItem(
            peer = Peer(
                name = "Device",
                transportInfo = PeerTransportInfo(
                    bluetoothConnections = 1,
                    lanConnections = 2,
                    p2pConnections = 2,
                    cloudConnections = 1
                ),
                connected = true,
                lastSeen = 0L,
                peerKeyString = "Key123"
            )
        )
    }
}

@Preview
@Composable
private fun PeerItemNotConnectedPreview() {
    PresenceDegradationReporterTheme {
        PeerItem(
            peer = Peer(
                name = "Device",
                transportInfo = PeerTransportInfo(
                    bluetoothConnections = 1,
                    lanConnections = 2,
                    p2pConnections = 2,
                    cloudConnections = 1
                ),
                connected = false,
                lastSeen = 0L,
                key = "Key123"
            )
        )
    }
}