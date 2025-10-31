package live.ditto.tools.diskusage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
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
    val observerHandle = remember(ditto.diskUsage, viewModel) {
        ditto.diskUsage.observe { diskUsageItem ->
            val children = diskUsageItem.childItems ?: return@observe
            viewModel.updateDiskUsage(
                path = ditto.persistenceDirectory,
                records = children,
            )
        }
    }
    DisposableEffect(key1 = observerHandle) {
        onDispose {
            observerHandle.close()
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