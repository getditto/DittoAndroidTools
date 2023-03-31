package live.ditto.dittotoolsapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.dittodiskusage.DittoDiskUsage
import live.ditto.*
import live.ditto.android.DefaultAndroidDittoDependencies
import live.ditto.dittodatabrowser.DataBrowser
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
                    color = MaterialTheme.colors.background
                ) {
                    ShowViews(ditto = ditto)
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
fun ShowViews(ditto: Ditto) {
    Box(modifier = Modifier.fillMaxSize()) {
        var showDataBrowser by remember { mutableStateOf(false) }
        var showDiskUsage by remember { mutableStateOf(false) }
        var showExportDialog by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier.align(Alignment.TopStart).padding(16.dp)
        ) {
            Button(
                onClick = { showDataBrowser = !showDataBrowser },
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text("Data Browser")
            }
            Button(
                onClick = { showDiskUsage = !showDiskUsage },
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
        }

        if (showDataBrowser) {
            DataBrowser(ditto = ditto)
        }

        if (showDiskUsage) {
            DittoDiskUsage(ditto = ditto)
        }

        if (showExportDialog) {
            ExportLogs(onDismiss = { showExportDialog = false })
        }
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    DittoToolsAppTheme {
    }
}