package com.ditto.tools.databrowser

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ditto.kotlin.DittoException
import com.ditto.kotlin.DittoStoreObserver
import com.ditto.kotlin.serialization.DittoCborSerializable

internal class DocumentsViewModel(private val collectionName: String) : ViewModel() {

    val docsList: MutableLiveData<MutableList<Document>> = MutableLiveData<MutableList<Document>>(mutableListOf())
    var selectedDoc = MutableLiveData<Document>()
    var errorMessage = MutableLiveData<String?>()

    private var observer: DittoStoreObserver? = null

    init {
        observeDocuments()
    }

    private fun observeDocuments(whereExpression: String? = null) {
        observer?.close()
        observer = null
        errorMessage.postValue(null)
        try {
            observer = DittoHandler.ditto.store.registerObserver(
                documentsQuery(collectionName, whereExpression)
            ) { result ->
                val newDocsList = mutableListOf<Document>()
                for (item in result.items) {
                    val valueMap = item.value
                    val keys = valueMap.keys.mapNotNull { it.stringOrNull }.sorted()

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
            val details = e.message?.let { "\n\n$it" }.orEmpty()
            val isWhereExpressionError =
                whereExpression != null &&
                    e is DittoException.StoreException &&
                    e.reason.isQueryFailure()
            if (isWhereExpressionError) {
                errorMessage.postValue(
                    "The filter couldn't be applied. Check the DQL WHERE expression and try again.$details"
                )
            } else {
                errorMessage.postValue(
                    "Documents couldn't be loaded.$details"
                )
            }
            docsList.postValue(mutableListOf())
        }
    }

    fun filterDocs(whereExpression: String) {
        observeDocuments(whereExpression.trim().ifEmpty { null })
    }

    override fun onCleared() {
        super.onCleared()
        observer?.close()
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

    internal class DocumentsViewModelFactory(private val collectionName: String) :
        ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel> create(modelClass: Class<T>): T = DocumentsViewModel(collectionName) as T
    }
}

private fun DittoException.StoreExceptionReason.isQueryFailure(): Boolean =
    this is DittoException.StoreExceptionReason.QueryCompilationFailed ||
        this is DittoException.StoreExceptionReason.QueryError ||
        this is DittoException.StoreExceptionReason.QueryInvalid ||
        this is DittoException.StoreExceptionReason.QueryNotSupported

internal fun documentsQuery(collectionName: String, whereExpression: String?): String {
    val predicate = whereExpression?.trim().orEmpty()
    return buildString {
        append("SELECT * FROM `")
        append(collectionName)
        append("`")
        if (predicate.isNotEmpty()) {
            append(" WHERE ")
            append(predicate)
        }
        append(" LIMIT 1000")
    }
}

internal fun selectedDocumentIndex(documents: List<Document>, selectedDocumentId: String?): Int? {
    if (documents.isEmpty()) {
        return null
    }

    return documents
        .indexOfFirst { document -> document.id == selectedDocumentId }
        .takeIf { index -> index >= 0 }
        ?: 0
}

internal fun formatDisplayValue(value: Any?): String {
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
