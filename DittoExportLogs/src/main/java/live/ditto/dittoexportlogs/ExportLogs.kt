package live.ditto.dittoexportlogs

import androidx.compose.runtime.Composable
import live.ditto.exporter.ExportDialog
import java.nio.file.Path

@Composable
fun ExportLogs(onDismiss: () -> Unit) {
    ExportDialog(
        title = "Export Logs",
        text = "Do you want to export logs?",
        confirmText = "Export",
        cancelText = "Cancel",
        fileProvider = { getZippedLogs().toFile() },
        mimeType = "application/x-zip",
        onDismiss = onDismiss
    )
}

private fun getZippedLogs(): Path {
    return DittoLogManager.createLogsZip()
}





