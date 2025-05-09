package live.ditto.tools.diskusage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * Wrapper composable function for `DiskUsageView`.
 */
@Composable
fun DiskUsageScreen() {
    val ditto = DittoHandler.ditto

    val viewModel = viewModel<DiskUsageViewModel>()
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
        viewModel = viewModel
    )
}

data class DiskUsage(
    val relativePath: String = "ditto/ditto_store",
    val sizeInBytes: Int = 0,
    val size: String = "Calculating...",
)