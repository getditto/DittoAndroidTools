package live.ditto.tools.health.ui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
            .padding(8.dp)
            .verticalScroll(state = rememberScrollState()),
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

/**
 * Enum class for each of the various health state screens.
 */
enum class HealthScreenSections {
    /**
     * Screen that shows the state of the various transports (Wifi/Bluetooth) used by Ditto and
     * their permissions state.
     */
    TRANSPORT_HEALTH,

    /**
     * Screen that displays WiFi Aware information for the device.
     */
    WIFI_AWARE_STATE
}
