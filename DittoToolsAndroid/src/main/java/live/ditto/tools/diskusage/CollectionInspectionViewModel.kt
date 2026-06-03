package live.ditto.tools.diskusage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import live.ditto.Ditto
import java.util.Date

/**
 * Owns the opt-in collection scan and sample state for the Inspector.
 * Both halves use one-shot store.execute DQL i.e, no observers, no subscriptions.
 */
class CollectionInspectionViewModel(
    private val ditto: Ditto
) : ViewModel() {

    companion object {
        /** Max documents per sample. Caps memory and keeps the query fast. */
        const val SAMPLE_LIMIT = 1_000
    }

    // -- Scan state --

    private val _discoveredCollections = MutableStateFlow<List<String>>(emptyList())
    val discoveredCollections: StateFlow<List<String>> = _discoveredCollections

    private val _collectionScanStates = MutableStateFlow<Map<String, CollectionScanState>>(emptyMap())
    val collectionScanStates: StateFlow<Map<String, CollectionScanState>> = _collectionScanStates

    private val _selectedCollection = MutableStateFlow<String?>(null)
    val selectedCollection: StateFlow<String?> = _selectedCollection

    private val _isScanningCollections = MutableStateFlow(false)
    val isScanningCollections: StateFlow<Boolean> = _isScanningCollections

    private val _hasScannedCollections = MutableStateFlow(false)
    val hasScannedCollections: StateFlow<Boolean> = _hasScannedCollections

    private val _scanError = MutableStateFlow<String?>(null)
    val scanError: StateFlow<String?> = _scanError

    // -- Sample state --

    private val _collectionSamples = MutableStateFlow<Map<String, CollectionSample>>(emptyMap())

    private val _isSamplingCollection = MutableStateFlow(false)
    val isSamplingCollection: StateFlow<Boolean> = _isSamplingCollection

    private val _sampleError = MutableStateFlow<String?>(null)
    val sampleError: StateFlow<String?> = _sampleError

    // -- Derived state --

    /** Last scanned document count for the currently selected collection. */
    val totalDocsForSelected: Int?
        get() {
            val name = _selectedCollection.value ?: return null
            val state = _collectionScanStates.value[name]
            return if (state is CollectionScanState.Counted) state.count else null
        }

    /** Cached sample for the currently selected collection. */
    val sampleForSelected: CollectionSample?
        get() {
            val name = _selectedCollection.value ?: return null
            return _collectionSamples.value[name]
        }

    /** Number of collections seen by the most recent successful scan. */
    val collectionCount: Int?
        get() {
            if (!_hasScannedCollections.value || _scanError.value != null) return null
            return _discoveredCollections.value.size
        }

    /** Sum of every successfully counted collection. */
    val totalDocumentCount: Int?
        get() {
            if (!_hasScannedCollections.value || _scanError.value != null) return null
            val states = _collectionScanStates.value.values
            if (states.any { it is CollectionScanState.Pending }) return null
            return states.sumOf { state ->
                if (state is CollectionScanState.Counted) state.count else 0
            }
        }

    // -- Public API --

    /** Kicks off a fresh discovery + count scan. No-op if one is already running. */
    fun scanCollections() {
        if (_isScanningCollections.value) return
        _isScanningCollections.value = true
        _scanError.value = null

        viewModelScope.launch {
            try {
                val names = fetchCollectionNames()

                _discoveredCollections.value = names
                _collectionScanStates.value = names.associateWith { CollectionScanState.Pending }

                // Drop cached samples for collections that no longer exist
                val validNames = names.toSet()
                _collectionSamples.value = _collectionSamples.value.filter { validNames.contains(it.key) }

                // Keep current selection if it survived, otherwise pick first
                val selectionSurvived = _selectedCollection.value?.let { names.contains(it) } ?: false
                if (!selectionSurvived) {
                    _selectedCollection.value = names.firstOrNull()
                    _sampleError.value = null
                }

                _hasScannedCollections.value = true

                // Fetch counts for each collection
                for (name in names) {
                    try {
                        val count = fetchCount(name)
                        val current = _collectionScanStates.value.toMutableMap()
                        current[name] = CollectionScanState.Counted(count)
                        _collectionScanStates.value = current
                    } catch (_: Exception) {
                        val current = _collectionScanStates.value.toMutableMap()
                        current[name] = CollectionScanState.Failed
                        _collectionScanStates.value = current
                    }
                }
            } catch (e: Exception) {
                _scanError.value = e.message ?: "Collection scan failed"
                _hasScannedCollections.value = true
            } finally {
                _isScanningCollections.value = false
            }
        }
    }

    /** Updates the selected collection. Unknown names are ignored. */
    fun selectCollection(name: String) {
        if (!_discoveredCollections.value.contains(name)) return
        _selectedCollection.value = name
        _sampleError.value = null
    }

    /** Samples the currently selected collection. No-op if nothing is selected. */
    fun sampleSelectedCollection() {
        if (_isSamplingCollection.value) return
        val name = _selectedCollection.value ?: return
        _isSamplingCollection.value = true
        _sampleError.value = null

        viewModelScope.launch {
            try {
                val sample = buildSample(name, SAMPLE_LIMIT)
                // Guard against re-scan dropping the collection mid-flight
                if (_discoveredCollections.value.contains(name)) {
                    val current = _collectionSamples.value.toMutableMap()
                    current[name] = sample
                    _collectionSamples.value = current
                }
            } catch (e: Exception) {
                _sampleError.value = e.message ?: "Sample failed"
            } finally {
                _isSamplingCollection.value = false
            }
        }
    }

    // -- Private helpers --

    private suspend fun fetchCollectionNames(): List<String> = withContext(Dispatchers.IO) {
        val result = ditto.store.execute("SELECT * FROM system:collections")
        result.items.mapNotNull { item ->
            item.value["name"] as? String
        }.distinct().sorted()
    }

    private suspend fun fetchCount(collection: String): Int = withContext(Dispatchers.IO) {
        val escaped = escapeIdentifier(collection)
        val result = ditto.store.execute("SELECT COUNT(*) AS total FROM $escaped")
        val item = result.items.firstOrNull()
            ?: throw Exception(DiskUsageScanError.EMPTY_RESULT.message)
        when (val total = item.value["total"]) {
            is Int -> total
            is Long -> total.toInt()
            is Double -> total.toInt()
            else -> throw Exception(DiskUsageScanError.UNEXPECTED_RESULT_FORMAT.message)
        }
    }

    private suspend fun buildSample(collection: String, limit: Int): CollectionSample =
        withContext(Dispatchers.IO) {
            val escaped = escapeIdentifier(collection)
            val counts = IntArray(BUCKET_TEMPLATES.size)
            var sampled = 0

            val result = ditto.store.execute("SELECT * FROM $escaped LIMIT $limit")
            for (item in result.items) {
                val bytes = item.jsonString().toByteArray().size
                counts[bucketIndex(bytes)] += 1
                sampled++
            }

            val buckets = BUCKET_TEMPLATES.mapIndexed { index, template ->
                DocSizeBucket(id = template.id, label = template.label, count = counts[index])
            }

            CollectionSample(
                collectionName = collection,
                sampledCount = sampled,
                buckets = buckets,
                reachedLimit = sampled >= limit,
                sampledAt = Date()
            )
        }

    private fun escapeIdentifier(name: String): String {
        return "`${name.replace("`", "``")}`"
    }
}

// -- Bucket definitions (6 buckets, powers of 4) --

data class BucketTemplate(val id: String, val label: String, val upperBoundBytes: Int?)

val BUCKET_TEMPLATES = listOf(
    BucketTemplate("under-1kb", "< 1 KB", 1024),
    BucketTemplate("1-4kb", "1–4 KB", 4096),
    BucketTemplate("4-16kb", "4–16 KB", 16384),
    BucketTemplate("16-64kb", "16–64 KB", 65536),
    BucketTemplate("64-256kb", "64–256 KB", 262144),
    BucketTemplate("256kb-plus", "≥ 256 KB", null),
)

fun bucketIndex(sizeBytes: Int): Int {
    for ((index, template) in BUCKET_TEMPLATES.withIndex()) {
        val upper = template.upperBoundBytes ?: return index
        if (sizeBytes < upper) return index
    }
    return BUCKET_TEMPLATES.size - 1
}