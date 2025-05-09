package live.ditto.dittotoolsapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import live.ditto.Ditto
import live.ditto.DittoIdentity
import live.ditto.DittoLogLevel
import live.ditto.DittoLogger
import live.ditto.android.DefaultAndroidDittoDependencies
import live.ditto.dittotoolsapp.ui.theme.DittoToolsAppTheme
import live.ditto.tools.toolsviewer.DittoToolsViewer
import live.ditto.transports.DittoSyncPermissions

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DittoToolsAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    var ditto: Ditto? by remember { mutableStateOf(null) }
                    var dittoError: String? by remember { mutableStateOf(null) }

                    LaunchedEffect(key1 = true) {
                        try {
                            ditto = createDitto()
                            ditto?.startSync()
                        } catch (e: Throwable) {
                            dittoError = e.message.toString()
                            Log.e("Ditto error", e.message.toString())
                        }
                    }

                    dittoError?.let {
                        DittoError(it)
                        return@Surface
                    }

                    if (ditto == null) {
                        Loading()
                        return@Surface
                    }

                    ditto?.let {
                        DittoToolsViewer(
                            ditto = it,
                            onExitTools = {  }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkPermissions()
    }

    private suspend fun createDitto(): Ditto = withContext(Dispatchers.Default) {
        val androidDependencies = DefaultAndroidDittoDependencies(applicationContext)
        val identity = DittoIdentity.OnlinePlayground(
            androidDependencies,
            appId = BuildConfig.DITTO_ONLINE_PLAYGROUND_APP_ID,
            token = BuildConfig.DITTO_ONLINE_PLAYGROUND_TOKEN,
            enableDittoCloudSync = true
        )
        val ditto = Ditto(androidDependencies, identity)
        DittoLogger.minimumLogLevel = DittoLogLevel.DEBUG

        ditto
    }

    private fun checkPermissions() {
        val missing = DittoSyncPermissions(this).missingPermissions()
        if (missing.isNotEmpty()) {
            this.requestPermissions(missing, 0)
        }
    }
}


@Composable
private fun DittoError(text: String) {
    DittoToolsAppTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .align(Alignment.Center)
            ) {
                Text(
                    text = "Ditto Error", fontWeight = FontWeight.Bold
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Text(text = text)
            }
        }
    }
}

@Composable
private fun Loading() {
    DittoToolsAppTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    DittoToolsAppTheme {}
}