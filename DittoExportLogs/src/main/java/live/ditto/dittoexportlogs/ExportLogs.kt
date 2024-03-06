package live.ditto.dittoexportlogs

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import live.ditto.exporter.ExportDialog
import java.nio.file.Path

@Composable
fun ExportLogs(onDismiss: () -> Unit) {
    ExportDialog(
        title = stringResource(R.string.export_logs),
        text = stringResource(R.string.do_you_want_to_export_logs),
        confirmText = stringResource(R.string.export),
        cancelText = stringResource(R.string.cancel),
        fileProvider = { getZippedLogs().toFile() },
        mimeType = stringResource(R.string.application_x_zip),
        onDismiss = onDismiss
    )
}

private fun getZippedLogs(): Path {
    return DittoLogManager.createLogsZip()
}





