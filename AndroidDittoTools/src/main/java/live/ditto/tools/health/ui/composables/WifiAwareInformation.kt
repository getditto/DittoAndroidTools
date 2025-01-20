package live.ditto.tools.health.ui.composables

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import live.ditto.androidtools.R
import live.ditto.health.theme.wifiAwareAvailableColor
import live.ditto.health.theme.wifiAwareUnavailableColor
import live.ditto.health.usecase.WifiAwareState
import live.ditto.tools.health.data.DeviceDetails
import live.ditto.tools.health.ui.viewmodel.HealthViewModel

@Composable
internal fun WifiAwareInformation(modifier: Modifier = Modifier) {
    WifiAwareInformationComposable(modifier = modifier)
}

@Composable
private fun WifiAwareInformationComposable(
    modifier: Modifier = Modifier,
    viewModel: HealthViewModel = HealthViewModel(LocalContext.current)
) {

    val state by viewModel.state.collectAsStateWithLifecycle()

    val uriHandler = LocalUriHandler.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        WifiAwareImageState(wifiAwareState = state.wifiAwareState)
        DeviceDetails(deviceDetails = state.deviceDetails)
        Button(
            onClick = {
                viewModel.openLearnMoreLink(uriHandler)
            }
        ) {
            Text(text = stringResource(R.string.button_learn_more_label))
        }
    }
}

@Composable
private fun DeviceDetails(deviceDetails: DeviceDetails) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = deviceDetails.modelAndManufacturer,
            textAlign = TextAlign.Start
        )
        Text(
            text = deviceDetails.androidVersionDetails,
            textAlign = TextAlign.Start
        )
    }
}

@Composable
private fun WifiAwareImageState(wifiAwareState: WifiAwareState) {
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

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            modifier = Modifier
                .height(100.dp)
                .width(100.dp),
            painter = painterResource(id = imageResource),
            contentDescription = stringResource(id = wifiAwareStateDescription),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(id = wifiAwareStateDescription),
            color = stateColor,
            fontSize = 16.sp
        )

    }
}