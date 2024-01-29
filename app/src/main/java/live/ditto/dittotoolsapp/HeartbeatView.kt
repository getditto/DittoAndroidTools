package live.ditto.dittotoolsapp


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import live.ditto.Ditto
import live.ditto.dittoheartbeat.HeartbeatConfig
import live.ditto.dittoheartbeat.HeartbeatInfo
import live.ditto.dittoheartbeat.startHeartbeat
import java.util.*

@Composable
fun ShowHeartbeatData(ditto: Ditto) {

    var heartbeatInfo by remember { mutableStateOf<HeartbeatInfo?>(null) }

    val config = HeartbeatConfig(
        interval = 10000,
        collectionName = "devices",
        metaData = mapOf(
         "locationId" to "abc123",
         "deviceId" to "123abc"
        )
    )

//    // Start heartbeat and observe the data
//    val handler = startHeartbeat(ditto, config) {
//        heartbeatInfo = it
//    }

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


@Composable
fun HeartbeatInfoCard(heartbeatInfo: HeartbeatInfo) {
    Column(
        modifier = Modifier
            .padding(16.dp)
    ) {
        Text("Device ID: ${heartbeatInfo.id["deviceId"]}")
        Text("Location ID: ${heartbeatInfo.id["locationId"]}")
        Text("\nLast Updated: ${heartbeatInfo.lastUpdated}")

        // Display presence information if available
        heartbeatInfo.presence?.let {
            Text("\nTotal Connections: ${it.totalConnections}", color = Color.Black)
            it.connections.forEachIndexed { index, connection ->
                Text("Connection ${index + 1}: \n     ID: ${connection.id.substring(0, minOf(connection.id.length, 10))} \n     Type:  ${connection.connectionType}") // Replace with actual property
            }
        }
    }
}

