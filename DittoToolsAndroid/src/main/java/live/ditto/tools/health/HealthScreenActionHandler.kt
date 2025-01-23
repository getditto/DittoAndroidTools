package live.ditto.tools.health

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

class HealthScreenActionHandler {
    fun handle(actionType: HealthUiActionType, context: Context) = when (actionType) {
        HealthUiActionType.NoAction -> {}
        HealthUiActionType.EnableWifi -> {
            context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
        }

        HealthUiActionType.RequestPermissions -> {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", context.packageName, null)
            intent.data = uri

            context.startActivity(intent)
        }

        HealthUiActionType.EnableBluetooth -> {
            context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
        }
    }
}