package live.ditto.tools.diskusage

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import live.ditto.Ditto
import live.ditto.DiskUsageItem
import live.ditto.tools.healthmetrics.HealthMetric
import live.ditto.tools.healthmetrics.HealthMetricProvider
import java.util.Date

/**
 * Subscribes to the SDK's diskUsage.observe() and republishes each
 * emission as a [StorageBreakdown]. Maintains a session-only in-memory
 * history buffer for the growth rate and sparkline trend. Registers no
 * observers or subscriptions on user collections.
 */
class DiskStorageViewModel(
    val ditto: Ditto,
    prefs: SharedPreferences? = null,
    healthThresholdBytes: Int = FIVE_HUNDRED_MEGABYTES_IN_BYTES,
    private val maxHistorySize: Int = MAX_HISTORY_SIZE
) : ViewModel(), HealthMetricProvider {

    companion object {
        /** ~1 minute of trend at a typical publisher rate. FIFO trim. */
        const val MAX_HISTORY_SIZE = 60

        /** Below this many seconds the growth rate window is too short. */
        const val GROWTH_RATE_MIN_ELAPSED_SECONDS = 5.0

        /** How far back the growth rate averages over. */
        const val GROWTH_RATE_WINDOW_SECONDS = 30.0
    }

    // Published state
    private val _breakdown = MutableStateFlow(StorageBreakdown.empty)
    val breakdown: StateFlow<StorageBreakdown> = _breakdown

    private val _hasReceivedFirstSnapshot = MutableStateFlow(false)
    val hasReceivedFirstSnapshot: StateFlow<Boolean> = _hasReceivedFirstSnapshot

    private val _healthThresholdBytes = MutableStateFlow(FIVE_HUNDRED_MEGABYTES_IN_BYTES)
    val healthThresholdBytes: StateFlow<Int> = _healthThresholdBytes

    // Internal history buffer
    private val history = mutableListOf<StorageBreakdown>()

    private val preferences: SharedPreferences? = prefs
    private var diskUsageObserverHandle: Any? = null

    init {
        // Resolve initial threshold: persisted value wins, then caller arg
        val persisted = preferences?.getInt(THRESHOLD_PREFS_KEY, -1) ?: -1
        val initial = if (persisted > 0) persisted else healthThresholdBytes
        _healthThresholdBytes.value = initial.coerceAtLeast(MINIMUM_THRESHOLD_BYTES)

        // Self-heal stale persisted value
        if (_healthThresholdBytes.value != persisted) {
            preferences?.edit { putInt(THRESHOLD_PREFS_KEY, _healthThresholdBytes.value) }
        }

        subscribe()
    }

    // -- Public API --

    fun setThreshold(bytes: Int) {
        val clamped = bytes.coerceAtLeast(MINIMUM_THRESHOLD_BYTES)
        _healthThresholdBytes.value = clamped
        preferences?.edit { putInt(THRESHOLD_PREFS_KEY, clamped) }
    }

    // -- Derived state --

    val healthStatus: HealthStatus
        get() = HealthStatus.from(_breakdown.value.totalOnDiskBytes, _healthThresholdBytes.value)

    /** Total on-disk bytes across the rolling window, oldest first. */
    val historyTotalBytes: List<Int>
        get() = history.map { it.totalOnDiskBytes }

    /**
     * Average byte growth per second over a rolling time window.
     * null until there's at least [GROWTH_RATE_MIN_ELAPSED_SECONDS] of data.
     */
    val growthRatePerSecond: Double?
        get() {
            val last = history.lastOrNull() ?: return null
            val cutoff = Date(last.capturedAt.time - (GROWTH_RATE_WINDOW_SECONDS * 1000).toLong())
            val first = history.firstOrNull { it.capturedAt >= cutoff } ?: return null
            val elapsed = (last.capturedAt.time - first.capturedAt.time) / 1000.0
            if (elapsed < GROWTH_RATE_MIN_ELAPSED_SECONDS) return null
            return (last.totalOnDiskBytes - first.totalOnDiskBytes) / elapsed
        }

    /**
     * Estimated seconds until current usage hits the threshold.
     * Returns 0 if already over, null if rate not known or shrinking.
     */
    val estimatedSecondsToThreshold: Double?
        get() {
            val remaining = _healthThresholdBytes.value - _breakdown.value.totalOnDiskBytes
            val rate = growthRatePerSecond ?: return null
            if (rate <= 0) return null
            if (remaining <= 0) return 0.0
            return remaining.toDouble() / rate
        }

    /** Fill ratio of currentBytes against the threshold, clamped to [0, 1]. */
    val fillRatio: Double
        get() {
            val threshold = _healthThresholdBytes.value
            if (threshold <= 0) return 0.0
            return (_breakdown.value.totalOnDiskBytes.toDouble() / threshold).coerceIn(0.0, 1.0)
        }

    // -- Subscription --

    private fun subscribe() {
        diskUsageObserverHandle = ditto.diskUsage.observe { item ->
            apply(item)
        }
    }

    private fun apply(item: DiskUsageItem) {
        val next = StorageBreakdown(
            totalOnDiskBytes = item.sizeInBytes,
            capturedAt = Date()
        )
        history.add(next)
        if (history.size > maxHistorySize) {
            history.removeAt(0)
        }
        _breakdown.value = next
        _hasReceivedFirstSnapshot.value = true
    }

    // -- Health metric provider --

    override val metricName: String = METRIC_NAME

    override fun getCurrentState(): HealthMetric {
        val current = _breakdown.value
        return if (_hasReceivedFirstSnapshot.value) {
            HealthMetric(
                isHealthy = current.totalOnDiskBytes <= _healthThresholdBytes.value,
                details = mapOf(METRIC_NAME to Format.bytes(current.totalOnDiskBytes))
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