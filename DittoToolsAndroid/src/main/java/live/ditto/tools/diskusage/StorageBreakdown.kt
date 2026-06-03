package live.ditto.tools.diskusage

import java.util.Date

/**
 * A point-in-time snapshot of on-disk storage from diskUsage.observe().
 *
 * Only the SDK-reported total is captured. The Inspector deliberately
 * does not infer per-category bytes from path strings, since that would
 * rely on the SDK's internal directory-naming conventions.
 */
data class StorageBreakdown(
    val totalOnDiskBytes: Int = 0,
    val capturedAt: Date = Date()
) {
    companion object {
        /** Sentinel "no data yet" value. */
        val empty = StorageBreakdown(totalOnDiskBytes = 0, capturedAt = Date(0))
    }
}