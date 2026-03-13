package live.ditto.tools.databrowser

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import live.ditto.DittoCollection
import live.ditto.DittoSubscription

class CollectionsViewModel: ViewModel() {

    var collections: MutableLiveData<List<DittoCollection>> = MutableLiveData(emptyList())
    private lateinit var subscription: DittoSubscription
    val liveQuery = DittoHandler.ditto.store.collections().observeLocal { collections ->
        this.collections.postValue(collections.collections)
    }

    fun startSubscription() {
        this.subscription = DittoHandler.ditto.store.collections().subscribe()
    }


}