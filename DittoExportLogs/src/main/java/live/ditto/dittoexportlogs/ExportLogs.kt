package live.ditto.dittoexportlogs

import android.os.Build
import android.os.Environment
import android.util.Log
import android.widget.Toast
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import live.ditto.DittoError
import live.ditto.DittoLogger
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ExportLogs(onDismiss: () -> Unit) {

    val title = stringResource(R.string.export_logs)

    val confirmText = stringResource(R.string.export)
    val cancelText = stringResource(R.string.cancel)
    val modifier = Modifier
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
    val appName = context.applicationInfo.loadLabel(context.packageManager).toString()

    val fileName = "$appName-dittologs-$now.jsonl.gz"
    val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    val filePath = File(downloadsDir, fileName).absolutePath

    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = {
            Text(title)
        },
        text = {
            Text("Logs will be exported to: \nDownloads/$fileName\"")
        },

        confirmButton = {
            Button(
                onClick = {


                    coroutineScope.launch {
                        isLoading = true
                        try {
                            val sizeWritten = DittoLogger.exportToFile(filePath)

                            val sizeWrittenInKB = sizeWritten / 1024u
                            val toastText = "Exported file size: $sizeWrittenInKB kB"
                            Toast.makeText(context, toastText, Toast.LENGTH_LONG).show()
                        }
                        catch (e: DittoError.IoError) {
                            Log.e("DittoTools LogExporter", e.reason.toString())
                            Toast.makeText(context, "Error exporting logs: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                        finally {
                            isLoading = false
                            onDismiss()
                        }
                    }
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






