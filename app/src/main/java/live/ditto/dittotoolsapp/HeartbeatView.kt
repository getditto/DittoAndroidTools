package live.ditto.dittotoolsapp

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.dittodiskusage.DiskUsageViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import live.ditto.Ditto
import live.ditto.healthmetrics.HealthMetricProvider
import live.ditto.dittoheartbeat.DittoHeartbeatConfig
import live.ditto.dittoheartbeat.DittoHeartbeatInfo
import live.ditto.dittoheartbeat.startHeartbeat
import java.util.*


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ShowHeartbeatData(ditto: Ditto) {

    var heartbeatInfo by remember { mutableStateOf<DittoHeartbeatInfo?>(null) }
    var healthMetricProviders: MutableList<HealthMetricProvider> = mutableListOf()
    val diskUsageViewModel = DiskUsageViewModel()
    diskUsageViewModel.isHealthyMBSizeLimit = 2048 //2GB worth of data
//    healthMetricProviders.add(diskUsageViewModel)

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
                    val typedConnection = connection as Map<String, Any> // Type cast connection to Map<String, Any>
                    ConnectionInfo(connection = typedConnection)
                } else {
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HeartbeatHeader(heartbeatInfo: DittoHeartbeatInfo) {
    Column {
        Text("ID: ${heartbeatInfo.id}")
        Text("SDK: ${heartbeatInfo.sdk}")
        Text("Last Updated: ${heartbeatInfo.lastUpdated}")
        Text("remotePeersCount: ${heartbeatInfo.presenceSnapshotDirectlyConnectedPeersCount}", color = Color.Black)
    }
}

@Composable
fun ConnectionInfo(connection: Map<String, Any>) {
    Column {
        Text("\nConnection: ${connection["deviceName"]}")
        Text("SDK: ${connection["sdk"]}")
        Text(text = if (connection["isConnectedToDittoCloud"] as Boolean) "Online" else "Offline")
        Text("BT: ${connection["bluetooth"]}")
        Text("P2PWifi: ${connection["p2pWifi"]}")
        Text("LAN: ${connection["lan"]}")
    }
}


