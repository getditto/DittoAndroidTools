package live.ditto.tools.databrowser

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class DocumentsViewModel(private val collectionName: String): ViewModel() {

    val docsList: MutableLiveData<MutableList<Document>> = MutableLiveData<MutableList<Document>>(mutableListOf())
    val docProperties: MutableLiveData<List<String>> = MutableLiveData(emptyList())
    val selectedDoc = MutableLiveData<Document>()
    val errorMessage = MutableLiveData<String?>()

    // Store all documents for client-side filtering
    private var allDocuments: MutableList<Document> = mutableListOf()
    private var currentFilter: String = ""
    private var isDQLMode: Boolean = false

    private var liveQuery = DittoHandler.ditto.store.collection(collectionName).findAll().observeLocal { docs, _ ->
        allDocuments = parseDocs(docs)
        applyFilter()
    }

    private fun findAllLiveQuery() {
        this.liveQuery = DittoHandler.ditto.store.collection(collectionName).findAll().observeLocal { docs, _ ->
            allDocuments = parseDocs(docs)
            applyFilter()
        }
    }

    private fun findWithFilterLiveQuery(queryString: String) {
        try {
            errorMessage.postValue(null)
            this.liveQuery = DittoHandler.ditto.store.collection(collectionName).find(queryString).observeLocal { docs, _ ->
                docsList.postValue(parseDocs(docs))
            }
        } catch (e: Exception) {
            errorMessage.postValue("Invalid DQL query: ${e.message}")
            docsList.postValue(mutableListOf())
        }
    }

    private fun parseDocs(docs: List<live.ditto.DittoDocument>): MutableList<Document> {
        val result = mutableListOf<Document>()
        for (doc in docs) {
            val docValues = mutableMapOf<String, Any?>()
            for ((key, value) in doc.value) {
                docValues[key] = value
            }
            result.add(Document(doc.id.toString(), docValues))
        }
        if (docs.isNotEmpty()) {
            docProperties.postValue(docs.last().value.keys.sorted())
        }
        return result
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
            allDocuments.filter { doc ->
                doc.id.contains(currentFilter, ignoreCase = true)
            }.toMutableList()
        }
        docsList.postValue(filtered)
    }

    private fun isDQLQuery(text: String): Boolean {
        val dqlOperators = listOf("==", "!=", "CONTAINS", "contains", ">", "<", ">=", "<=", "AND", "and", "OR", "or", "IN", "in")
        return dqlOperators.any { text.contains(it) }
    }

    class MyViewModelFactory(private val collectionName: String) :
        ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel> create(modelClass: Class<T>): T = DocumentsViewModel(collectionName) as T
    }
}
