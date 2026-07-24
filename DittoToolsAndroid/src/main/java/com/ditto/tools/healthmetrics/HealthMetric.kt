package com.ditto.tools.healthmetrics

data class HealthMetric(
    val isHealthy: Boolean,
    val details: Map<String, String>
)
