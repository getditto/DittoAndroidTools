package live.ditto.tools.health.usecase

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

class GetWifiAwareStatusUseCase(private val context: Context) {

    operator fun invoke(): WifiAwareState {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (context.packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI_AWARE)) {
                WifiAwareState.SUPPORTED
            } else {
                WifiAwareState.UNSUPPORTED
            }
        } else {
            WifiAwareState.UNSUPPORTED_ANDROID_VERSION
        }
    }
}

enum class WifiAwareState {
    SUPPORTED,
    UNSUPPORTED,
    UNSUPPORTED_ANDROID_VERSION
}