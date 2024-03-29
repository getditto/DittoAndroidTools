package com.example.dittodiskusage

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import live.ditto.DiskUsageItem
import live.ditto.Ditto
import live.ditto.exporter.ZipFolderUseCase
import java.io.File
import java.text.DecimalFormat
import kotlin.math.log10
import kotlin.math.pow

class DiskUsageViewModel(
    private val zipFolderUseCase: ZipFolderUseCase = ZipFolderUseCase(),
    private val dispatcherIO: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    /* Private mutable state */
    private val _uiState = MutableStateFlow(DiskUsageState())

    /* Public immutable state */
    val uiState: StateFlow<DiskUsageState>
        get() = _uiState.asStateFlow()

    private fun getFileSize(size: Int): String {
        if (size <= 0) return "0"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
        return DecimalFormat("#,##0.#").format(
            size / 1024.0.pow(digitGroups.toDouble())
        ) + " " + units[digitGroups]
    }

    // List of 1 for now
    fun updateDiskUsage(path: String, records: List<DiskUsageItem>) {
        var totalSizeInBytes = 0
        val children = mutableListOf<DiskUsage>()
        val sortedRecords = records.sortedBy { it.path }
        for (record in sortedRecords) {
            totalSizeInBytes += record.sizeInBytes
            val du = DiskUsage(
                relativePath = record.path,
                sizeInBytes = record.sizeInBytes,
                size = getFileSize(record.sizeInBytes)
            )
            children.add(du)
        }
        _uiState.update { currentState ->
            currentState.copy(
                rootPath = path,
                totalSizeInBytes = totalSizeInBytes,
                totalSize = getFileSize(totalSizeInBytes),
                children = children,
            )
        }
    }

    suspend fun exportButtonOnClick(ditto: Ditto): File {
        val inputDirectory = File(ditto.persistenceDirectory)
        val outputZipFile = withContext(dispatcherIO) {
            File.createTempFile("ditto_", ".zip")
        }

        zipFolderUseCase(
            inputDirectory = inputDirectory,
            outputZipFile = outputZipFile
        )

        return outputZipFile
    }
}