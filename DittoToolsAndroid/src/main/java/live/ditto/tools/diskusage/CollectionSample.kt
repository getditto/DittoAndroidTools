package live.ditto.tools.diskusage

import java.util.Date

/**
 * One bucket in the document-size histogram.
 */
data class DocSizeBucket(
    val id: String,
    val label: String,
    val count: Int
)

/**
 * Result of a collection sample, grouped by JSON byte size.
 */
data class CollectionSample(
    val collectionName: String,
    val sampledCount: Int,
    val buckets: List<DocSizeBucket>,
    /** true if the sample hit the limit. */
    val reachedLimit: Boolean,
    val sampledAt: Date
)