package live.ditto.tools.databrowser

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ditto.kotlin.DittoStoreObserver
import com.ditto.kotlin.serialization.DittoCborSerializable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DocumentsViewModel(private val collectionName: String) : ViewModel() {

    companion object {
        const val PAGE_SIZE = 25
    }

    data class Page(
        val docs: List<Document>,
        val docProperties: List<String>,
        val offset: Int,
        val snapshotAtCount: Int,
    )

    data class PageUiState(
        val page: Page? = null,
        val isLoading: Boolean = true,
        val error: String? = null,
    )

    private val _totalCount = MutableStateFlow<Int?>(null)
    val totalCount: StateFlow<Int?> = _totalCount.asStateFlow()

    private val _pageState = MutableStateFlow(PageUiState())
    val pageState: StateFlow<PageUiState> = _pageState.asStateFlow()

    private val _selectedDoc = MutableStateFlow<Document?>(null)
    val selectedDoc: StateFlow<Document?> = _selectedDoc.asStateFlow()

    private var whereClause: String = ""
    private var countObserver: DittoStoreObserver? = null

    init {
        countObserver = registerCountObserver(whereClause)
        loadPage(0)
    }

    fun nextPage() {
        val current = _pageState.value.page ?: return
        val total = _totalCount.value ?: return
        val next = current.offset + PAGE_SIZE
        if (next < total) loadPage(next)
    }

    fun previousPage() {
        val current = _pageState.value.page ?: return
        val prev = (current.offset - PAGE_SIZE).coerceAtLeast(0)
        if (prev != current.offset) loadPage(prev)
    }

    fun refresh() {
        loadPage(_pageState.value.page?.offset ?: 0)
    }

    fun selectDoc(doc: Document) {
        _selectedDoc.value = doc
    }

    fun filterDocs(query: String) {
        val newWhere = whereClauseFor(query)
        if (newWhere == whereClause) return
        whereClause = newWhere
        countObserver?.close()
        countObserver = registerCountObserver(whereClause)
        loadPage(0)
    }

    private fun whereClauseFor(input: String): String {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return ""
        if (looksLikeDql(trimmed)) return trimmed
        val escaped = trimmed.replace("'", "''")
        return "_id LIKE '%$escaped%'"
    }

    private fun looksLikeDql(text: String): Boolean {
        val operators = listOf("==", "!=", ">=", "<=", " > ", " < ", " AND ", " OR ", " IN ")
        val upper = " ${text.uppercase()} "
        return operators.any { upper.contains(it) }
    }

    private fun loadPage(offset: Int) {
        _pageState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val sql = buildString {
                    append("SELECT * FROM `").append(collectionName).append("`")
                    if (whereClause.isNotEmpty()) append(" WHERE ").append(whereClause)
                    append(" ORDER BY _id ASC LIMIT ").append(PAGE_SIZE).append(" OFFSET ").append(offset)
                }
                val docs = mutableListOf<Document>()
                val keys = sortedSetOf<String>()
                DittoHandler.ditto.store.execute(sql) { result ->
                    for (item in result.items) {
                        val itemKeys = item.value.keys.mapNotNull { it.stringOrNull }
                        keys.addAll(itemKeys)
                        val props = mutableMapOf<String, Any?>()
                        for (k in itemKeys) props[k] = cborToDisplay(item.value[k])
                        val id = cborToDisplay(item.value["_id"])?.toString() ?: ""
                        docs.add(Document(id, props))
                    }
                }
                val snapshotCount = atomicCount()
                val page = Page(
                    docs = docs,
                    docProperties = keys.toList(),
                    offset = offset,
                    snapshotAtCount = snapshotCount,
                )
                _pageState.value = PageUiState(page = page, isLoading = false, error = null)
                _selectedDoc.value = docs.firstOrNull()
            } catch (t: Throwable) {
                _pageState.update {
                    it.copy(isLoading = false, error = t.message ?: "Query failed")
                }
                _selectedDoc.value = null
            }
        }
    }

    private suspend fun atomicCount(): Int {
        val sql = buildString {
            append("SELECT COUNT(*) AS c FROM `").append(collectionName).append("`")
            if (whereClause.isNotEmpty()) append(" WHERE ").append(whereClause)
        }
        var count = 0
        DittoHandler.ditto.store.execute(sql) { result ->
            count = result.items.firstOrNull()?.value?.get("c")?.longOrNull?.toInt() ?: 0
        }
        return count
    }

    private fun registerCountObserver(where: String): DittoStoreObserver? {
        val sql = buildString {
            append("SELECT COUNT(*) AS c FROM `").append(collectionName).append("`")
            if (where.isNotEmpty()) append(" WHERE ").append(where)
        }
        return try {
            DittoHandler.ditto.store.registerObserver(sql) { result ->
                val c = result.items.firstOrNull()?.value?.get("c")?.longOrNull?.toInt() ?: 0
                _totalCount.value = c
            }
        } catch (t: Throwable) {
            _pageState.update { it.copy(error = t.message ?: "Count query failed") }
            null
        }
    }

    override fun onCleared() {
        super.onCleared()
        countObserver?.close()
    }

    private fun cborToDisplay(cbor: DittoCborSerializable): Any? {
        return cbor.stringOrNull
            ?: cbor.longOrNull
            ?: cbor.booleanOrNull
            ?: cbor.doubleOrNull
            ?: cbor.floatOrNull
            ?: when {
                cbor.isNull -> null
                cbor is DittoCborSerializable.Dictionary -> cbor.entries.associate { (k, v) ->
                    (k.stringOrNull ?: k.toString()) to cborToDisplay(v)
                }
                cbor is DittoCborSerializable.ArrayValue -> cbor.map { cborToDisplay(it) }
                else -> cbor.toString()
            }
    }

    class Factory(private val collectionName: String) :
        ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            DocumentsViewModel(collectionName) as T
    }
}

fun formatDisplayValue(value: Any?): String {
    return when (value) {
        null -> "null"
        is String -> value
        is Map<*, *> -> buildString {
            append("{")
            append(value.entries.joinToString(", ") { (k, v) -> "\"$k\": ${formatDisplayValue(v)}" })
            append("}")
        }
        is List<*> -> buildString {
            append("[")
            append(value.joinToString(", ") { formatDisplayValue(it) })
            append("]")
        }
        else -> value.toString()
    }
}
