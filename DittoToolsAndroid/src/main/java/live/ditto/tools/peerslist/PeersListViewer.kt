package live.ditto.tools.peerslist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import live.ditto.Ditto
import live.ditto.DittoPeer
import live.ditto.tools.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeersListViewer(
    modifier: Modifier = Modifier,
    ditto: Ditto,
    viewModel: PeerListViewModel = PeerListViewModel(ditto)
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .background(color = MaterialTheme.colorScheme.primary),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.peers_list_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = modifier
                    .padding(16.dp),
                color = Color.White
            )

            IconButton(onClick = {
                viewModel.togglePause()
            }) {
                if (uiState.isPaused) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = modifier.size(24.dp),
                        tint = Color.White
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_pause_circle),
                        contentDescription = null,
                        modifier = modifier.size(24.dp),
                        tint = Color.White
                    )
                }
            }
        }

        PeerListView(uiState)
    }
}

@Composable
private fun PeerListView(
    state: PeerListUiState
) {
    Surface {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    stringResource(R.string.local_self_peer),
                    style = MaterialTheme.typography.labelLarge
                )
            }
            item {
                PeerView(state.localPeer)
            }

            item {
                Text(
                    stringResource(R.string.remote_peers),
                    style = MaterialTheme.typography.labelLarge
                )
            }

            items(state.remotePeers, key = { it.peerKeyString }) { peer ->
                // Remote peer
                PeerView(peer)
            }
        }
    }
}

@Composable
private fun PeerView(peer: DittoPeer) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            Column {
                Text("${peer.deviceName}:", style = MaterialTheme.typography.titleLarge)
                Text(peer.peerKeyString)
                peer.dittoSdkVersion?.let { Text(stringResource(R.string.sdk_version, it)) }
            }
        }
    }
}