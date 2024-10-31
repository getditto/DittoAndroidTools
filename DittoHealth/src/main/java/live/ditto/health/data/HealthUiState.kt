package live.ditto.health.data

import android.content.Context
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import live.ditto.health.HealthUiActionType
import live.ditto.health.HealthUiStateCause
import live.ditto.health.R
import live.ditto.health.usecase.WifiAwareState

data class HealthUiState(
    val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    val missingPermissions: List<String> = emptyList(),
    val wifiEnabled: Boolean = true,
    val bluetoothEnabled: Boolean = true,
    val wifiAwareState: WifiAwareState = WifiAwareState.UNSUPPORTED,
    val deviceDetails: DeviceDetails
) {
    suspend fun sortedHealthUiStateCauses(context: Context) = withContext(defaultDispatcher) {
        val withActions = mutableListOf<HealthUiStateCause>()
        val noActions = mutableListOf<HealthUiStateCause>()

        processMissingPermissions(noActions, withActions, context)
        processWifiStatus(noActions, withActions, context)
        processBluetoothStatus(noActions, withActions, context)

        withActions + noActions
    }

    private fun processMissingPermissions(
        noActions: MutableList<HealthUiStateCause>,
        withActions: MutableList<HealthUiStateCause>,
        context: Context,
    ) {
        if (missingPermissions.isEmpty()) {
            noActions.add(
                HealthUiStateCause(
                    reason = context.getString(R.string.required_permissions),
                    details = listOf(context.getString(R.string.required_permissions_granted)),
                    actionType = HealthUiActionType.NoAction
                )
            )
        } else {
            withActions.add(
                HealthUiStateCause(
                    reason = context.getString(R.string.missing_permissions),
                    details = missingPermissions,
                    actionType = HealthUiActionType.RequestPermissions
                )
            )
        }
    }

    private fun processWifiStatus(
        noActions: MutableList<HealthUiStateCause>,
        withActions: MutableList<HealthUiStateCause>,
        context: Context
    ) {
        if (wifiEnabled) {
            noActions.add(
                HealthUiStateCause(
                    reason = context.getString(R.string.wifi_status),
                    details = listOf(context.getString(R.string.wifi_enabled)),
                    actionType = HealthUiActionType.NoAction
                )
            )
        } else {
            withActions.add(
                HealthUiStateCause(
                    reason = context.getString(R.string.wifi_status),
                    details = listOf(context.getString(R.string.wifi_not_enabled)),
                    actionType = HealthUiActionType.EnableWifi
                )
            )
        }
    }

    private fun processBluetoothStatus(
        noActions: MutableList<HealthUiStateCause>,
        withActions: MutableList<HealthUiStateCause>,
        context: Context
    ) {
        if (bluetoothEnabled) {
            noActions.add(
                HealthUiStateCause(
                    reason = context.getString(R.string.bluetooth_status),
                    details = listOf(context.getString(R.string.bluetooth_enabled)),
                    actionType = HealthUiActionType.NoAction
                )
            )
        } else {
            withActions.add(
                HealthUiStateCause(
                    reason = context.getString(R.string.bluetooth_status),
                    details = listOf(context.getString(R.string.bluetooth_not_enabled)),
                    actionType = HealthUiActionType.EnableBluetooth
                )
            )
        }
    }
}