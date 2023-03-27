package live.ditto.dittotoolsapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.dittodiskusage.DittoDiskUsage
import live.ditto.Ditto
import live.ditto.DittoError
import live.ditto.DittoIdentity
import live.ditto.android.DefaultAndroidDittoDependencies
import live.ditto.dittodatabrowser.DataBrowser
import live.ditto.dittotoolsapp.ui.theme.DittoToolsAppTheme
import live.ditto.transports.DittoSyncPermissions

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lateinit var ditto: Ditto
        try {
            val androidDependencies = DefaultAndroidDittoDependencies(applicationContext)
            val identity = DittoIdentity.OnlinePlayground(androidDependencies, appId = "YOUR_APP_ID", token = "YOUR_TOKEN", enableDittoCloudSync = true)
            ditto = Ditto(androidDependencies, identity)
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
//                    DataBrowser(ditto = ditto)
                    DittoDiskUsage(ditto = ditto)
                }
            }
        }
    }

    fun checkPermissions() {
        val missing = DittoSyncPermissions(this).missingPermissions()
        if (missing.isNotEmpty()) {
            this.requestPermissions(missing, 0)
        }
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    DittoToolsAppTheme {
    }
}