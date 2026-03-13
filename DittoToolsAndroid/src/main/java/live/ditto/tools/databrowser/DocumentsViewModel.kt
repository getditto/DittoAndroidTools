package live.ditto.tools.databrowser

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ditto.kotlin.DittoStoreObserver
import com.ditto.kotlin.DittoSyncSubscription
import com.ditto.kotlin.serialization.DittoCborSerializable

class DocumentsViewModel(private val collectionName: String, isStandAlone: Boolean) : ViewModel() {

    val docsList: MutableLiveData<MutableList<Document>> = MutableLiveData<MutableList<Document>>(mutableListOf())
    var docProperties: MutableLiveData<List<String>> = MutableLiveData(emptyList())
    var selectedDoc = MutableLiveData<Document>()
    var errorMessage = MutableLiveData<String?>()

    // Store all documents for client-side filtering
    private var allDocuments: MutableList<Document> = mutableListOf()
    private var currentFilter: String = ""
    private var isDQLMode: Boolean = false

    val subscription: DittoSyncSubscription? = if (isStandAlone) {
        DittoHandler.ditto.sync.registerSubscription("SELECT * FROM `$collectionName` LIMIT 1000")
    } else {
        null
    }

    private var observer: DittoStoreObserver = createFindAllObserver()

    private fun createFindAllObserver(): DittoStoreObserver {
        return DittoHandler.ditto.store.registerObserver(
            "SELECT * FROM `$collectionName` LIMIT 1000"
        ) { result ->
            val newDocsList = mutableListOf<Document>()
            for (item in result.items) {
                val valueMap = item.value
                val keys = valueMap.keys.mapNotNull { it.stringOrNull }.sorted()
                this.docProperties.postValue(keys)

                val docValues = mutableMapOf<String, Any?>()
                for (key in keys) {
                    docValues[key] = cborToDisplayValue(valueMap[key])
                }
                val id = cborToDisplayValue(valueMap["_id"])?.toString() ?: ""
                newDocsList.add(Document(id, docValues))
            }
            allDocuments = newDocsList
            applyFilter()
        }
    }

    private fun findAllObserver() {
        observer.close()
        observer = createFindAllObserver()
    }

    private fun findWithFilterObserver(queryString: String) {
        try {
            errorMessage.postValue(null)
            observer.close()
            observer = DittoHandler.ditto.store.registerObserver(
                "SELECT * FROM `$collectionName` WHERE $queryString LIMIT 1000"
            ) { result ->
                val newDocsList = mutableListOf<Document>()
                for (item in result.items) {
                    val valueMap = item.value
                    val keys = valueMap.keys.mapNotNull { it.stringOrNull }.sorted()
                    this.docProperties.postValue(keys)

                    val docValues = mutableMapOf<String, Any?>()
                    for (key in keys) {
                        docValues[key] = cborToDisplayValue(valueMap[key])
                    }
                    val id = cborToDisplayValue(valueMap["_id"])?.toString() ?: ""
                    newDocsList.add(Document(id, docValues))
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
            isDQLMode = true
            findWithFilterObserver(queryString)
        } else {
            // Simple text search - use client-side filtering
            if (isDQLMode) {
                // Switching from DQL mode back to simple search
                isDQLMode = false
                findAllObserver()
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

    override fun onCleared() {
        super.onCleared()
        observer.close()
        subscription?.close()
    }

    private fun cborToDisplayValue(cbor: DittoCborSerializable): Any? {
        return cbor.stringOrNull
            ?: cbor.longOrNull
            ?: cbor.booleanOrNull
            ?: cbor.doubleOrNull
            ?: cbor.floatOrNull
            ?: when {
                cbor.isNull -> null
                cbor is DittoCborSerializable.Dictionary -> cbor.entries.associate { (k, v) ->
                    (k.stringOrNull ?: k.toString()) to cborToDisplayValue(v)
                }
                cbor is DittoCborSerializable.ArrayValue -> cbor.map { cborToDisplayValue(it) }
                else -> cbor.toString()
            }
    }

    class Factory(private val collectionName: String, private val isStandAlone: Boolean) :
        ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel> create(modelClass: Class<T>): T = DocumentsViewModel(collectionName, isStandAlone) as T
    }
}
