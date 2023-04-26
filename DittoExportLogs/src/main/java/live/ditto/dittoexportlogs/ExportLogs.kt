package live.ditto.dittoexportlogs

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import live.ditto.DittoLogger
import java.io.FileInputStream
import java.nio.file.Path

@Composable
fun ExportLogs(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val exportLauncher = rememberLauncherForActivityResult(CreateDocument("application/zip")) { uri ->
        uri?.let {
            val inputStream = FileInputStream(Config.zippedLogsFile.toFile())
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        onDismiss()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Export Logs") },
        text = { Text("Do you want to export logs?") },
        confirmButton = {
            Button(onClick = {
                val zippedLogsFile = getZippedLogs()
                zippedLogsFile.let {
                    exportLauncher.launch(it.fileName.toString())
                }
            }) {
                Text("Export")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

fun getZippedLogs(): Path {
    DittoLogManager.logFile.let { logFile ->
        DittoLogger.setLogFile(logFile.toString())
    }
    return DittoLogManager.createLogsZip()
}





