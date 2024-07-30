package com.example.dittodiskusage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import live.ditto.Ditto
import live.ditto.dittodiskusage.R
import live.ditto.exporter.ExportDialog
import java.io.File


private val ScreenTypography = Typography()

@Composable
fun DiskUsageView(
    ditto: Ditto,
    viewModel: DiskUsageViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    DiskUsageView(
        uiState = uiState,
        fileProvider = {
            viewModel.exportButtonOnClick(ditto)
        },
    )
}

@Composable
private fun DiskUsageView(
    uiState: DiskUsageState,
    fileProvider: suspend () -> File,
) {
    var isExportDialogOpen by remember { mutableStateOf(false) }

    Surface {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Spacer(modifier = Modifier.height(24.dp))

            Row {
                Text(
                    text = stringResource(R.string.disk_usage),
                    style = ScreenTypography.headlineLarge,
                )

                Spacer(modifier = Modifier.weight(1f))

                IconButton(
                    enabled = !isExportDialogOpen,
                    onClick = { isExportDialogOpen = true },
                    modifier = Modifier
                        .widthIn(min = 60.dp) // Ensure a minimum width for the button
                        .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
                        .padding(horizontal = 5.dp)
                ) {
                    Text(
                        text = "Export"
                    )
                }
            }

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

            if (isExportDialogOpen) {
                ExportDialog(
                    title = stringResource(R.string.export_ditto_directory),
                    text = stringResource(R.string.do_you_want_to_export_ditto_directory),
                    confirmText = stringResource(R.string.export),
                    cancelText = stringResource(R.string.cancel),
                    fileProvider = fileProvider,
                    mimeType = stringResource(R.string.application_x_zip),
                    onDismiss = { isExportDialogOpen = false }
                )
            }
        }
    }
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
                ),
            ),
            fileProvider = { File("") }
        )
    }
}