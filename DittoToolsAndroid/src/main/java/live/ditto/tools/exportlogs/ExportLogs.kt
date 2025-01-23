package live.ditto.tools.exportlogs

import android.os.Build
import android.os.Environment
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import live.ditto.tools.R
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ExportLogs(onDismiss: () -> Unit, viewModel: ExportLogsViewModel = ExportLogsViewModel()) {

    val isLoading by remember { mutableStateOf(viewModel.isLoading)}

    val title = stringResource(R.string.export_logs)

    val confirmText = stringResource(R.string.export)
    val cancelText = stringResource(R.string.cancel)
    val context = LocalContext.current

    val now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
    val appName = context.applicationInfo.loadLabel(context.packageManager).toString()

    val fileName = "$appName-dittologs-$now.jsonl.gz"
    val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    val filePath = File(downloadsDir, fileName).absolutePath

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(title)
        },
        text = {
            Text(stringResource(R.string.export_logs_message, fileName))
        },

        confirmButton = {
            Button(
                onClick = {
                       viewModel.exportLogs(filePath, context, onDismiss)
                }
            ) {
                Row {
                    Text(confirmText)

                    if (isLoading) {
                        Spacer(Modifier.width(16.dp))

                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    }
                }
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
            ) {
                Text(cancelText)
            }
        }
    )
}






