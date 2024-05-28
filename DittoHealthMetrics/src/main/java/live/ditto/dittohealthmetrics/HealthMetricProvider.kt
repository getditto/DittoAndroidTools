package live.ditto.dittohealthmetrics

/**
 * A system to provide custom `HealthMetric`s via the HeartbeatTool for remote monitoring.
 */
interface HealthMetricProvider {
    /**
     * The unique name of this health metric. Used as the key when storing this into the list of health metrics.
     */
    val metricName: String

    /**
     * Used to return the current state of this metric. The Heartbeat tool will call this on its configured interval
     * to get a snapshot of the current state of this health metric.
     */
    fun getCurrentState(): HealthMetric
}
