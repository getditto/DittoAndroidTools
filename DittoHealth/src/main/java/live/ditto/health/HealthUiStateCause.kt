package live.ditto.health

import android.content.Context

data class HealthUiStateCause(
    val reason: String,
    val details: List<String>,
    val actionType: HealthUiActionType,
) {
    val detailsAsMultilineString = details.joinToString("\n")

    val isHealthy by lazy {
        when (actionType) {
            HealthUiActionType.NoAction -> true
            HealthUiActionType.EnableWifi -> false
            HealthUiActionType.RequestPermissions -> false
            HealthUiActionType.EnableBluetooth -> false
            HealthUiActionType.BluetoothUnsupported -> false
        }
    }

    fun actionText(context: Context) = when (actionType) {
        HealthUiActionType.NoAction -> ""
        HealthUiActionType.EnableWifi -> context.getString(R.string.enable_wifi)
        HealthUiActionType.RequestPermissions -> context.getString(R.string.request_permissions)
        HealthUiActionType.EnableBluetooth -> context.getString(R.string.enable_bluetooth)
        HealthUiActionType.BluetoothUnsupported -> ""
    }
}