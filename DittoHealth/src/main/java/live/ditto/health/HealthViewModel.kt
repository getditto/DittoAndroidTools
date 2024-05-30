package live.ditto.health

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import live.ditto.dittohealthmetrics.HealthMetric
import live.ditto.dittohealthmetrics.HealthMetricProvider
import live.ditto.health.usecase.GetBluetoothStatusFlow
import live.ditto.health.usecase.GetDittoMissingPermissionsFlow
import live.ditto.health.usecase.GetPermissionsMetrics
import live.ditto.health.usecase.GetWifiStatusFlow

class HealthViewModel(
    context: Context,
    getDittoMissingPermissionsFlow: GetDittoMissingPermissionsFlow = GetDittoMissingPermissionsFlow(
        context = context,
    ),
    getWifiStatusFlow: GetWifiStatusFlow = GetWifiStatusFlow(context = context),
    getBluetoothStatusFlow: GetBluetoothStatusFlow = GetBluetoothStatusFlow(context = context),
    private val getPermissionsMetrics: GetPermissionsMetrics = GetPermissionsMetrics(),
) : ViewModel(), HealthMetricProvider {
    private var _state = MutableStateFlow(HealthUiState())
    val state = _state.asStateFlow()

    init {
        getDittoMissingPermissionsFlow()
            .onEach(::onDittoMissingPermissions)
            .flowOn(Dispatchers.Default)
            .launchIn(viewModelScope)

        getWifiStatusFlow()
            .onEach(::onWifiStatus)
            .flowOn(Dispatchers.Default)
            .launchIn(viewModelScope)

        getBluetoothStatusFlow()
            .onEach(::onBluetoothStatus)
            .flowOn(Dispatchers.Default)
            .launchIn(viewModelScope)
    }

    private fun onDittoMissingPermissions(missingPermissions: Array<String>) {
        _state.update {
            it.copy(missingPermissions = missingPermissions.asList())
        }
    }

    private fun onWifiStatus(status: Boolean) {
        _state.update {
            it.copy(wifiEnabled = status)
        }
    }

    private fun onBluetoothStatus(status: Boolean) {
        _state.update {
            it.copy(bluetoothEnabled = status)
        }
    }

    override val metricName: String
        get() = getPermissionsMetrics.metricName

    override fun getCurrentState(): HealthMetric {
        val currentState = _state.value

        return getPermissionsMetrics.execute(
            currentState.missingPermissions,
            currentState.wifiEnabled,
            currentState.bluetoothEnabled
        )
    }
}