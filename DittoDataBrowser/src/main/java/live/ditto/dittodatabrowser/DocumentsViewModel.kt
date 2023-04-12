package live.ditto.dittodatabrowser

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class DocumentsViewModel(private val collectionName: String, isStandAlone: Boolean): ViewModel() {

    val docsList: MutableLiveData<MutableList<Document>> = MutableLiveData<MutableList<Document>>(mutableListOf())
    var docProperties: MutableLiveData<List<String>> = MutableLiveData(emptyList())
    var selectedDoc = MutableLiveData<Document>()

    val subscription = if (isStandAlone) DittoHandler.ditto.store.collection(collectionName).findAll().limit(1000).subscribe() else null
    private var liveQuery = DittoHandler.ditto.store.collection(collectionName).findAll().limit(1000).observeLocal { docs, _ ->

        docsList.value?.clear()
        for(doc in docs) {
            this.docProperties.postValue(doc.value.keys.map{it}.sorted())

            val docValues = mutableMapOf<String, Any?>()
            for((key, value) in doc.value) {
                docValues[key] = value
            }
            docsList.value?.add(Document(doc.id.toString(), docValues))
        }
    }

    private fun findAllliveQuery() {
        this.liveQuery =  DittoHandler.ditto.store.collection(collectionName).findAll().limit(1000).observeLocal { docs, _ ->
            docsList.value?.clear()
            for(doc in docs) {
                this.docProperties.postValue(doc.value.keys.map{it}.sorted())

                val docValues = mutableMapOf<String, Any?>()
                for((key, value) in doc.value) {
                    docValues[key] = value
                }
                docsList.value?.add(Document(doc.id.toString(), docValues))
            }
        }
    }

    private fun findWithFilterLiveQuery(queryString: String) {
        this.liveQuery =  DittoHandler.ditto.store.collection(collectionName).find(queryString).limit(1000).observeLocal { docs, _ ->
            docsList.value?.clear()

            for(doc in docs) {
                this.docProperties.postValue(doc.value.keys.map{it}.sorted())

                val docValues = mutableMapOf<String, Any?>()
                for((key, value) in doc.value) {
                    docValues[key] = value
                }
                docsList.value?.add(Document(doc.id.toString(), docValues))
            }
        }
    }

    fun filterDocs(queryString: String) {
        liveQuery.close()

        if(queryString.isEmpty()) {
            findAllliveQuery()
        }
        else {
            findWithFilterLiveQuery(queryString)
        }
    }

    class MyViewModelFactory(private val collectionName: String, private val isStandAlone: Boolean) :
        ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel> create(modelClass: Class<T>): T = DocumentsViewModel(collectionName, isStandAlone) as T
    }
}