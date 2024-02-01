package live.ditto.dittotoolsapp


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
        collectionName = "devices2",
//        metaData = mapOf()
    )

    // Cleanup when the composable is disposed
    DisposableEffect(Unit) {
        // Start heartbeat and observe the data
        val handler = startHeartbeat(ditto, config) {
            heartbeatInfo = it
        }

        onDispose {
            handler.removeCallbacksAndMessages(null)
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
        Text("Mesh ID: ${heartbeatInfo.id}")
//        Text("Location ID: ${heartbeatInfo.id["locationId"]}")
        // Parse the ISO-8601 string to LocalDateTime
        val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        val dateTime = LocalDateTime.parse(heartbeatInfo.lastUpdated, dateTimeFormatter)
        val customFormatter = DateTimeFormatter.ofPattern("hh:mm a MM/dd/yyyy")
        val formattedDateTime = dateTime.format(customFormatter)
        Text("Last Updated: $formattedDateTime")
        heartbeatInfo.presence?.let {
            Text("Total Peers in Mesh: ${it.totalPeers}", color = Color.Black)
        }
    }
}

@Composable
fun ConnectionInfo(connection: DittoPeer) {
    Column {
        Text("\nConnection: ${connection.deviceName}")
        Text(text = if (connection.isConnectedToDittoCloud) "Online" else "Offline")
//        Text("Total Connections: ${connection.connections.size}")
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

