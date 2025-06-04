package live.ditto.tools.peerslist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Surface(tonalElevation = 4.dp) {
                TopAppBar(
                    title = {
                        Text(stringResource(R.string.peers_list_title))
                    },
                    actions = {
                        IconButton(onClick = {
                            viewModel.togglePause()
                        }) {
                            if (uiState.isPaused) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_pause_circle),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        PeerListView(uiState.localPeer, uiState.remotePeers, padding = innerPadding)
    }
}

@Composable
private fun PeerListView(
    localPeer: DittoPeer,
    remotePeers: List<DittoPeer>,
    padding: PaddingValues
) {
    Surface {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    stringResource(R.string.local_self_peer),
                    style = MaterialTheme.typography.labelLarge
                )
            }
            item {
                PeerView(localPeer)
            }

            item {
                Text(
                    stringResource(R.string.remote_peers),
                    style = MaterialTheme.typography.labelLarge
                )
            }

            items(remotePeers, key = { it.peerKeyString }) { peer ->
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