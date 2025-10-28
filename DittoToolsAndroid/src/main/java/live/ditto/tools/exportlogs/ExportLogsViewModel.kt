package live.ditto.tools.exportlogs

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import live.ditto.Ditto
import live.ditto.DittoError
import live.ditto.DittoLogger
import live.ditto.tools.R

class ExportLogsViewModel : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    private val _uiState = MutableStateFlow<ExportLogsUiState>(ExportLogsUiState.Idle)
    val uiState = _uiState.asStateFlow()

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

    fun onConfirmClicked(ditto: Ditto, context: Context){
        if (_uiState.value is ExportLogsUiState.Error) {
            resetState()
        }
        uploadLogsToPortal(ditto, context)
    }

    fun resetState() {
        _uiState.value = ExportLogsUiState.Idle
    }

    private fun uploadLogsToPortal(ditto: Ditto, context: Context) {
        if (_uiState.value !is ExportLogsUiState.Idle) return
        viewModelScope.launch {
            _uiState.value = ExportLogsUiState.Exporting
            try {
                DittoTools.uploadLogsToPortal(ditto)
                _uiState.value = ExportLogsUiState.Success
            } catch (e: DittoError) {
                _uiState.value = ExportLogsUiState.Error(
                    context.getString(
                        R.string.log_export_failed,
                        e.message
                    ))
            }
        }
    }
}

sealed interface ExportLogsUiState {
    object Idle : ExportLogsUiState
    object Exporting : ExportLogsUiState
    object Success : ExportLogsUiState
    data class Error(val message: String) : ExportLogsUiState
}
