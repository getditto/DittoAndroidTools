package live.ditto.healthmetrics

data class HealthMetric(
    val isHealthy: Boolean,
    val details: Map<String, String>
)
