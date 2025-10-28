package live.ditto.tools.exportlogs

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import live.ditto.Ditto
import live.ditto.tools.R

@Composable
fun ExportLogsToPortal(
    ditto: Ditto,
    onDismiss: () -> Unit,
    viewModel: ExportLogsViewModel = viewModel()
) {
    val title = stringResource(R.string.export_logs_to_portal_tool_label)

    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    DisposableEffect(Unit) {
        onDispose {
            viewModel.resetState()
        }
    }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is ExportLogsUiState.Success -> {
                Toast.makeText(
                    context,
                    context.getString(R.string.export_requested_successfully),
                    Toast.LENGTH_SHORT
                ).show()
                onDismiss()
            }

            is ExportLogsUiState.Error -> {
                Log.d("ExportLogsToPortal", state.message)
                Toast.makeText(
                    context, context.getString(R.string.export_failed),
                    Toast.LENGTH_LONG
                ).show()
            }

            else -> {}
        }
    }

    AlertDialog(
        onDismissRequest = {
            if (uiState !is ExportLogsUiState.Exporting) {
                onDismiss()
            }
        },
        title = {
            Text(title)
        },
        text = {
            Column {
                Text(
                    text = stringResource(
                        R.string.logs_will_be_exported_to_portal_for_appid,
                        ditto.appId ?: "unknown"
                    )
                )
                if (uiState is ExportLogsUiState.Error) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = (uiState as ExportLogsUiState.Error).message,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                enabled = uiState !is ExportLogsUiState.Exporting,
                onClick = {
                    viewModel.onConfirmClicked(ditto, context)
                }
            ) {
                if (uiState is ExportLogsUiState.Exporting) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.exporting))
                    }
                } else {
                    val buttonText = if (uiState is ExportLogsUiState.Error) {
                        stringResource(R.string.retry)
                    } else {
                        stringResource(R.string.export)
                    }
                    Text(buttonText)
                }
            }
        },
        dismissButton = {
            Button(
                enabled = uiState !is ExportLogsUiState.Exporting,
                onClick = onDismiss
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
