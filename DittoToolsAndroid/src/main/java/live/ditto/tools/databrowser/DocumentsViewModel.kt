package live.ditto.tools.databrowser

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class DocumentsViewModel(private val collectionName: String, isStandAlone: Boolean): ViewModel() {

    val docsList: MutableLiveData<MutableList<Document>> = MutableLiveData<MutableList<Document>>(mutableListOf())
    var docProperties: MutableLiveData<List<String>> = MutableLiveData(emptyList())
    var selectedDoc = MutableLiveData<Document>()
    var errorMessage = MutableLiveData<String?>()

    // Store all documents for client-side filtering
    private var allDocuments: MutableList<Document> = mutableListOf()
    private var currentFilter: String = ""
    private var isDQLMode: Boolean = false

    val subscription = if (isStandAlone) DittoHandler.ditto.store.collection(collectionName).findAll().limit(50000).subscribe() else null
    private var liveQuery = DittoHandler.ditto.store.collection(collectionName).findAll().limit(50000).observeLocal { docs, _ ->

        val newDocsList = mutableListOf<Document>()
        for(doc in docs) {
            this.docProperties.postValue(doc.value.keys.map{it}.sorted())

            val docValues = mutableMapOf<String, Any?>()
            for((key, value) in doc.value) {
                docValues[key] = value
            }
            newDocsList.add(Document(doc.id.toString(), docValues))
        }
        allDocuments = newDocsList
        applyFilter()
    }

    private fun findAllLiveQuery() {
        this.liveQuery =  DittoHandler.ditto.store.collection(collectionName).findAll().limit(50000).observeLocal { docs, _ ->
            val newDocsList = mutableListOf<Document>()
            for(doc in docs) {
                this.docProperties.postValue(doc.value.keys.map{it}.sorted())

                val docValues = mutableMapOf<String, Any?>()
                for((key, value) in doc.value) {
                    docValues[key] = value
                }
                newDocsList.add(Document(doc.id.toString(), docValues))
            }
            allDocuments = newDocsList
            applyFilter()
        }
    }

    private fun findWithFilterLiveQuery(queryString: String) {
        try {
            errorMessage.postValue(null)
            this.liveQuery =  DittoHandler.ditto.store.collection(collectionName).find(queryString).limit(50000).observeLocal { docs, _ ->
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
        currentFilter = queryString

        if (isDQLQuery(queryString)) {
            // User provided explicit DQL query - use server-side filtering
            liveQuery.close()
            isDQLMode = true
            findWithFilterLiveQuery(queryString)
        } else {
            // Simple text search - use client-side filtering
            if (isDQLMode) {
                // Switching from DQL mode back to simple search
                // Need to restart the findAll query
                liveQuery.close()
                isDQLMode = false
                findAllLiveQuery()
            } else {
                // Already in simple mode, just filter the existing data
                applyFilter()
            }
        }
    }

    private fun applyFilter() {
        val filtered = if (currentFilter.isEmpty()) {
            allDocuments
        } else {
            // Filter documents where ID contains the search text (case-insensitive)
            allDocuments.filter { doc ->
                doc.id.contains(currentFilter, ignoreCase = true)
            }.toMutableList()
        }
        docsList.postValue(filtered)
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