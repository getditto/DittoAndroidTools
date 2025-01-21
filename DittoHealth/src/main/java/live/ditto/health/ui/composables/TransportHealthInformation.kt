package live.ditto.health.ui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import live.ditto.health.HealthScreenActionHandler
import live.ditto.health.HealthUiActionType
import live.ditto.health.HealthUiStateCause
import live.ditto.health.components.HealthCheckWithAction
import live.ditto.health.components.HealthCheckWithNoAction
import live.ditto.health.components.Loading
import live.ditto.health.data.DeviceDetails
import live.ditto.health.data.HealthUiState
import live.ditto.health.ui.viewmodel.HealthViewModel

@Composable
internal fun TransportHealthInformation(
    modifier: Modifier = Modifier,
    viewModel: HealthViewModel = HealthViewModel(LocalContext.current),
    healthScreenActionHandler: HealthScreenActionHandler = HealthScreenActionHandler()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    TransportHealthInformation(
        modifier = modifier,
        state = state,
        onAction = { actionType ->
            healthScreenActionHandler.handle(actionType = actionType, context = context)
        },
    )
}

@Composable
private fun TransportHealthInformation(
    modifier: Modifier = Modifier,
    state: HealthUiState,
    onAction: (HealthUiActionType) -> Unit,
) {
    val context = LocalContext.current
    var healthUiStateCauses: List<HealthUiStateCause> by remember(state) {
        mutableStateOf(emptyList())
    }

    LaunchedEffect(key1 = state) {
        healthUiStateCauses = state.sortedHealthUiStateCauses(context = context)
    }

    if (healthUiStateCauses.isEmpty()) {
        Loading(modifier = modifier)
    } else {
        Column(
            modifier = modifier
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            healthUiStateCauses.forEach { healthUiStateCause ->
                val actionType = healthUiStateCause.actionType

                if (actionType == HealthUiActionType.NoAction || actionType == HealthUiActionType.BluetoothUnsupported) {
                    HealthCheckWithNoAction(
                        header = healthUiStateCause.reason,
                        isHealthy = healthUiStateCause.isHealthy,
                        description = healthUiStateCause.detailsAsMultilineString
                    )
                } else {
                    HealthCheckWithAction(
                        header = healthUiStateCause.reason,
                        isHealthy = healthUiStateCause.isHealthy,
                        description = healthUiStateCause.detailsAsMultilineString,
                        actionText = healthUiStateCause.actionText(context),
                        onAction = { onAction(actionType) }
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun LoadingScreenPreview() {
    TransportHealthInformation()
}

@Preview
@Composable
private fun NotHealthyScreenPreview() {
    TransportHealthInformation(
        onAction = {},
        state = HealthUiState(
            missingPermissions = listOf("FooPermission", "BarPermission"),
            deviceDetails = DeviceDetails(
                modelAndManufacturer = "Samsung Galaxy S22",
                androidVersionDetails = "Android 13 | SDK 33"
            )
        )
    )
}