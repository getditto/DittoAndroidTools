package live.ditto.tools.logviewer

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import live.ditto.Ditto
import live.ditto.tools.utils.LogUtils
import live.ditto.tools.utils.Utils
import java.io.File

class LogDetailsScreenViewModel(val ditto : Ditto, val filesDir: File) : ViewModel(){

    private val dittoLogUtils = LogUtils(
        ditto = ditto,
        filesDir = filesDir
    )

    private val _logConfiguration = mutableStateOf<AnnotatedString>(AnnotatedString(""))
    val logConfiguration = _logConfiguration

    private val _logDirectoryInfo = mutableStateOf<AnnotatedString>(AnnotatedString(""))
    val logDirectoryInfo = _logDirectoryInfo

    fun getLogConfigurationInfo(){
        viewModelScope.launch(Dispatchers.IO) {
            val config = dittoLogUtils.getLogConfiguration()

            _logConfiguration.value = buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)){
                    append("Max File Age: ")
                }
                append("${config.maxAge} Hours")
                append("\n")

                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)){
                    append("Max File Size: ")
                }
                append("${config.maxSize} MB")
                append("\n")

                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)){
                    append("Max Files On Disk: ")
                }
                append("${config.maxFilesOnDisk}")
            }
        }
    }

    fun getLogDirInfo(){

        viewModelScope.launch(Dispatchers.IO) {
            val fileSize = Utils.formatFileSize(dittoLogUtils.getLogFileDirSize())

            _logDirectoryInfo.value = buildAnnotatedString {

                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("Log Directory Size: ")
                }
                append("$fileSize\n")

                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("Log File Count: ")
                }
                append("${dittoLogUtils.getLogFileCount()}\n")

                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("Current Log File Error Count: ")
                }
                append("${dittoLogUtils.logErrorCount()}")
            }
        }
    }
}

class LogDetailsScreenViewModelFactory(
    private val ditto: Ditto,
    private val filesDir: File
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LogDetailsScreenViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LogDetailsScreenViewModel(ditto, filesDir = filesDir) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}