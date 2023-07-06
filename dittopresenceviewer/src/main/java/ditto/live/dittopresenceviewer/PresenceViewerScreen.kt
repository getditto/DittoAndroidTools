package ditto.live.dittopresenceviewer

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import live.ditto.Ditto
import live.ditto.DittoPresenceObserver


@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun DittoPresenceViewer() {
    val viewModel: PresenceViewModel = viewModel()
    val presenceJSON = viewModel.presenceJSON

    val viewModelPresence = viewModel<VisJSWebViewViewModel>()
    val helper = remember { VisJSWebViewHelper(viewModelPresence) }

    Scaffold(modifier = Modifier.fillMaxSize()) {
        VisJSWebView()
    }
//    VisJSWebView()

    LaunchedEffect(presenceJSON.value) {
        Log.d("PresenceViewerFragment", "--- V3 Peers updated")

        helper.updateNetwork(presenceJSON.value) {}
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycle = lifecycleOwner.lifecycle

    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> helper.updateAppState(false)
                Lifecycle.Event.ON_PAUSE -> helper.updateAppState(true)
                else -> {}
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }
}

class PresenceViewModel : ViewModel() {
    var ditto: Ditto? = null
        set(value) {
            if (value === ditto) {
                return
            }

            peersObserver?.close()
            value?.let { ditto ->
                peersObserver = ditto.presence.observe { graph ->
                    presenceJSON.value = graph.json()
                }
            }
            field = value
        }

    private var peersObserver: DittoPresenceObserver? = null

    val presenceJSON = mutableStateOf("")
}


