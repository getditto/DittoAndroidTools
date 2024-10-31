package live.ditto.health.ui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HealthScreen(
    modifier: Modifier = Modifier,
    displayList: List<HealthScreenSections> = HealthScreenSections.values().toList()
) {
    Column(
        modifier = modifier
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        displayList.forEach { screen ->
            ComposableForScreen(
                screen = screen
            )
        }
    }
}

@Composable
private fun ComposableForScreen(
    modifier: Modifier = Modifier,
    screen: HealthScreenSections
) {
    when (screen) {
        HealthScreenSections.TRANSPORT_HEALTH -> TransportHealthInformation(modifier = modifier)
        HealthScreenSections.WIFI_AWARE_STATE -> WifiAwareInformation(modifier = modifier)
    }
}

enum class HealthScreenSections {
    TRANSPORT_HEALTH,
    WIFI_AWARE_STATE
}
