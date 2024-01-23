package live.ditto.dittotoolsapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dittodiskusage.DittoDiskUsage
import ditto.live.dittopresenceviewer.DittoPresenceViewer
import live.ditto.*
import live.ditto.android.DefaultAndroidDittoDependencies
import live.ditto.dittodatabrowser.DittoDataBrowser
import live.ditto.dittoexportlogs.ExportLogs
import live.ditto.dittotoolsapp.ui.theme.DittoToolsAppTheme
import live.ditto.transports.DittoSyncPermissions

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lateinit var ditto: Ditto
        try {
            val androidDependencies = DefaultAndroidDittoDependencies(applicationContext)
            val identity = DittoIdentity.OnlinePlayground(androidDependencies, appId = "YOUR_APPID", token = "YOUR_TOKEN", enableDittoCloudSync = true)
            ditto = Ditto(androidDependencies, identity)
            DittoLogger.minimumLogLevel = DittoLogLevel.DEBUG

            LogFileConfig.logFile.let { logFile ->
                DittoLogger.setLogFile(logFile.toString())
            }

            ditto.startSync()

        } catch(e: DittoError) {
            Log.e("Ditto error", e.message!!)
        }

        checkPermissions()

        setContent {
            DittoToolsAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Root(ditto = ditto)
                }
            }
        }
    }

    private fun checkPermissions() {
        val missing = DittoSyncPermissions(this).missingPermissions()
        if (missing.isNotEmpty()) {
            this.requestPermissions(missing, 0)
        }
    }
}

@Composable
fun ShowViews(navController: NavHostController, ditto: Ditto) {
    Box(modifier = Modifier.fillMaxSize()) {
        var showExportDialog by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Button(
                onClick = {navController.navigate("dataBrowser")},
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

            if (showExportDialog) {
                ExportLogs(onDismiss = { showExportDialog = false })
            }
        }
    }
}

@Composable
fun Root(ditto: Ditto) {

    val navController = rememberNavController()

    // A surface container using the 'background' color from the theme
    Surface(color = MaterialTheme.colorScheme.background) {
        NavHost(navController = navController, startDestination = "showViews") {
            composable("showViews") { ShowViews(navController = navController, ditto = ditto) }
            composable("dataBrowser") { DittoDataBrowser(ditto = ditto) }
            composable("diskUsage") { DittoDiskUsage( ditto = ditto) }
            composable("presenceViewer") { DittoPresenceViewer(ditto = ditto)}
        }
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    DittoToolsAppTheme {
    }
}