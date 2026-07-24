package com.ditto.tools.databrowser

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ditto.kotlin.DittoStoreObserver
import com.ditto.kotlin.DittoSyncSubscription

class CollectionsViewModel : ViewModel() {

    var collections: MutableLiveData<List<String>> = MutableLiveData(emptyList())

    private val subscriptions = mutableMapOf<String, DittoSyncSubscription>()
    private var collectionsSubscription: DittoSyncSubscription? = null

    private val observer: DittoStoreObserver = DittoHandler.ditto.store.registerObserver(
        "SELECT name FROM __collections"
    ) { result ->
        val names = result.items.mapNotNull { item ->
            item.value["name"].stringOrNull
        }
        collections.postValue(names)
        if (collectionsSubscription != null) {
            subscribeToCollections(names)
        }
    }

    fun startSubscription() {
        if (collectionsSubscription == null) {
            collectionsSubscription = DittoHandler.ditto.sync.registerSubscription(
                "SELECT * FROM __collections"
            )
        }
        // Subscribe to all currently known collections
        collections.value?.let { subscribeToCollections(it) }
    }

    private fun subscribeToCollections(names: List<String>) {
        for (name in names) {
            if (name !in subscriptions) {
                subscriptions[name] = DittoHandler.ditto.sync.registerSubscription(
                    "SELECT * FROM `$name`"
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        observer.close()
        collectionsSubscription?.close()
        subscriptions.values.forEach { it.close() }
    }
}
