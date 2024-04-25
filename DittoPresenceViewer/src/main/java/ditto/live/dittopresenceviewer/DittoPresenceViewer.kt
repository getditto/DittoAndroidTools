package ditto.live.dittopresenceviewer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.CoroutineScope
import live.ditto.Ditto

@Composable
fun DittoPresenceViewer(
    modifier: Modifier = Modifier,
    ditto: Ditto,
    viewModel: PresenceViewModel = PresenceViewModel(ditto)
) {

    val coroutineScope: CoroutineScope = rememberCoroutineScope()
    val graphJson by viewModel.graphJson.collectAsState(initial = null)

    AndroidView(
        factory = { context ->
            DittoPresenceViewerWebView(
                context = context,
                coroutineScope = coroutineScope
            )
        },
        update = { view ->
            graphJson?.let {
                view.setJson(it)
            }
        },
        modifier = modifier
    )
}
