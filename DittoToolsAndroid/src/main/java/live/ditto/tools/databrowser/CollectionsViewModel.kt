package live.ditto.tools.databrowser

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class CollectionsViewModel : ViewModel() {

    companion object {
        private const val POLL_INTERVAL_MS = 2000L
    }

    var collections: MutableLiveData<List<String>> = MutableLiveData(emptyList())
    var isStandAlone = false

    // system:collections is a virtual collection that doesn't support registerObserver,
    // so we poll periodically with store.execute().
    private val pollJob = viewModelScope.launch(Dispatchers.IO) {
        while (isActive) {
            try {
                val result = DittoHandler.ditto.store.execute(
                    "SELECT name FROM system:collections"
                )
                val names = result.items.mapNotNull { item ->
                    item.value["name"].stringOrNull
                }
                collections.postValue(names)
            } catch (e: Exception) {
                // Query may fail if store is not yet ready; retry on next poll
            }
            delay(POLL_INTERVAL_MS)
        }
    }

    fun startSubscription() {
        // "Stand alone" mode: subscribe to all discovered collections so data syncs in
        isStandAlone = true
    }
}
