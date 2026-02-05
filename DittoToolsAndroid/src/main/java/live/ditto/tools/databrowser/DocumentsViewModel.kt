package live.ditto.tools.databrowser

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class DocumentsViewModel(private val collectionName: String, isStandAlone: Boolean): ViewModel() {

    val docsList: MutableLiveData<MutableList<Document>> = MutableLiveData<MutableList<Document>>(mutableListOf())
    var docProperties: MutableLiveData<List<String>> = MutableLiveData(emptyList())
    var selectedDoc = MutableLiveData<Document>()
    var errorMessage = MutableLiveData<String?>()

    val subscription = if (isStandAlone) DittoHandler.ditto.store.collection(collectionName).findAll().limit(1000).subscribe() else null
    private var liveQuery = DittoHandler.ditto.store.collection(collectionName).findAll().limit(1000).observeLocal { docs, _ ->

        val newDocsList = mutableListOf<Document>()
        for(doc in docs) {
            this.docProperties.postValue(doc.value.keys.map{it}.sorted())

            val docValues = mutableMapOf<String, Any?>()
            for((key, value) in doc.value) {
                docValues[key] = value
            }
            newDocsList.add(Document(doc.id.toString(), docValues))
        }
        docsList.postValue(newDocsList)
    }

    private fun findAllLiveQuery() {
        this.liveQuery =  DittoHandler.ditto.store.collection(collectionName).findAll().limit(1000).observeLocal { docs, _ ->
            val newDocsList = mutableListOf<Document>()
            for(doc in docs) {
                this.docProperties.postValue(doc.value.keys.map{it}.sorted())

                val docValues = mutableMapOf<String, Any?>()
                for((key, value) in doc.value) {
                    docValues[key] = value
                }
                newDocsList.add(Document(doc.id.toString(), docValues))
            }
            docsList.postValue(newDocsList)
        }
    }

    private fun findWithFilterLiveQuery(queryString: String) {
        try {
            errorMessage.postValue(null)
            this.liveQuery =  DittoHandler.ditto.store.collection(collectionName).find(queryString).limit(1000).observeLocal { docs, _ ->
                val newDocsList = mutableListOf<Document>()

                for(doc in docs) {
                    this.docProperties.postValue(doc.value.keys.map{it}.sorted())

                    val docValues = mutableMapOf<String, Any?>()
                    for((key, value) in doc.value) {
                        docValues[key] = value
                    }
                    newDocsList.add(Document(doc.id.toString(), docValues))
                }
                docsList.postValue(newDocsList)
            }
        } catch (e: Exception) {
            errorMessage.postValue("Invalid DQL query: ${e.message}")
            docsList.postValue(mutableListOf())
        }
    }

    fun filterDocs(queryString: String) {
        liveQuery.close()

        if(queryString.isEmpty()) {
            findAllLiveQuery()
        }
        else {
            // Auto-detect if it's a DQL query or simple ID search
            val actualQuery = if (isDQLQuery(queryString)) {
                queryString
            } else {
                // Simple ID search - wrap in DQL syntax
                "_id CONTAINS \"$queryString\""
            }
            findWithFilterLiveQuery(actualQuery)
        }
    }

    private fun isDQLQuery(text: String): Boolean {
        // Check if the text contains DQL operators
        val dqlOperators = listOf("==", "!=", "CONTAINS", "contains", ">", "<", ">=", "<=", "AND", "and", "OR", "or", "IN", "in")
        return dqlOperators.any { text.contains(it) }
    }

    class MyViewModelFactory(private val collectionName: String, private val isStandAlone: Boolean) :
        ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel> create(modelClass: Class<T>): T = DocumentsViewModel(collectionName, isStandAlone) as T
    }
}