package live.ditto.dittotoolsapp


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import live.ditto.Ditto
import live.ditto.DittoConnectionType
import live.ditto.DittoPeer
import live.ditto.dittoheartbeat.DittoHeartbeatConfig
import live.ditto.dittoheartbeat.DittoHeartbeatInfo
import live.ditto.dittoheartbeat.startHeartbeat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun ShowHeartbeatData(ditto: Ditto) {

    var heartbeatInfo by remember { mutableStateOf<DittoHeartbeatInfo?>(null) }

    val config = DittoHeartbeatConfig(
        id = mapOf(
            "storeId" to "Tulsa, OK",
            "venueId" to "Food Truck",
            "deviceId" to "123abc"
        ),
        secondsInterval = 30,
        collectionName = "devices4",
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
        if (heartbeatInfo.remotePeersCount > 0) {
            for (entry in heartbeatInfo.peerConnections) {
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
        Text("remotePeersCount: ${heartbeatInfo.remotePeersCount}", color = Color.Black)
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


