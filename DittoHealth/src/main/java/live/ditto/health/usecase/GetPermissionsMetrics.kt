package live.ditto.health.usecase;

import live.ditto.dittohealthmetrics.HealthMetric

public class GetPermissionsMetrics {
    val metricName: String = "DittoPermissionsHealth"

    fun execute(missingPermissions: List<String>, wifiEnabled: Boolean, bluetoothEnabled: Boolean): HealthMetric {
        val isHealthy = missingPermissions.isEmpty() && wifiEnabled && bluetoothEnabled
        val details = mutableMapOf<String, String>().apply {
            if (missingPermissions.isNotEmpty()) {
                this["Missing Permissions"] = missingPermissions.joinToString()
            }
            this["WiFi Enabled"] = wifiEnabled.toString()
            this["Bluetooth Enabled"] = bluetoothEnabled.toString()
        }

        return HealthMetric(
            isHealthy = isHealthy,
            details = details
        )
    }
}
