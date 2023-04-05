package com.example.dittodiskusage

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import live.ditto.DiskUsageItem
import java.text.DecimalFormat
import kotlin.math.log10
import kotlin.math.pow

data class DiskUsage(
    val relativePath: String = "ditto/ditto_store",
    val sizeInBytes: Int = 0,
    val size: String = "Calculating...",
)

data class DiskUsageState(
    val rootPath: String = "ditto",
    val totalSizeInBytes: Int = 0,
    val totalSize: String = "Calculating...",
    val children: List<DiskUsage> = listOf(DiskUsage()),
)

class DiskUsageViewModel: ViewModel() {
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
}

@Composable
fun DiskUsageView(
    viewModel: DiskUsageViewModel = viewModel(),
    navController: NavController,
    ) {
    val uiState by viewModel.uiState.collectAsState()

    Surface {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "Disk Usage",
                fontSize = 24.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(uiState.children) { child ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = child.relativePath,
                                fontSize = 18.sp
                            )
                        }

                        Column {
                            Text(
                                text = child.size,
                                fontSize = 18.sp,
                                textAlign = TextAlign.End
                            )
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Total",
                                fontSize = 18.sp
                            )
                        }

                        Column {
                            Text(
                                text = uiState.totalSize,
                                fontSize = 18.sp,
                                textAlign = TextAlign.End
                            )
                        }
                    }
                }
            }

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                onClick = { navController.popBackStack() }
            ) {
                Text(
                    text = "Close",
                    fontSize = 18.sp
                )
            }
        }
    }
}
