package com.example.dittodiskusage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController

/**
 * Wrapper composable function for `DiskUsageView`.
 */
@Composable
fun DiskUsageScreen(navController: NavHostController) {
    val ditto = DittoHandler.ditto
    val lifecycleOwner = LocalLifecycleOwner.current

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
    DiskUsageView(viewModel = viewModel, navController = navController)
}

