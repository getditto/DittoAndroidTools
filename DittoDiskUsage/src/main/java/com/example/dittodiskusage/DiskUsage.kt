package com.example.dittodiskusage

import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController

/**
 * Wrapper composable function for `DiskUsageView`.
 */
@Composable
fun DiskUsageScreen(navController: NavHostController? = null) {
//    Text("This is the Disk Usage Screen")
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

    DiskUsageView(viewModel = viewModel, onClose = {
        navController?.popBackStack()
    })
}

