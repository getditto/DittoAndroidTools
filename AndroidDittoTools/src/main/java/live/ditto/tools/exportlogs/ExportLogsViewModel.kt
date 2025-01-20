package live.ditto.tools.exportlogs

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import live.ditto.DittoError
import live.ditto.DittoLogger
import live.ditto.androidtools.R

class ExportLogsViewModel : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    fun exportLogs(filePath: String, context: Context, onDismiss: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            try {
                val sizeWritten = DittoLogger.exportToFile(filePath)

                val sizeWrittenInKB = (sizeWritten / 1024u).toInt()
                val toastText = context.getString(R.string.exported_file_size_kb, sizeWrittenInKB)
                Toast.makeText(context, toastText, Toast.LENGTH_LONG).show()
            } catch (e: DittoError.IoError) {
                Log.e("DittoTools LogExporter", e.reason.toString())
                Toast.makeText(context,
                    context.getString(R.string.error_exporting_logs, e.message), Toast.LENGTH_LONG).show()
            } finally {
                isLoading = false
                onDismiss()
            }
        }
    }
}
