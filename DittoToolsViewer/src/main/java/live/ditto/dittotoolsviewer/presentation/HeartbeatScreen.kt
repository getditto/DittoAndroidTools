package live.ditto.dittotoolsviewer.presentation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import live.ditto.Ditto
import live.ditto.dittodiskusage.DiskUsageViewModel
import live.ditto.healthmetrics.HealthMetricProvider
import live.ditto.dittoheartbeat.DittoHeartbeatConfig
import live.ditto.dittoheartbeat.DittoHeartbeatInfo
import live.ditto.dittoheartbeat.startHeartbeat
import live.ditto.dittotoolsviewer.R
import java.util.*


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HeartbeatScreen(ditto: Ditto) {

    var heartbeatInfo by remember { mutableStateOf<DittoHeartbeatInfo?>(null) }
    val healthMetricProviders: MutableList<HealthMetricProvider> = mutableListOf()
    val diskUsageViewModel = DiskUsageViewModel()
    healthMetricProviders.add(diskUsageViewModel)

    val config = DittoHeartbeatConfig(
        //id for testing only. Unique id will not persist
        id = UUID.randomUUID().toString(),
        secondsInterval = 30,
        healthMetricProviders = healthMetricProviders,
        publishToDittoCollection = true // Set to false to avoid publishing to collection
    )

    DisposableEffect(Unit) {
        val job = CoroutineScope(Dispatchers.Main).launch {
            startHeartbeat(ditto, config).collect { heartbeatInfo = it }
        }

        onDispose {
            job.cancel()
        }
    }

    // UI to display heartbeat info
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (heartbeatInfo != null) {
            HeartbeatInfoCard(heartbeatInfo!!)
        } else {
            // Loading indicator or placeholder
            CircularProgressIndicator(
                modifier = Modifier.size(50.dp),
                color = Color.Blue
            )
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HeartbeatInfoCard(heartbeatInfo: DittoHeartbeatInfo) {
    Column(
        modifier = Modifier
            .padding(16.dp)
    ) {
        HeartbeatHeader(heartbeatInfo)

        // Display presence information if available
        if (heartbeatInfo.presenceSnapshotDirectlyConnectedPeersCount > 0) {
            for (entry in heartbeatInfo.presenceSnapshotDirectlyConnectedPeers) {
                val connection = entry.value
                if (connection is Map<*, *>) { // Check if connection is a Map
                    @Suppress("UNCHECKED_CAST")
                    val typedConnection =
                        connection as Map<String, Any> // Type cast connection to Map<String, Any>
                    ConnectionInfo(connection = typedConnection)
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HeartbeatHeader(heartbeatInfo: DittoHeartbeatInfo) {
    Column {
        Text(stringResource(R.string.heartbeat_id_label, heartbeatInfo.id))
        Text(stringResource(R.string.heartbeat_sdk_label, heartbeatInfo.sdk))
        Text(stringResource(R.string.heartbeat_last_updated_label, heartbeatInfo.lastUpdated))
        Text(
            text = stringResource(
                R.string.heartbeat_remotepeerscount_label,
                heartbeatInfo.presenceSnapshotDirectlyConnectedPeersCount
            ),
            color = Color.Black
        )
        Text(stringResource(R.string.heartbeat_peer_key_label, heartbeatInfo.peerKey))
    }
}

@Composable
fun ConnectionInfo(connection: Map<String, Any>) {
    Column {
        Text(stringResource(R.string.connection_info_connection, connection["deviceName"] ?: ""))
        Text(stringResource(R.string.connection_info_sdk, connection["sdk"] ?: ""))
        val isConnectedToDittoCloudString = if (connection["isConnectedToDittoCloud"] as Boolean) {
            stringResource(R.string.connection_info_online)
        } else stringResource(
            R.string.connection_info_offline
        )
        Text(isConnectedToDittoCloudString)
        Text(stringResource(R.string.connection_info_bt, connection["bluetooth"] ?: ""))
        Text(stringResource(R.string.connection_info_p2pwifi, connection["p2pWifi"] ?: ""))
        Text(stringResource(R.string.connection_info_lan, connection["lan"] ?: ""))
    }
}


