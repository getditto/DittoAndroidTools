package live.ditto.tools.health.ui.viewmodel

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
import live.ditto.tools.health.usecase.GetBluetoothStatusFlow
import live.ditto.tools.health.usecase.GetDittoMissingPermissionsFlow
import live.ditto.tools.health.usecase.GetWifiAwareStatusUseCase
import live.ditto.tools.health.usecase.GetWifiStatusFlow
import live.ditto.tools.health.data.DeviceDetails
import live.ditto.tools.health.data.HealthUiState
import live.ditto.tools.health.usecase.BluetoothState
import live.ditto.tools.health.usecase.GetPermissionsMetricsUseCase
import live.ditto.tools.healthmetrics.HealthMetric
import live.ditto.tools.healthmetrics.HealthMetricProvider
import java.util.Locale

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
            model.replaceFirstChar { char -> char.uppercase() }
        } else {
            manufacturer.replaceFirstChar { char -> char.uppercase() } + " " + model
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

    private fun onBluetoothStatus(bluetoothState: BluetoothState) {
        _state.update {
            it.copy(bluetoothState = bluetoothState)
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