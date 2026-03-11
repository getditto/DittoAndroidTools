package live.ditto.tools.databrowser

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.json.JSONObject

class DocumentsViewModel(private val collectionName: String, isStandAlone: Boolean): ViewModel() {

    companion object {
        const val PAGE_SIZE = 50
    }

    val docsList: MutableLiveData<MutableList<Document>> = MutableLiveData(mutableListOf())
    var docProperties: MutableLiveData<List<String>> = MutableLiveData(emptyList())
    var selectedDoc = MutableLiveData<Document>()
    var errorMessage = MutableLiveData<String?>()
    val currentOffset = MutableLiveData(0)
    val totalCount = MutableLiveData(0)

    private var currentWhereClause: String = ""

    // Keep subscription for sync (standalone only)
    val subscription = if (isStandAlone) DittoHandler.ditto.store.collection(collectionName).findAll().subscribe() else null

    // Observers initialized at declaration; re-assigned on page/filter changes
    private var pageObserver = registerPageObserver("", 0)
    private var countObserver = registerCountObserver("")

    private fun registerPageObserver(whereClause: String, offset: Int): AutoCloseable {
        val query = if (whereClause.isEmpty()) {
            "SELECT * FROM $collectionName ORDER BY _id ASC LIMIT $PAGE_SIZE OFFSET $offset"
        } else {
            "SELECT * FROM $collectionName WHERE $whereClause ORDER BY _id ASC LIMIT $PAGE_SIZE OFFSET $offset"
        }
        return try {
            errorMessage.postValue(null)
            DittoHandler.ditto.store.registerObserver(query) { result ->
                val newDocs = mutableListOf<Document>()
                val allKeys = mutableSetOf<String>()
                for (item in result.items) {
                    try {
                        val json = JSONObject(item.jsonString())
                        val id = json.opt("_id")?.toString() ?: continue
                        val docValues = mutableMapOf<String, Any?>()
                        for (key in json.keys()) {
                            docValues[key] = json.get(key)
                        }
                        allKeys.addAll(docValues.keys)
                        newDocs.add(Document(id, docValues))
                    } catch (e: Exception) {
                        // skip malformed items
                    }
                }
                if (allKeys.isNotEmpty()) {
                    docProperties.postValue(allKeys.sorted())
                }
                docsList.postValue(newDocs)
            }
        } catch (e: Exception) {
            errorMessage.postValue("Query error: ${e.message}")
            AutoCloseable { }
        }
    }

    private fun registerCountObserver(whereClause: String): AutoCloseable {
        val query = if (whereClause.isEmpty()) {
            "SELECT COUNT(*) AS count FROM $collectionName"
        } else {
            "SELECT COUNT(*) AS count FROM $collectionName WHERE $whereClause"
        }
        return try {
            DittoHandler.ditto.store.registerObserver(query) { result ->
                val count = result.items.firstOrNull()?.let {
                    JSONObject(it.jsonString()).optInt("count", 0)
                } ?: 0
                totalCount.postValue(count)
            }
        } catch (e: Exception) {
            AutoCloseable { }
        }
    }

    private fun reloadPage(whereClause: String, offset: Int) {
        pageObserver.close()
        countObserver.close()
        currentOffset.value = offset
        pageObserver = registerPageObserver(whereClause, offset)
        countObserver = registerCountObserver(whereClause)
    }

    fun nextPage() {
        reloadPage(currentWhereClause, (currentOffset.value ?: 0) + PAGE_SIZE)
    }

    fun previousPage() {
        reloadPage(currentWhereClause, maxOf(0, (currentOffset.value ?: 0) - PAGE_SIZE))
    }

    fun filterDocs(queryString: String) {
        currentWhereClause = when {
            queryString.isEmpty() -> ""
            isDQLQuery(queryString) -> queryString
            else -> "_id LIKE '%${queryString.replace("'", "''")}%'"
        }
        reloadPage(currentWhereClause, 0)
    }

    private fun isDQLQuery(text: String): Boolean {
        val dqlOperators = listOf("==", "!=", "CONTAINS", "contains", ">", "<", ">=", "<=", "AND", "and", "OR", "or", "IN", "in")
        return dqlOperators.any { text.contains(it) }
    }

    override fun onCleared() {
        super.onCleared()
        pageObserver.close()
        countObserver.close()
    }

    class MyViewModelFactory(private val collectionName: String, private val isStandAlone: Boolean) :
        ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel> create(modelClass: Class<T>): T = DocumentsViewModel(collectionName, isStandAlone) as T
    }
}
