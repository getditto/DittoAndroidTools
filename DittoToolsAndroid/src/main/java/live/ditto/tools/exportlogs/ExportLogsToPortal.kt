package live.ditto.tools.exportlogs

import android.util.Log
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import live.ditto.Ditto
import live.ditto.DittoError
import live.ditto.tools.R
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormatterBuilder
import org.joda.time.format.ISODateTimeFormat

@Composable
fun ExportLogsToPortal(
    ditto: Ditto,
    onDismiss: () -> Unit
) {
    val title = stringResource(R.string.export_logs_to_portal_tool_label)

    val confirmText = stringResource(R.string.export)
    val cancelText = stringResource(R.string.cancel)

    val scope = rememberCoroutineScope()

    var isExporting by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(title)
        },
        text = {
            Text(
                text = stringResource(
                    R.string.logs_will_be_exported_to_portal_for_appid,
                    ditto.appId ?: "unknown"
                )
            )
        },
        confirmButton = {
            Button(
                enabled = !isExporting,
                onClick = {
                    isExporting = true
                    scope.launch {
                        try {
                            DittoTools.requestLogExport(ditto)
                            onDismiss()
                        } catch (e: DittoError) {
                            Log.d("ExportLogsToPortal", "ToolsViewerNavHost: $e")
                            isExporting = false
                        }
                    }
                }
            ) {
                Row {
                    if (isExporting) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            modifier = Modifier.align(Alignment.CenterVertically),
                            text = stringResource(R.string.exporting)
                        )
                    } else {
                        Text(confirmText)
                    }
                }
            }
        },
        dismissButton = {
            Button(
                enabled = !isExporting,
                onClick = onDismiss,
            ) {
                Text(cancelText)
            }
        }
    )
}
