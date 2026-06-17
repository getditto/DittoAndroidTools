package live.ditto.dittotoolsapp

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.ditto.kotlin.Ditto
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import live.ditto.dittotoolsapp.ui.theme.DittoToolsAppTheme
import live.ditto.tools.toolsviewer.DittoToolsViewer

class MainActivity : ComponentActivity() {

    private val app by lazy { application as DittoToolsApplication }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DittoToolsAppTheme {
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

                    ToolsWithLoadGenerator(ditto = ditto!!)
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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun ToolsWithLoadGenerator(ditto: Ditto) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var inFlight by remember { mutableStateOf(false) }
    var totalInserted by remember { mutableStateOf(0) }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    if (inFlight) return@ExtendedFloatingActionButton
                    inFlight = true
                    scope.launch {
                        try {
                            DemoLoadGenerator.insertBatch(ditto)
                            totalInserted += DemoLoadGenerator.BATCH_SIZE
                            Toast.makeText(
                                context,
                                "Inserted ${DemoLoadGenerator.BATCH_SIZE} into ${DemoLoadGenerator.DEMO_COLLECTION} (session total: $totalInserted)",
                                Toast.LENGTH_SHORT
                            ).show()
                        } catch (t: Throwable) {
                            Toast.makeText(
                                context,
                                "Insert failed: ${t.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        } finally {
                            inFlight = false
                        }
                    }
                },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text(if (inFlight) "Inserting…" else "Add ${DemoLoadGenerator.BATCH_SIZE} docs") },
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            DittoToolsViewer(ditto = ditto)
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
