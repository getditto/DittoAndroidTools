package live.ditto.health.ui.viewmodel

import android.content.Context
import android.os.Build
import androidx.compose.ui.platform.UriHandler
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import live.ditto.health.data.DeviceDetails
import live.ditto.health.data.HealthUiState
import live.ditto.health.usecase.GetBluetoothStatusFlow
import live.ditto.health.usecase.GetDittoMissingPermissionsFlow
import live.ditto.health.usecase.GetPermissionsMetricsUseCase
import live.ditto.health.usecase.GetWifiAwareStatusUseCase
import live.ditto.health.usecase.GetWifiStatusFlow
import live.ditto.healthmetrics.HealthMetric
import live.ditto.healthmetrics.HealthMetricProvider

class HealthViewModel(
    context: Context,
    getDittoMissingPermissionsFlow: GetDittoMissingPermissionsFlow = GetDittoMissingPermissionsFlow(
        context = context,
    ),
    getWifiStatusFlow: GetWifiStatusFlow = GetWifiStatusFlow(context = context),
    getBluetoothStatusFlow: GetBluetoothStatusFlow = GetBluetoothStatusFlow(context = context),
    private val getPermissionsMetricsUseCase: GetPermissionsMetricsUseCase = GetPermissionsMetricsUseCase(),
    getWifiAwareStatusUseCase: GetWifiAwareStatusUseCase = GetWifiAwareStatusUseCase(context = context)
) : ViewModel(), HealthMetricProvider {

    private var _state = MutableStateFlow(
        HealthUiState(
            deviceDetails = DeviceDetails(
                modelAndManufacturer = "",
                androidVersionDetails = ""
            )
        )
    )
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

        _state.update {
            it.copy(wifiAwareState = getWifiAwareStatusUseCase())
        }

        updateDeviceDetails()
    }

    fun openLearnMoreLink(uriHandler: UriHandler) {
        uriHandler.openUri(LEARN_MORE_URL)
    }

    private fun updateDeviceDetails() {
        _state.update {
            it.copy(
                deviceDetails = DeviceDetails(
                    modelAndManufacturer = createModelAndManufacturer(),
                    androidVersionDetails = createAndroidVersionDetails()
                )
            )
        }
    }

    private fun createAndroidVersionDetails(): String {
        val sdkVersion = Build.VERSION.SDK_INT.toString()
        val androidReleaseVersion = Build.VERSION.RELEASE
        return "Android $androidReleaseVersion | SDK $sdkVersion"
    }

    private fun createModelAndManufacturer(): String {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        return if (model.startsWith(manufacturer)) {
            model.capitalize()
        } else {
            manufacturer.capitalize() + " " + model
        }
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

    private fun onBluetoothStatus(status: Boolean?) {
        _state.update {
            it.copy(bluetoothEnabled = status ?: false)
        }
    }

    override val metricName: String
        get() = METRIC_NAME

    override fun getCurrentState(): HealthMetric {
        val currentState = _state.value

        return getPermissionsMetricsUseCase(currentState)
    }

    companion object {
        const val LEARN_MORE_URL =
            "https://developer.android.com/guide/topics/connectivity/wifi-aware"
        const val METRIC_NAME = "DittoPermissionsHealth"
    }
}