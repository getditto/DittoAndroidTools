package live.ditto.health.ui.composables

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import live.ditto.health.HealthViewModel
import live.ditto.health.R
import live.ditto.health.theme.wifiAwareAvailableColor
import live.ditto.health.theme.wifiAwareUnavailableColor
import live.ditto.health.usecase.WifiAwareState

@Composable
fun WifiAwareInformation(modifier: Modifier = Modifier) {
    WifiAwareInformationComposable(modifier = modifier)
}

@Composable
internal fun WifiAwareInformationComposable(
    modifier: Modifier = Modifier,
    viewModel: HealthViewModel = HealthViewModel(LocalContext.current)
) {

    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxWidth()) {
        WifiAwareImageState(wifiAwareState = state.wifiAwareState)
    }
}

@Composable
fun WifiAwareImageState(wifiAwareState: WifiAwareState) {
    @DrawableRes val imageResource: Int = when (wifiAwareState) {
        WifiAwareState.SUPPORTED -> R.drawable.ic_wifi_yes
        WifiAwareState.UNSUPPORTED, WifiAwareState.UNSUPPORTED_ANDROID_VERSION -> R.drawable.ic_wifi_no
    }

    @StringRes val wifiAwareStateDescription: Int = when (wifiAwareState) {
        WifiAwareState.SUPPORTED -> R.string.wifi_aware_supported
        WifiAwareState.UNSUPPORTED -> R.string.wifi_aware_unsupported
        WifiAwareState.UNSUPPORTED_ANDROID_VERSION -> R.string.wifi_aware_unsupported_android_version
    }

    val stateColor = when (wifiAwareState) {
        WifiAwareState.SUPPORTED -> wifiAwareAvailableColor
        WifiAwareState.UNSUPPORTED, WifiAwareState.UNSUPPORTED_ANDROID_VERSION -> wifiAwareUnavailableColor
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Image(
            painter = painterResource(id = imageResource),
            contentDescription = stringResource(id = wifiAwareStateDescription),
            alignment = Alignment.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(id = wifiAwareStateDescription),
            color = stateColor,
            fontSize = 16.sp
        )

    }
}