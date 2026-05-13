package live.ditto.tools.diskusage

import java.util.Date

data class DocSizeBucket(
    val id: String,
    val label: String,
    var count: Int
)

data class CollectionSample(
    val name: String,
    val sampledCount: Int,
    val sampleBytes: Int,
    val buckets: List<DocSizeBucket>,
    val wasTruncated: Boolean,
    val scannedAt: Date
)