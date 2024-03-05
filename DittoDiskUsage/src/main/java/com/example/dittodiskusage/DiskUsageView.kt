package com.example.dittodiskusage

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import live.ditto.DiskUsageItem
import live.ditto.dittodiskusage.R
import java.text.DecimalFormat
import kotlin.math.log10
import kotlin.math.pow

private val ScreenTypography = Typography()

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

class DiskUsageViewModel : ViewModel() {
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
    viewModel: DiskUsageViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    DiskUsageView(uiState = uiState)
}

@Composable
private fun Item(
    leftText: String,
    rightText: String,
    style: TextStyle,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = leftText,
            style = style
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = rightText,
            style = style
        )
    }
}

@Composable
private fun DiskUsageView(uiState: DiskUsageState) {
    Surface {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.disk_usage),
                style = ScreenTypography.headlineLarge,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(24.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(uiState.children) { diskUsage ->
                    Item(
                        leftText = diskUsage.relativePath,
                        rightText = diskUsage.size,
                        style = ScreenTypography.bodyLarge
                    )
                }

                item {
                    Divider(Modifier.padding(vertical = 8.dp))
                }

                item {
                    Item(
                        leftText = stringResource(R.string.total),
                        rightText = uiState.totalSize,
                        style = ScreenTypography.titleLarge
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun ItemPreview() {
    Surface(color = MaterialTheme.colorScheme.background) {
        Item(
            leftText = "foo/bar",
            rightText = "400",
            style = ScreenTypography.bodyLarge
        )
    }
}


@Preview
@Composable
private fun ItemTotalPreview() {
    Surface(color = MaterialTheme.colorScheme.background) {
        Item(
            leftText = "Total",
            rightText = "400",
            style = ScreenTypography.titleLarge
        )
    }
}

@Preview
@Composable
private fun DiskUsageViewPreview() {
    Surface(color = MaterialTheme.colorScheme.background) {
        DiskUsageView(
            uiState = DiskUsageState(
                rootPath = "root",
                totalSizeInBytes = 200,
                totalSize = "400",
                children = listOf(
                    DiskUsage(),
                    DiskUsage(size = "200"),
                    DiskUsage(size = "200"),
                    DiskUsage(size = "200"),
                    DiskUsage(size = "200"),
                    DiskUsage(size = "200"),
                )
            )
        )
    }
}