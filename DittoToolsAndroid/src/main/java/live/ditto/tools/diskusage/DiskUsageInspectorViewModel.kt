package live.ditto.tools.diskusage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import live.ditto.Ditto
import live.ditto.DiskUsageItem
import live.ditto.tools.healthmetrics.HealthMetric
import live.ditto.tools.healthmetrics.HealthMetricProvider
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DiskUsageInspectorViewModel(
    val ditto: Ditto,
    healthThresholdBytes: Int = FIVE_HUNDRED_MEGABYTES_IN_BYTES
) : ViewModel(), HealthMetricProvider {

    // Published State
    val fileListing = MutableStateFlow<DiskUsageState?>(
        DiskUsageState()
    )
    val breakdown = MutableStateFlow(StorageBreakdown())
    val diskUsageHistory = MutableStateFlow<List<Int>>(emptyList())
    val attachmentBytesHistory = MutableStateFlow<List<Int>>(emptyList())
    val growthRatePerSecond = MutableStateFlow<Double?>(null)
    val gcEventsDetected = MutableStateFlow(0)
    val lastGCEventDate = MutableStateFlow<Date?>(null)
    val gcBytesReclaimed = MutableStateFlow(0)
    val estimatedSecondsToThreshold = MutableStateFlow<Double?>(null)
    val parseWarnings = MutableStateFlow<List<String>>(emptyList())
    val lastParseDate = MutableStateFlow<Date?>(null)

    // Collection Scan State
    val collections = MutableStateFlow<List<String>>(emptyList())
    val selectedCollection = MutableStateFlow("")
    val collectionCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
    val collectionsWithFailedCount = MutableStateFlow<List<String>>(emptyList())
    val collectionSamples = MutableStateFlow<Map<String, CollectionSample>>(emptyMap())
    val isScanningCollections = MutableStateFlow(false)
    val isSamplingCollection = MutableStateFlow(false)
    val lastCollectionScanDate = MutableStateFlow<Date?>(null)

    val unhealthySizeInBytes = MutableStateFlow(healthThresholdBytes)

    private val maxHistoryCount = 60
    private val historyTimestamps = mutableListOf<Date>()
    private var prevAttachmentBytes = 0
    private var diskUsageObserverHandle: Any? = null

    val dateFormatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    companion object {
        const val SAMPLE_LIMIT = 1000
    }

    init {
        startDiskUsageObservation()
    }

    private fun startDiskUsageObservation() {
        diskUsageObserverHandle = ditto.diskUsage.observe { diskUsageItem ->
            val children = diskUsageItem.childItems ?: return@observe
            viewModelScope.launch(Dispatchers.Main) {
                updateFileListing(diskUsageItem.path, diskUsageItem.sizeInBytes, children)
                updateBreakdown(diskUsageItem.sizeInBytes, children)
            }
        }
    }

    private fun updateFileListing(rootPath: String, totalBytes: Int, children: List<DiskUsageItem>) {
        val childList = children
            .map { child ->
                DiskUsage(
                    relativePath = child.path,
                    sizeInBytes = child.sizeInBytes,
                    size = StorageBreakdown.formatBytes(child.sizeInBytes)
                )
            }
            .sortedByDescending { it.sizeInBytes }

        fileListing.value = DiskUsageState(
            rootPath = rootPath,
            totalSizeInBytes = totalBytes,
            totalSize = StorageBreakdown.formatBytes(totalBytes),
            children = childList,
            unhealthySizeInBytes = unhealthySizeInBytes.value
        )
    }

    private fun updateBreakdown(totalBytes: Int, children: List<DiskUsageItem>) {
        var store = 0
        var attachBytes = 0
        var logs = 0
        var repl = 0

        for (child in children) {
            val p = child.path.lowercase()
            when {
                p.endsWith(DITTO_STORE) -> store = child.sizeInBytes
                p.endsWith(DITTO_ATTACHMENTS) -> attachBytes = child.sizeInBytes
                p.endsWith(DITTO_LOGS) -> logs = child.sizeInBytes
                p.endsWith(DITTO_REPLICATION) -> repl = child.sizeInBytes
            }
        }

        breakdown.value = StorageBreakdown(
            totalOnDiskBytes = totalBytes,
            storeBytes = store,
            attachmentBytes = attachBytes,
            logsBytes = logs,
            replicationBytes = repl
        )

        // GC detection
        val bytesDelta = prevAttachmentBytes - attachBytes
        if (prevAttachmentBytes > 0 && bytesDelta > 10_240) {
            gcEventsDetected.value += 1
            gcBytesReclaimed.value += bytesDelta
            lastGCEventDate.value = Date()
        }
        prevAttachmentBytes = attachBytes

        // History tracking
        diskUsageHistory.value = appendToHistory(totalBytes, diskUsageHistory.value)
        attachmentBytesHistory.value = appendToHistory(attachBytes, attachmentBytesHistory.value)

        val now = Date()
        historyTimestamps.add(now)
        if (historyTimestamps.size > maxHistoryCount) {
            historyTimestamps.removeAt(0)
        }

        // Growth rate calculation
        val history = diskUsageHistory.value
        if (history.size >= 3 && historyTimestamps.size >= 3) {
            val firstTime = historyTimestamps.first()
            val lastTime = historyTimestamps.last()
            val elapsed = (lastTime.time - firstTime.time) / 1000.0
            if (elapsed > 0) {
                growthRatePerSecond.value = (history.last() - history.first()) / elapsed
            }
        }

        // Growth prediction
        val rate = growthRatePerSecond.value
        if (rate != null && rate > 0) {
            val remaining = (unhealthySizeInBytes.value - totalBytes).toDouble()
            estimatedSecondsToThreshold.value = if (remaining > 0) remaining / rate else 0.0
        } else {
            estimatedSecondsToThreshold.value = null
        }

        // Parse warnings
        val warnings = mutableListOf<String>()
        val childPaths = children.map { it.path.lowercase() }

        if (!childPaths.any { it.endsWith(DITTO_STORE) }) {
            warnings.add("Missing expected ditto_store directory")
        }
        if (!childPaths.any { it.endsWith(DITTO_REPLICATION) }) {
            warnings.add("Missing expected ditto_replication directory")
        }

        val childSum = children.sumOf { it.sizeInBytes }
        if (childSum > 0 && totalBytes > 0) {
            val sizeRatio = totalBytes.toDouble() / childSum.toDouble()
            if (sizeRatio !in 0.5..2.0) {
                warnings.add("Total size diverges from sum of children (ratio: %.2fx)".format(sizeRatio))
            }
        }

        if (totalBytes == 0 && children.isNotEmpty()) {
            warnings.add("Root reports 0 bytes but has ${children.size} child items")
        }

        parseWarnings.value = warnings
        lastParseDate.value = Date()
    }

    // Collection Scanning
    fun scanCollections() {
        if (isScanningCollections.value) return
        isScanningCollections.value = true

        viewModelScope.launch {
            try {
                val names = fetchCollectionNames().sorted()
                val counts = mutableMapOf<String, Int>()
                val failed = mutableListOf<String>()

                for (name in names) {
                    try {
                        counts[name] = fetchCount(name)
                    } catch (_: Exception) {
                        failed.add(name)
                    }
                }

                collections.value = names
                collectionCounts.value = counts
                collectionsWithFailedCount.value = failed
                if (!names.contains(selectedCollection.value)) {
                    selectedCollection.value = names.firstOrNull() ?: ""
                }
                lastCollectionScanDate.value = Date()
            } catch (_: Exception) {
                // system:collections query failed
            } finally {
                isScanningCollections.value = false
            }
        }
    }

    fun sampleSelectedCollection() {
        if (isSamplingCollection.value) return
        val name = selectedCollection.value
        if (name.isEmpty()) return

        isSamplingCollection.value = true

        viewModelScope.launch {
            try {
                val sample = buildSample(name, SAMPLE_LIMIT)
                val current = collectionSamples.value.toMutableMap()
                current[name] = sample
                collectionSamples.value = current
            } catch (_: Exception) {
                // sample query failed
            } finally {
                isSamplingCollection.value = false
            }
        }
    }

    fun selectCollection(name: String) {
        selectedCollection.value = name
    }

    fun adjustThreshold(delta: Int) {
        val newValue = (unhealthySizeInBytes.value + delta).coerceAtLeast(50_000_000)
        unhealthySizeInBytes.value = newValue
    }

    // Scan helpers
    private suspend fun fetchCollectionNames(): List<String> = withContext(Dispatchers.IO) {
        val result = ditto.store.execute(
            "SELECT * FROM system:collections"
        )
        result.items.mapNotNull { item ->
            item.value["name"] as? String
        }.distinct()
    }

    private suspend fun fetchCount(collection: String): Int = withContext(Dispatchers.IO) {
        val dqlName = escapeIdentifier(collection)
        val result = ditto.store.execute(
            "SELECT COUNT(*) AS total FROM $dqlName"
        )
        val item = result.items.firstOrNull()
            ?: throw Exception("Empty result")
        when (val total = item.value["total"]) {
            is Int -> total
            is Long -> total.toInt()
            is Double -> total.toInt()
            else -> throw Exception("Unexpected result format")
        }
    }

    private suspend fun buildSample(collection: String, limit: Int): CollectionSample =
        withContext(Dispatchers.IO) {
            val dqlName = escapeIdentifier(collection)
            val buckets = emptySizeBuckets().toMutableList()
            var totalBytes = 0
            var sampledCount = 0

            val result = ditto.store.execute(
                "SELECT * FROM $dqlName LIMIT $limit"
            )
            for (item in result.items) {
                val size = item.jsonString().toByteArray().size
                totalBytes += size
                val bucketIndex = bucketIndex(size)
                buckets[bucketIndex] = buckets[bucketIndex].copy(
                    count = buckets[bucketIndex].count + 1
                )
                sampledCount++
            }

            val knownTotal = collectionCounts.value[collection]
            val wasTruncated = if (knownTotal != null) {
                knownTotal > sampledCount
            } else {
                sampledCount >= limit
            }

            CollectionSample(
                name = collection,
                sampledCount = sampledCount,
                sampleBytes = totalBytes,
                buckets = buckets,
                wasTruncated = wasTruncated,
                scannedAt = Date()
            )
        }

    private fun escapeIdentifier(name: String): String {
        return "`${name.replace("`", "``")}`"
    }

    // History helpers
    private fun appendToHistory(value: Int, list: List<Int>): List<Int> {
        val mutable = list.toMutableList()
        mutable.add(value)
        if (mutable.size > maxHistoryCount) {
            mutable.removeAt(0)
        }
        return mutable
    }

    // Health metric provider
    override val metricName: String = METRIC_NAME

    override fun getCurrentState(): HealthMetric {
        val listing = fileListing.value
        return if (listing != null) {
            val healthCheckSize = listing.children
                .filter {
                    val short = it.relativePath.substringAfterLast('/')
                    short == DITTO_STORE || short == DITTO_REPLICATION
                }
                .sumOf { it.sizeInBytes }
            HealthMetric(
                isHealthy = healthCheckSize <= unhealthySizeInBytes.value,
                details = mapOf(METRIC_NAME to StorageBreakdown.formatBytes(listing.totalSizeInBytes))
            )
        } else {
            HealthMetric(isHealthy = true, details = mapOf(METRIC_NAME to NO_DATA))
        }
    }

    override fun onCleared() {
        super.onCleared()
        (diskUsageObserverHandle as? AutoCloseable)?.close()
    }
}

// Bucket helpers (top-level functions)
fun emptySizeBuckets(): List<DocSizeBucket> = listOf(
    DocSizeBucket("tiny", "< 1 KB", 0),
    DocSizeBucket("small", "1–10 KB", 0),
    DocSizeBucket("medium", "10–100 KB", 0),
    DocSizeBucket("large", "100 KB–1 MB", 0),
    DocSizeBucket("xlarge", "> 1 MB", 0),
)

fun bucketIndex(size: Int): Int = when {
    size < 1_024 -> 0
    size < 10_240 -> 1
    size < 102_400 -> 2
    size < 1_048_576 -> 3
    else -> 4
}