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
import live.ditto.dittoheartbeat.HeartbeatConfig
import live.ditto.dittoheartbeat.HeartbeatInfo
import live.ditto.dittoheartbeat.startHeartbeat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun ShowHeartbeatData(ditto: Ditto) {

    var heartbeatInfo by remember { mutableStateOf<HeartbeatInfo?>(null) }

    val config = HeartbeatConfig(
        id = mapOf(
            "storeId" to "Tulsa, OK",
            "venueId" to "Food Truck",
            "deviceId" to "123abc"
        ),
        interval = 30000,
        collectionName = "devices",
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
fun HeartbeatInfoCard(heartbeatInfo: HeartbeatInfo) {
    Column(
        modifier = Modifier
            .padding(16.dp)
    ) {
        HeartbeatHeader(heartbeatInfo)

        // Display presence information if available
        heartbeatInfo.presence?.let {
            LazyColumn {
                items(it.peers) { connection ->
                    ConnectionInfo(connection)
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HeartbeatHeader(heartbeatInfo: HeartbeatInfo) {
    Column {
        Text("ID: ${heartbeatInfo.id}")
        Text("Last Updated: $heartbeatInfo.lastUpdated")
        heartbeatInfo.presence?.let {
            Text("remotePeersCount: ${it.remotePeersCount}", color = Color.Black)
        }
    }
}

@Composable
fun ConnectionInfo(connection: DittoPeer) {
    Column {
        Text("\nConnection: ${connection.deviceName}")
        Text(text = if (connection.isConnectedToDittoCloud) "Online" else "Offline")
        val connectionsMap = getConnectionTypeCount(connection = connection)
        Text("BT: ${connectionsMap["bt"]}")
        Text("P2PWifi: ${connectionsMap["p2pWifi"]}")
        Text("LAN: ${connectionsMap["lan"]}")
    }
}

fun getConnectionTypeCount(connection: DittoPeer): Map<String, Int> {
    var bt = 0
    var p2pWifi = 0
    var lan = 0

    connection.connections.forEach { connection ->
        when (connection.connectionType) {
            DittoConnectionType.Bluetooth -> bt += 1
            DittoConnectionType.P2PWiFi -> p2pWifi += 1
            DittoConnectionType.AccessPoint, DittoConnectionType.WebSocket -> lan += 1
        }
    }
    return mapOf("bt" to bt, "p2pWifi" to p2pWifi, "lan" to lan)
}

