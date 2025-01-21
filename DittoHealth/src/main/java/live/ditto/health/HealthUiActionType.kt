package live.ditto.health

sealed class HealthUiActionType {
    object NoAction : HealthUiActionType()
    object RequestPermissions : HealthUiActionType()
    object EnableWifi : HealthUiActionType()
    object EnableBluetooth : HealthUiActionType()
    object BluetoothUnsupported : HealthUiActionType()
}