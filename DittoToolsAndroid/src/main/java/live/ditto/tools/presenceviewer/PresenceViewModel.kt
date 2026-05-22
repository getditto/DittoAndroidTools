package live.ditto.tools.presenceviewer

import androidx.lifecycle.ViewModel
import com.ditto.kotlin.Ditto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class PresenceViewModel(
    ditto: Ditto,
    coroutineScope: CoroutineScope
) : ViewModel() {

    val graphJson = ditto.presence.observe()
        .map { it.serializeToJson() }
        .stateIn(
            coroutineScope,
            SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000L),
            null
        )
}
