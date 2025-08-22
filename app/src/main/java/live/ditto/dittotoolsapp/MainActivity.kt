package live.ditto.dittotoolsapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import java.math.BigInteger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import live.ditto.Ditto
import live.ditto.DittoIdentity
import live.ditto.DittoLogLevel
import live.ditto.DittoLogger
import live.ditto.android.DefaultAndroidDittoDependencies
import live.ditto.dittotoolsapp.credentials.CredentialsBottomSheetContent
import live.ditto.dittotoolsapp.model.CredentialsData
import live.ditto.dittotoolsapp.model.IdentityType
import live.ditto.dittotoolsapp.service.CredentialsService
import live.ditto.dittotoolsapp.service.DittoService
import live.ditto.dittotoolsapp.service.AuthService
import live.ditto.dittotoolsapp.ui.theme.DittoToolsAppTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import live.ditto.tools.toolsviewer.DittoToolsViewer
import live.ditto.transports.DittoSyncPermissions

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var dittoService: DittoService
    
    @Inject
    lateinit var authService: AuthService
    
    @Inject
    lateinit var credentialsService: CredentialsService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DittoToolsAppTheme {
                MainScreen()
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun MainScreen() {
        Surface(
            modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
        ) {
            var ditto: Ditto? by remember { mutableStateOf(null) }
            var dittoError: String? by remember { mutableStateOf(null) }
            val activeCredentials = remember { credentialsService.activeCredentials }
            var showCredentialsBottomSheet by remember { mutableStateOf(false) }
            val credentialsSheetState = rememberModalBottomSheetState(
                skipPartiallyExpanded = true,
                confirmValueChange = { it != SheetValue.Hidden }
            )

            LaunchedEffect(key1 = activeCredentials) {
                activeCredentials?.let { credentialsData ->
                    try {
                        dittoService.stopSync()
                        dittoService.initialize(credentialsData)
                        ditto = dittoService.ditto
                        dittoError = null
                    } catch (e: Throwable) {
                        dittoError = e.message.toString()
                        Log.e("Ditto error", e.message.toString())
                    }
                }
            }

            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = {
                        showCredentialsBottomSheet = true
                    }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                }

                dittoError?.let {
                    DittoError(it)
                    return@Column
                }

                if (activeCredentials == null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Configure credentials to start")
                    }

                    // Show credentials dialog automatically
                    LaunchedEffect(Unit) {
                        showCredentialsBottomSheet = true
                    }
                    return@Column
                }

                if (ditto == null) {
                    Loading()
                    return@Column
                }

                ditto?.let {
                    DittoToolsViewer(
                        ditto = it,
                        onExitTools = { /* No-op */ }
                    )
                }
            }

            // Bottom Sheet
            if (showCredentialsBottomSheet) {
                ModalBottomSheet(
                    onDismissRequest = {
                        // Prevent dismissal - only allow through explicit actions
                    },
                    sheetState = credentialsSheetState,
                    dragHandle = null,
                    windowInsets = WindowInsets(0)
                ) {
                    CredentialsBottomSheetContent(
                        onDismiss = {
                            if (activeCredentials != null) {
                                showCredentialsBottomSheet = false
                            }
                        },
                        onCredentialsSaved = {
                            showCredentialsBottomSheet = false
                            recreate()
                        },
                        ditto = ditto
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkPermissions()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Hilt handles cleanup automatically
        dittoService.stopSync()
        authService.destroy()
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