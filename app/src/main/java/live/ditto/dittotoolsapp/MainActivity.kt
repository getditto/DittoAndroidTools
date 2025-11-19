package live.ditto.dittotoolsapp

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.delay
import live.ditto.Ditto
import live.ditto.dittotoolsapp.ui.theme.DittoToolsAppTheme
import live.ditto.tools.toolsviewer.DittoToolsViewer

class MainActivity : ComponentActivity() {

    private val app by lazy { application as DittoToolsApplication }
    
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DittoToolsAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    var ditto: Ditto? by remember { mutableStateOf(null) }

                    LaunchedEffect(key1 = true) {
                        while (app.getDittoOrNull() == null) {
                            delay(100)
                        }
                        ditto = app.ditto
                    }

                    if (ditto == null) {
                        Loading()
                        return@Surface
                    }

                    DittoToolsViewer(
                        ditto = ditto!!
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkPermissions()
    }

    private fun checkPermissions() {
        val missing = app.missingPermissions()
        if (missing.isNotEmpty()) {
            requestPermissions(missing, 0)
            app.getDittoOrNull()?.refreshPermissions()
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