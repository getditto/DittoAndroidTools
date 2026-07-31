package com.ditto.tools.databrowser

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ditto.kotlin.DittoStoreObserver

internal class CollectionsViewModel : ViewModel() {

    var collections: MutableLiveData<List<String>> = MutableLiveData(emptyList())

    // The Data Browser intentionally observes only the local store. The host
    // application owns its sync subscriptions.
    private val observer: DittoStoreObserver = DittoHandler.ditto.store.registerObserver(
        "SELECT name FROM __collections"
    ) { result ->
        val names = result.items.mapNotNull { item ->
            item.value["name"].stringOrNull
        }
        collections.postValue(names)
    }

    override fun onCleared() {
        super.onCleared()
        observer.close()
    }
}
