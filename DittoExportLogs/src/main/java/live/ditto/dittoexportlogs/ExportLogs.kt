package live.ditto.dittoexportlogs

import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.ui.platform.LocalContext
import java.nio.file.Path
import live.ditto.DittoLogger
import androidx.compose.runtime.Composable
import java.io.FileInputStream

@Composable
fun ExportLogs(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument()) { uri ->
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





