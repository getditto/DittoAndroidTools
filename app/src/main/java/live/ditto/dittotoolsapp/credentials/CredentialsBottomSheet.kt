package live.ditto.dittotoolsapp.credentials

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.hilt.navigation.compose.hiltViewModel
import java.util.UUID
import live.ditto.Ditto
import live.ditto.DittoAuthenticationStatus
import live.ditto.DittoAuthenticationStatusDidChangeCallback
import live.ditto.DittoAuthenticator
import live.ditto.DittoPresenceGraph
import live.ditto.dittotoolsapp.model.CredentialsData
import live.ditto.dittotoolsapp.model.IdentityType
import androidx.compose.runtime.DisposableEffect
import live.ditto.DittoPresenceObserver

@Composable
fun CredentialsBottomSheetContent(
    onDismiss: () -> Unit,
    onCredentialsSaved: () -> Unit,
    ditto: Ditto? // Added Ditto parameter
) {
    val viewModel: CredentialsViewModel = hiltViewModel()
    val existingCredentials = remember { viewModel.activeCredentials }

    var appId by remember { mutableStateOf(existingCredentials?.appId ?: "") }
    var token by remember { mutableStateOf(existingCredentials?.token ?: "") }
    var customAuthUrl by remember { mutableStateOf(existingCredentials?.authProvider ?: "") }
    var socketUrl by remember { mutableStateOf(existingCredentials?.socketUrl ?: "") }
    var enableCloudSync by remember { mutableStateOf(existingCredentials?.enableCloudSync ?: true) }

    val isAppIdValid = remember(appId) { appId.isUuid() }
    val isTokenValid = remember(token) { token.isUuid() }
    val authenticationState by viewModel.authenticationState.collectAsState()
    val authStatus by viewModel.authStatus.collectAsState()
    val isAuthenticated by viewModel.isAuthenticated.collectAsState()
    val lastError by viewModel.lastError.collectAsState()

    var showClearCredentialsDialog by remember { mutableStateOf(false) }
    var showInitializationErrorDialog by remember { mutableStateOf(false) }
    var initializationErrorMessage by remember { mutableStateOf("") }
    var dittoStatus by remember { mutableStateOf("Ditto status: Not initialized") }

    // Status will be updated via DisposableEffect observers

    DisposableEffect(ditto) {
        var authStatusCallback: DittoAuthenticationStatusDidChangeCallback? = null
        var presenceObserver: DittoPresenceObserver? = null

        if (ditto != null) {
            // Initial status
            val initialAuthStatus = ditto.auth?.status
            val authStatusText = if (initialAuthStatus?.isAuthenticated == true) {
                "Authenticated" + (initialAuthStatus.userId?.let { " (User: $it)" } ?: "")
            } else {
                "Not Authenticated"
            }
            dittoStatus = "Ditto status: $authStatusText"

            // Observe status changes
            authStatusCallback = object : DittoAuthenticationStatusDidChangeCallback {
                override fun authenticationStatusDidChange(status: DittoAuthenticationStatus) {
                    val updatedAuthStatusText = if (status.isAuthenticated) {
                        "Authenticated" + (status.userId?.let { " (User: $it)" } ?: "")
                    } else {
                        "Not Authenticated"
                    }
                    dittoStatus = "Ditto status: $updatedAuthStatusText"
                }
            }
            ditto.auth?.observeStatus(authStatusCallback!!)

            presenceObserver = ditto.presence.observe { presenceGraph: DittoPresenceGraph ->
                val remotePeers = presenceGraph.remotePeers
                val localPeer = presenceGraph.localPeer
                dittoStatus += ", Connected to ${remotePeers.size} remote peers"
            }
        }

        onDispose {
            // TODO: Remove auth status observer when API is available
            // if (ditto != null && authStatusCallback != null) {
            //     ditto.auth?.removeObserver(authStatusCallback!!)
            // }
            presenceObserver?.close()
        }
    }

    // Handle authentication state changes - but only show dialog if initialization error dialog isn't already showing
    LaunchedEffect(authenticationState) {
        when (authenticationState) {
            is AuthenticationState.Success -> {
                // Do nothing here, success is handled by the Test Connection button
                viewModel.resetAuthenticationState()
            }
            is AuthenticationState.Failure -> {
                // Only show the legacy dialog if the new initialization dialog isn't already handling it
                if (!showInitializationErrorDialog) {
                    initializationErrorMessage = (authenticationState as AuthenticationState.Failure).message
                    showInitializationErrorDialog = true
                }
            }
            else -> { /* Do nothing */ }
        }
    }

    // Handle provider errors
    LaunchedEffect(lastError) {
        lastError?.let { error ->
            if (!showInitializationErrorDialog) {
                initializationErrorMessage = error
                showInitializationErrorDialog = true
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
            Text(
                "Credentials",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = {
                    val credentials = CredentialsData(
                        identityType = IdentityType.ONLINE_PLAYGROUND,
                        appId = appId,
                        token = token,
                        enableCloudSync = enableCloudSync,
                        authProvider = customAuthUrl.takeIf { it.isNotBlank() },
                        socketUrl = socketUrl.takeIf { it.isNotBlank() }
                    )
                    viewModel.saveCredentials(credentials)
                    onCredentialsSaved()
                },
                enabled = isAppIdValid && isTokenValid
            ) {
                Text("Apply")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Identity Type
        Text(
            "Identity Type",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Online Playground")
            Text("⌄", style = MaterialTheme.typography.bodyLarge)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // App ID Field
        Text(
            "App ID (UUID)",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedTextField(
            value = appId,
            onValueChange = { appId = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("123e4567-e89b-12d3-a456-426614...") },
            singleLine = true,
            isError = !isAppIdValid && appId.isNotEmpty(),
            trailingIcon = {
                if (isAppIdValid) {
                    Icon(Icons.Default.Check, contentDescription = "Valid App ID")
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Token Field
        Text(
            "Playground Token (UUID)",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedTextField(
            value = token,
            onValueChange = { token = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("123e4567-e89b-12d3-a456-426614...") },
            singleLine = true,
            isError = !isTokenValid && token.isNotEmpty(),
            trailingIcon = {
                if (isTokenValid) {
                    Icon(Icons.Default.Check, contentDescription = "Valid Token")
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Custom Auth URL Field
        Text(
            "Custom Auth URL (OPTIONAL)",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedTextField(
            value = customAuthUrl,
            onValueChange = { customAuthUrl = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("https://example.com") },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Socket URL Field
        Text(
            "Socket URL (OPTIONAL)",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedTextField(
            value = socketUrl,
            onValueChange = { socketUrl = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("wss://example.com/socket") },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Cloud Sync Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Enable Cloud Sync")
            Switch(
                checked = enableCloudSync,
                onCheckedChange = { enableCloudSync = it },
                colors = SwitchDefaults.colors(
                    uncheckedThumbColor = Color.Gray,
                    uncheckedTrackColor = Color.LightGray
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Footer text
        Text(
            "Applying/testing these credentials will restart the Ditto sync engine.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Test Connection Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            val isLoading = authenticationState is AuthenticationState.Loading
            val buttonText = when {
                isLoading -> {
                    if (authenticationState is AuthenticationState.Loading) "Testing Connection..." else "Loading..."
                }
                authenticationState is AuthenticationState.Success -> "Connection Successful!"
                authenticationState is AuthenticationState.Failure -> "Test Connection"
                else -> "Test Connection"
            }
            
            Button(
                onClick = {
                    val credentials = CredentialsData(
                        identityType = IdentityType.ONLINE_PLAYGROUND,
                        appId = appId,
                        token = token,
                        enableCloudSync = enableCloudSync,
                        authProvider = customAuthUrl.takeIf { it.isNotBlank() },
                        socketUrl = socketUrl.takeIf { it.isNotBlank() }
                    )
                    viewModel.testConnection(credentials, ditto)
                },
                enabled = (isAppIdValid && isTokenValid) && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White)
                } else {
                    Text(buttonText)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp)) // Spacer below Test Connection button

        // Enhanced status display with auth service state
        val detailedStatus = remember(dittoStatus, authenticationState, authStatus, lastError) {
            when {
                authenticationState is AuthenticationState.Loading -> "Testing connection..."
                lastError != null -> "Error: $lastError"
                else -> {
                    val currentAuthStatus = authStatus
                    if (currentAuthStatus != null && currentAuthStatus.isAuthenticated) {
                        "Authenticated (${currentAuthStatus.userId ?: "Unknown User"})"
                    } else {
                        dittoStatus
                    }
                }
            }
        }
        
        Text(
            text = detailedStatus,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Clear Credentials button - always show but with conditional styling
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            val hasCredentials = existingCredentials != null
            val hasValidFields = isAppIdValid && isTokenValid
            val isEnabled = hasCredentials || hasValidFields
            
            TextButton(
                onClick = {
                    showClearCredentialsDialog = true
                },
                enabled = isEnabled
            ) {
                Text(
                    text = "Clear Credentials",
                    color = if (isEnabled) {
                        MaterialTheme.colorScheme.error // Always use danger/red color when enabled
                    } else {
                        Color.Gray
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showClearCredentialsDialog) {
        AlertDialog(
            onDismissRequest = { showClearCredentialsDialog = false },
            title = { Text("Clear Credentials") },
            text = { Text("Are you sure you want to clear all credentials? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearCredentials()
                        // Reset form fields
                        appId = ""
                        token = ""
                        customAuthUrl = ""
                        socketUrl = ""
                        enableCloudSync = true
                        // Reset any error states
                        viewModel.resetAuthenticationState()
                        showClearCredentialsDialog = false
                        onDismiss() // Just dismiss the sheet, don't trigger connection logic
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("OK", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearCredentialsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showInitializationErrorDialog) {
        AlertDialog(
            onDismissRequest = { 
                showInitializationErrorDialog = false
                initializationErrorMessage = ""
            },
            title = { Text("Connection Failed") },
            text = { 
                Text(
                    text = initializationErrorMessage.takeIf { it.isNotBlank() } 
                        ?: "An unknown error occurred during connection test."
                )
            },
            confirmButton = {
                Button(onClick = { 
                    showInitializationErrorDialog = false
                    initializationErrorMessage = ""
                }) {
                    Text("OK")
                }
            }
        )
    }
}

private fun String.isUuid(): Boolean {
    return try {
        UUID.fromString(this)
        true
    } catch (e: IllegalArgumentException) {
        false
    }
}
