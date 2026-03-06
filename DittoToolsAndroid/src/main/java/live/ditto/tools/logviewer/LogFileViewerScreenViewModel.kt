package live.ditto.tools.logviewer

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.ditto.kotlin.Ditto
import live.ditto.tools.utils.LogUtils
import java.io.File
import kotlin.collections.plus
import kotlin.collections.takeLast

class LogFileViewerScreenViewModel(val ditto : Ditto, val filesDir: File) : ViewModel(){

    private val dittoLogUtils = LogUtils(filesDir = filesDir, ditto = ditto)
    private var tailJob: Job? = null
    private var _expandedInnerMenu by  mutableStateOf(false)
    val isExpandedInnerMenu : Boolean
        get() = _expandedInnerMenu
    fun setExpandedInnerMenu(value: Boolean){
        _expandedInnerMenu = value
    }
    private var _isMenuFilter by mutableStateOf(false)
    val isMenuFilter : Boolean
        get() = _isMenuFilter
    fun setMenuFilter(value: Boolean){
        _isMenuFilter = value
    }

    private val _logFileList = mutableStateOf<List<String>>(emptyList())
    val logFileList = _logFileList

    private val _reverse = MutableStateFlow(false)
    val reverse: StateFlow<Boolean> = _reverse

    private val _tail = MutableStateFlow(false)
    val tail: StateFlow<Boolean> = _tail

    //Search
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    //Log Lines
    private val _allLines = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val filteredLines: StateFlow<List<Map<String, Any>>> =
        combine(_allLines, _query){ lines, q ->
            if (q.isBlank()){
                lines
            }else if(isMenuFilter){
                lines.filter { it ->
                    if (q == "FATAL"){
                        (it["level"] as String).contains(q, ignoreCase = true)
                                || (it["level"] as String).contains("PANIC", ignoreCase = true)
                    }else{
                        (it["level"] as String).contains(q)
                    }
                }
            }else{
                val trimmedQuery = q.trim()
                lines
                    .filter {
                        it.message().contains(trimmedQuery, ignoreCase = true)
                            || (it["level"] as String).contains(trimmedQuery, ignoreCase = true)
                            || (it["target"] as String).contains(trimmedQuery, ignoreCase = true)
                    }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private fun Map<String, Any>.message(): String =
        when (val v = this["message"]) {
            is String -> v
            is Long -> v.toString()
            is Int -> v.toString()
            else -> ""
    }


    init {
        setup()
    }

    private fun getLogFileNameList() : Job {
        return viewModelScope.launch {
            val list : MutableList<String> = mutableListOf<String>()
            dittoLogUtils.getLogFileList().forEach { file ->
                list.add(file.name)
            }
            _logFileList.value = list
        }
    }

    private fun loadLogFile(fileName : String) : Job {
        return viewModelScope.launch{
            _allLines.value = dittoLogUtils.readLogFile(fileName = fileName)
        }
    }

    private fun setup(){
        viewModelScope.launch(Dispatchers.IO) {
            val fileNamesJob = getLogFileNameList()
            fileNamesJob.join()
            if (logFileList.value.isNotEmpty()) {
                loadLogFile(logFileList.value[0])
            }
        }
    }

    fun onQueryChange(newQuery: String){
        _query.value = newQuery
    }

    fun toggleReverse(){
        _reverse.value = !_reverse.value
    }

    fun toggleTail(){
        _tail.value = !_tail.value

        if (_tail.value){
            startTrailing()
        }else{
            stopTrailing()
        }
    }

    /**
     * Trails log file and takes the last 5000 lines
     */
    private fun startTrailing(){
        tailJob?.cancel()
        tailJob = viewModelScope.launch(Dispatchers.IO) {
            dittoLogUtils.tailLogFile().collect { newLine ->
                _allLines.update { old ->
                    val updatedLine = dittoLogUtils.parseLogLine(newLine)
                    updatedLine?.let { (old + it).takeLast(5000) } ?: old
                }
            }
        }
    }

    /**
     * Stops trailing log job
     */
    private fun stopTrailing(){
        tailJob?.cancel()
        tailJob = null
    }

    override fun onCleared() {
        stopTrailing()
        super.onCleared()
    }
}

class LogFileScreenViewModelFactory(
    private val ditto: Ditto,
    private val filesDir: File
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LogFileViewerScreenViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LogFileViewerScreenViewModel(ditto, filesDir) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}