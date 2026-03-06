package live.ditto.tools.utils

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.longOrNull
import com.ditto.kotlin.Ditto
import com.ditto.kotlin.serialization.DittoCborSerializable
import live.ditto.tools.R
import live.ditto.tools.data.LogConfiguration
import java.io.File
import java.io.RandomAccessFile

class LogUtils(filesDir: File, val ditto: Ditto) {
    private val dittoFileDir = filesDir
    private val dittoLogDir = File("${dittoFileDir.path}/ditto/ditto_logs")
    private val logConfigItems = listOf(MAX_AGE, MAX_SIZE, MAX_FILES_ON_DISK)

    /**
     * Reads log configuration from store
     * @return LogConfiguration
     * */
    suspend fun getLogConfiguration() : LogConfiguration {

        var maxAge: Int = -1
        var maxFilesOnDisk: Int = -1
        var maxSize: Int = -1

        withContext(Dispatchers.IO) {
            logConfigItems.forEach { configItem ->
                val resultItem = ditto.store.execute("SHOW $configItem").items[0]
                val cbor = resultItem.value[configItem] as? DittoCborSerializable
                val intVal = cbor?.longOrNull?.toInt() ?: -1
                when(configItem){
                    MAX_AGE -> maxAge = intVal
                    MAX_SIZE -> maxSize = intVal
                    MAX_FILES_ON_DISK -> maxFilesOnDisk = intVal
                }
            }
        }

        return LogConfiguration(maxAge = maxAge, maxFilesOnDisk = maxFilesOnDisk, maxSize = maxSize)
    }

    /**
     * Log File directory size in bytes
     * */
    fun getLogFileDirSize() : Long{
        var size: Long = 0

        try {
            if(dittoLogDir.exists() && dittoLogDir.isDirectory){
                dittoLogDir.walkTopDown()
                    .filter { it.isFile }
                    .forEach{ size += it.length() }
            }
        }catch (e: Exception){
            Log.e(TAG, "Error getting log directory size", e)
        }

        return size
    }

    /**
     * Returns number of log files currently in directory
     * */
    fun getLogFileCount() : Int{
        var count = 0
        try {
            if(dittoLogDir.exists() && dittoLogDir.isDirectory){
                dittoLogDir.walkTopDown()
                    .filter { it.isFile }
                    .forEach{ _ -> count++}
            }
        }catch (e: Exception){
            Log.e(TAG, "Error getting log directory count", e)
        }
        return count
    }

    suspend fun getLogFileList() : List<File> {
        var result : List<File> = emptyList()
        withContext(Dispatchers.IO){
            try{
                if(dittoLogDir.exists() && dittoLogDir.isDirectory){
                    result = dittoLogDir.listFiles()?.toList() ?: emptyList()
                }else return@withContext
            }catch (e: Exception ){
                Log.e(TAG, "Error getting log files", e)
            }
        }
        return result
    }

    /**
     * Parses log line
     * @param line line that will be parsed
     * @return Map<String, Any> containing parsed log data, or null if parsing fails
     * */
    fun parseLogLine(line: String): Map<String, Any>? {
        return try {
            json.parseToJsonElement(line)
                .jsonObject
                .mapValues { (_, value) ->
                    when (value) {
                        is JsonPrimitive -> {
                            // Try to preserve primitive types
                            value.booleanOrNull
                                ?: value.longOrNull
                                ?: value.doubleOrNull
                                ?: value.content
                        }
                        is JsonObject -> value  // Keep as JsonObject
                        is JsonArray -> value   // Keep as JsonArray
                        else -> value.toString()
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing log line [$line]", e)
            null
        }
    }

    /**
     * Reads MAX_LINES number of lines.
     * @param fileName Name of file used to read.
     * @return List<String> containing log lines parsed.
     * */
    suspend fun readLogFile(fileName: String) : ArrayList<Map<String, Any>> {
        val lines = ArrayList<Map<String, Any>>(minOf(MAX_LINES, 1024))
        withContext(Dispatchers.IO){

            try {
                val bufferedReader = if (dittoLogDir.exists() && dittoLogDir.isDirectory){
                     dittoLogDir.listFiles()
                        ?.filter { file -> file.name == fileName }
                        ?.get(0)?.bufferedReader()
                } else null

                bufferedReader?.useLines { sequence ->
                    sequence.take(MAX_LINES).forEach { line ->
                        val output = parseLogLine(line)
                        output?.let { lines.add(it) }
                    }
                }
            }catch (e: Exception){
                Log.e(TAG, "Error reading log file $e")
            }
        }
        return lines
    }

    /**
     * Checks number of errors contained in log file.
     * Based solely on log level ERROR
     * */
     fun logErrorCount() : Int {
        var count = 0

        try{
            if(dittoLogDir.exists() && dittoLogDir.isDirectory){
                dittoLogDir.listFiles()
                    ?.filter { file -> file.name.endsWith(".log") }
                    ?.get(0)
                    ?.bufferedReader()
                    ?.useLines { sequence ->
                        sequence.forEach { line -> if (line.contains(ERROR_REGEX)) count++ }
                    }
            }
        }catch (e: Exception ){
            Log.e(TAG, "Error getting log files: $e")
        }

        return count
    }

    fun getCurrentLogFile() : File?{
        var result : File? = null
        try{
            if(dittoLogDir.exists() && dittoLogDir.isDirectory){
                result = dittoLogDir.listFiles()
                    ?.firstOrNull { file -> file.name.endsWith(".log") }
            }
        }catch (e: Exception ){
            Log.e(TAG, "Error getting log files: $e")
        }
        return result
    }

    fun tailLogFile(): Flow<String> = flow {
        val file = getCurrentLogFile() ?: return@flow

        RandomAccessFile(file, "r").use { raf ->
            var filePointer = raf.length()
            raf.seek(filePointer)
            while (true) {
                val newLength = file.length()
                if (newLength > filePointer) {
                    raf.seek(filePointer)

                    while (true) {
                        val line = raf.readLine() ?: break
                        emit(line)
                    }

                    filePointer = raf.filePointer
                }
                delay(1000)
            }
        }
    }.flowOn(Dispatchers.IO)

    companion object{
        private const val TAG = "DittoLogUtils"
        private const val MAX_SIZE = "rotating_log_file_max_size_mb"
        private const val MAX_AGE = "rotating_log_file_max_age_h"
        private const val MAX_FILES_ON_DISK = "rotating_log_file_max_files_on_disk"
        private const val MAX_LINES = 5000 //Max log lines to read to protect against a big file

        // Regex pattern that captures "level":"ERROR" and standalone "error" word
        // but ignores metrics like doc_id_filter_error_rate
        private val ERROR_REGEX = Regex(""""level"\s*:\s*"ERROR"|(?<![a-z_])(?i)error(?-i)(?![a-z_])""")

        // Kotlin Serialization JSON configuration
        private val json = Json {
            ignoreUnknownKeys = true
            isLenient = true  // Equivalent to moshiAdapter.lenient()
        }

        fun getBackgroundColor(level: String) : Int{
            return when(level){
                "DEBUG" -> R.color.log_debug
                "INFO" -> R.color.log_info
                "WARN" -> R.color.log_warn
                "ERROR" -> R.color.log_error
                "FATAL", "PANIC" -> R.color.log_fatal
                else -> R.color.log_unknown
            }
        }
    }
}