package live.ditto.health

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import live.ditto.health.components.HealthCheckWithAction
import live.ditto.health.components.HealthCheckWithNoAction
import live.ditto.health.components.Loading

@Composable
fun HealthScreen(
    viewModel: HealthViewModel = HealthViewModel(LocalContext.current),
    healthScreenActionHandler: HealthScreenActionHandler = HealthScreenActionHandler()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    Surface {
        HealthScreen(
            state = state,
            onAction = { actionType ->
                healthScreenActionHandler.handle(actionType = actionType, context = context)
            },
        )
    }
}

@Composable
private fun HealthScreen(
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
        Loading()
    } else {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .verticalScroll(state = rememberScrollState()),
        ) {
            healthUiStateCauses.forEach { healthUiStateCause ->
                val actionType = healthUiStateCause.actionType

                if (actionType == HealthUiActionType.NoAction) {
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

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Preview
@Composable
private fun LoadingScreenPreview() {
    HealthScreen()
}

@Preview
@Composable
private fun NotHealthyScreenPreview() {
    HealthScreen(
        onAction = {},
        state = HealthUiState(
            missingPermissions = listOf("FooPermission", "BarPermission")
        )
    )
}