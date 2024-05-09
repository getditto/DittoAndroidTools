package live.ditto.dittohealthmetrics

data class HealthMetric(
    val isHealthy: Boolean,
    val details: Map<String, String>
)
