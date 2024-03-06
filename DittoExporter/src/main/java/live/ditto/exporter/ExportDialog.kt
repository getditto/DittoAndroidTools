package live.ditto.exporter

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
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
import androidx.compose.ui.unit.dp
import androidx.core.net.toFile
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream

/**
 * A customized AlertDialog to export the given [file]
 *
 * @param title: Dialog title
 * @param text: Dialog text
 * @param confirmText: Confirm button's text
 * @param cancelText: Cancel button's text
 * @param fileProvider: A callback to produce the file to be exported
 * @param mimeType: The file's mime-type
 * @param onDismiss: Callback to execute when Dialog dismissed
 * @param modifier: Compose Modifier applied to the AlertDialog
 */
@Composable
fun ExportDialog(
    title: String,
    text: String,
    confirmText: String,
    cancelText: String,
    fileProvider: suspend () -> File,
    mimeType: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var file: File? by remember { mutableStateOf(null) }
    val context = LocalContext.current
    val exportLauncher = rememberLauncherForActivityResult(
        CreateDocument(mimeType)
    ) { uri ->
        uri?.let {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                FileInputStream(file).copyTo(outputStream)
            }
        }

        onDismiss()
    }

    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = {
            Text(title)
        },
        text = {
            Text(text)
        },
        confirmButton = {
            Button(
                onClick = {
                    coroutineScope.launch {
                        isLoading = true
                        file = fileProvider()
                        file?.let {
                            exportLauncher.launch(it.absolutePath)
                        }
                        isLoading = false
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


