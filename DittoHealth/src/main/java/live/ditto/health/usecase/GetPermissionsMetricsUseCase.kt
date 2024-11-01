package live.ditto.health.usecase

import live.ditto.health.data.HealthUiState
import live.ditto.healthmetrics.HealthMetric

class GetPermissionsMetricsUseCase {

    operator fun invoke(healthUiState: HealthUiState): HealthMetric {
        val missingPermissions = healthUiState.missingPermissions
        val wifiEnabled = healthUiState.wifiEnabled
        val bluetoothEnabled = healthUiState.bluetoothEnabled
        val wifiAwareState = healthUiState.wifiAwareState

        val isHealthy = missingPermissions.isEmpty() && wifiEnabled && bluetoothEnabled

        val details = createDetailsMap(
            missingPermissions = missingPermissions,
            wifiEnabled = wifiEnabled,
            bluetoothEnabled = bluetoothEnabled,
            wifiAwareState = wifiAwareState
        )

        return HealthMetric(
            isHealthy = isHealthy,
            details = details
        )
    }

    private fun createDetailsMap(
        missingPermissions: List<String>,
        wifiEnabled: Boolean,
        bluetoothEnabled: Boolean,
        wifiAwareState: WifiAwareState
    ): MutableMap<String, String> {
        return mutableMapOf<String, String>().apply {
            if (missingPermissions.isNotEmpty()) {
                this[KEY_MISSING_PERMISSIONS] = missingPermissions.joinToString()
            }
            this[KEY_WIFI_ENABLED] = wifiEnabled.toString()
            this[KEY_BLUETOOTH_ENABLED] = bluetoothEnabled.toString()
            this[KEY_WIFI_AWARE_STATE] = getWifiAwareStateDescription(wifiAwareState)
        }
    }

    private fun getWifiAwareStateDescription(wifiAwareState: WifiAwareState): String {
        return when (wifiAwareState) {
            WifiAwareState.SUPPORTED -> "Wifi Aware is supported"
            WifiAwareState.UNSUPPORTED -> "Wifi Aware is not supported"
            WifiAwareState.UNSUPPORTED_ANDROID_VERSION -> "Wifi Aware is not supported on this Android version"
        }
    }

    companion object {
        const val KEY_MISSING_PERMISSIONS = "Missing Permissions"
        const val KEY_WIFI_ENABLED = "WiFi Enabled"
        const val KEY_BLUETOOTH_ENABLED = "Bluetooth Enabled"
        const val KEY_WIFI_AWARE_STATE = "Wifi Aware State"
    }
}
