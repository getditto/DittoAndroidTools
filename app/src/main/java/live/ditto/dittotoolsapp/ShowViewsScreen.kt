package live.ditto.dittotoolsapp

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import live.ditto.Ditto
import live.ditto.dittoexportlogs.ExportLogs
import live.ditto.presencedegradationreporter.presenceDegradationReporterFlow

@Composable
fun ShowViewsScreen(navController: NavHostController, ditto: Ditto) {
    var message: String? by remember { mutableStateOf(null) }

    LaunchedEffect(key1 = ditto) {
        ditto.presenceDegradationReporterFlow().collect { state ->
            if (!state.settings.reportApiEnabled) return@collect
            if (!state.settings.hasSeenExpectedPeers) return@collect

            val expectedPeers = state.settings.expectedPeers
            val connectedPeers = state.remotePeers.count { it.connected }
            message = "Reporting: ExpectedPeers=$expectedPeers, ConnectedPeers=$connectedPeers"
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        var showExportDialog by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Button(
                onClick = { navController.navigate("dataBrowser") },
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text("Data Browser")
            }
            Button(
                onClick = { navController.navigate("diskUsage") },
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text("Disk Usage")
            }
            Button(
                onClick = { showExportDialog = !showExportDialog },
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text("Export Logs")
            }
            Button(
                onClick = { navController.navigate("presenceViewer") },
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text("Presence Viewer")
            }

            Button(
                onClick = { navController.navigate("health") },
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text("Health Viewer")
            }
            Button(
                onClick = { navController.navigate("heartbeatInfo") },
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text("Heartbeat Info")
            }

            Button(
                onClick = { navController.navigate("presencedegradationreporter") },
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text("Presence Degradation Reporter")
            }

            if (showExportDialog) {
                ExportLogs(onDismiss = { showExportDialog = false })
            }

            Spacer(modifier = Modifier.weight(1f))

            message?.let {
                Snackbar { Text(text = it) }
            }
        }
    }
}