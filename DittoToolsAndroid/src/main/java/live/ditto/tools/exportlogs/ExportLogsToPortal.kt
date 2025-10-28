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
            Text("Logs will be exported to Portal for appID: ${ditto.appId}")
        },
        confirmButton = {
            Button(
                enabled = !isExporting,
                onClick = {
                    isExporting = true
                    scope.launch {
                        try {
                            val peerKey = ditto.presence.graph.localPeer.peerKeyString
                            val formatter = DateTimeFormatterBuilder()
                                .append(ISODateTimeFormat.dateHourMinuteSecond())
                                .appendTimeZoneOffset(null, true, 2, 2)
                                .toFormatter()
                            val currentTime = formatter.print(DateTime.now(DateTimeZone.UTC))
                            val query = """UPDATE __small_peer_info
                            SET log_requests.device_logs.requested_at = :currentTime
                            WHERE _id = :peerKey""".trimIndent()
                            ditto.store.execute(
                                query = query,
                                arguments = mapOf(
                                    "currentTime" to currentTime, "peerKey" to peerKey
                                )
                            )
                            delay(3000)
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
                            text = "Exporting..."
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
