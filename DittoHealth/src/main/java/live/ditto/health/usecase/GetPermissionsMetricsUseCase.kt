package live.ditto.health.usecase

import live.ditto.health.data.HealthUiState
import live.ditto.healthmetrics.HealthMetric

class GetPermissionsMetricsUseCase {

    operator fun invoke(healthUiState: HealthUiState): HealthMetric {
        val missingPermissions = healthUiState.missingPermissions
        val wifiEnabled = healthUiState.wifiEnabled
        val bluetoothState = healthUiState.bluetoothState
        val wifiAwareState = healthUiState.wifiAwareState

        val isHealthy by lazy {
            missingPermissions.isEmpty()
                    && wifiEnabled
                    && bluetoothState == BluetoothState.ENABLED
        }

        val details = createDetailsMap(
            missingPermissions = missingPermissions,
            wifiEnabled = wifiEnabled,
            bluetoothState = bluetoothState,
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
        bluetoothState: BluetoothState,
        wifiAwareState: WifiAwareState
    ): MutableMap<String, String> {
        return mutableMapOf<String, String>().apply {
            if (missingPermissions.isNotEmpty()) {
                this[KEY_MISSING_PERMISSIONS] = missingPermissions.joinToString()
            }
            this[KEY_WIFI_ENABLED] = wifiEnabled.toString()
            this[KEY_BLUETOOTH_STATE] = getBluetoothStateDescription(bluetoothState)
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

    private fun getBluetoothStateDescription(bluetoothState: BluetoothState): String {
        return when (bluetoothState) {
            BluetoothState.ENABLED -> "Bluetooth is enabled"
            BluetoothState.DISABLED -> "Bluetooth is disabled"
            BluetoothState.UNSUPPORTED -> "Bluetooth is not supported by this device"
        }
    }

    companion object {
        const val KEY_MISSING_PERMISSIONS = "Missing Permissions"
        const val KEY_WIFI_ENABLED = "WiFi Enabled"
        const val KEY_BLUETOOTH_STATE = "Bluetooth State"
        const val KEY_WIFI_AWARE_STATE = "Wifi Aware State"
    }
}
