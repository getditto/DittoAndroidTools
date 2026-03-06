package live.ditto.tools.databrowser

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class CollectionsViewModel : ViewModel() {

    var collections: MutableLiveData<List<String>> = MutableLiveData(emptyList())
    var isStandAlone = false

    private val scope = CoroutineScope(Dispatchers.IO + Job())

    // system:collections is a virtual collection that doesn't support registerObserver,
    // so we poll periodically with store.execute().
    private val pollJob: Job = scope.launch {
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
            delay(2000)
        }
    }

    fun startSubscription() {
        // "Stand alone" mode: subscribe to all discovered collections so data syncs in
        isStandAlone = true
    }

    override fun onCleared() {
        super.onCleared()
        pollJob.cancel()
    }
}
