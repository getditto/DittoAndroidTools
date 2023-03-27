package live.ditto.dittodatabrowser

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import live.ditto.DittoCollection
import live.ditto.DittoSubscription

class CollectionsViewModel: ViewModel() {

    var collections: MutableLiveData<List<DittoCollection>> = MutableLiveData(emptyList())
    var isStandAlone = false

    private lateinit var subscription: DittoSubscription
    val liveQuery = DittoHandler.ditto.store.collections().observeLocal { collections ->
        this.collections.postValue(collections.collections)
    }

    fun startSubscription() {
       this.subscription = DittoHandler.ditto.store.collections().subscribe()
        isStandAlone = true
    }


}