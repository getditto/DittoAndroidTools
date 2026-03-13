package live.ditto.tools.databrowser

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ditto.kotlin.DittoSyncSubscription
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

    private val subscriptions = mutableMapOf<String, DittoSyncSubscription>()

    // system:collections is a virtual collection that doesn't support registerObserver,
    // so we poll periodically with store.execute().
    private val pollJob = viewModelScope.launch(Dispatchers.IO) {
        while (isActive) {
            try {
                val result = DittoHandler.ditto.store.executeRaw(
                    "SELECT name FROM system:collections"
                )
                val names = result.items.mapNotNull { item ->
                    item.value["name"].stringOrNull
                }
                collections.postValue(names)
                if (isStandAlone) {
                    subscribeToCollections(names)
                }
            } catch (e: Exception) {
                // Query may fail if store is not yet ready; retry on next poll
            }
            delay(POLL_INTERVAL_MS)
        }
    }

    fun startSubscription() {
        isStandAlone = true
        // Subscribe to all currently known collections
        collections.value?.let { subscribeToCollections(it) }
    }

    private fun subscribeToCollections(names: List<String>) {
        for (name in names) {
            if (name !in subscriptions) {
                subscriptions[name] = DittoHandler.ditto.sync.registerSubscription(
                    "SELECT * FROM $name"
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        subscriptions.values.forEach { it.close() }
    }
}
