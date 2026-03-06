package live.ditto.tools.diskusage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import java.io.File

/**
 * Wrapper composable function for `DiskUsageView`.
 */
@Composable
fun DiskUsageScreen(
    onExport: ((File) -> Unit)? = null
) {
    val ditto = DittoHandler.ditto

    val viewModel = viewModel<DiskUsageViewModel> {
        DiskUsageViewModel(onExport = onExport)
    }

    LaunchedEffect(ditto, viewModel) {
        ditto.diskUsage.observe().collect { diskUsageItem ->
            val children = diskUsageItem.childItems ?: return@collect
            viewModel.updateDiskUsage(
                path = ditto.absolutePersistenceDirectory,
                records = children,
            )
        }
    }

    DiskUsageView(
        ditto = ditto,
        viewModel = viewModel,
        onExport = onExport
    )
}

data class DiskUsage(
    val relativePath: String = "ditto/ditto_store",
    val sizeInBytes: Int = 0,
    val size: String = "Calculating...",
)
