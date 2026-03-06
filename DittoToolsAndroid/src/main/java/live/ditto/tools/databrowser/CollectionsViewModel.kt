package live.ditto.tools.databrowser

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ditto.kotlin.DittoStoreObserver
import com.ditto.kotlin.DittoSyncSubscription

class CollectionsViewModel : ViewModel() {

    var collections: MutableLiveData<List<String>> = MutableLiveData(emptyList())
    var isStandAlone = false

    private var subscription: DittoSyncSubscription? = null

    private val liveQuery: DittoStoreObserver = DittoHandler.ditto.store.registerObserver(
        "SELECT * FROM __collections"
    ) { result ->
        val names = result.items.mapNotNull { item ->
            item.value["name"].stringOrNull
        }
        this.collections.postValue(names)
    }

    fun startSubscription() {
        subscription = DittoHandler.ditto.sync.registerSubscription("SELECT * FROM __collections")
        isStandAlone = true
    }

    override fun onCleared() {
        super.onCleared()
        liveQuery.close()
        subscription?.close()
    }
}
