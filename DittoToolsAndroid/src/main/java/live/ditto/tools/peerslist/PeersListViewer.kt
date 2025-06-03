package live.ditto.tools.peerslist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import live.ditto.Ditto
import live.ditto.DittoPeer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeersListViewer(
    modifier: Modifier = Modifier,
    ditto: Ditto
) {
    val peerListViewScope = rememberCoroutineScope()
    val stateHolder = remember { PeerListViewStateHolder(ditto, peerListViewScope) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Surface(tonalElevation = 4.dp) {
                TopAppBar(
                    title = {
                        Text("Peers List")
                    },
                    actions = {
                        IconButton(onClick = {
                            stateHolder.togglePause()
                        }) {
                            if (stateHolder.isPaused) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null
                                )
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        PeerListView(stateHolder.localPeer, stateHolder.remotePeers, padding = innerPadding)
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
                    "LOCAL (SELF) PEER",
                    style = MaterialTheme.typography.labelLarge
                )
            }
            item {
                PeerView(localPeer)
            }

            item {
                Text(
                    "REMOTE PEERS",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            items(remotePeers) { peer ->
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
                peer.dittoSdkVersion?.let { Text("SDK v${it}") }
            }
        }
    }
}