package live.ditto.health.usecase

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch


class GetBluetoothStatusFlow(context: Context) {
    private val bluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

    private val state = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                launch { send(determineBluetoothState(bluetoothManager = bluetoothManager)) }
            }
        }

        context.registerReceiver(receiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))

        if (bluetoothManager.adapter == null) {
            send(BluetoothState.UNSUPPORTED)
        } else {
            if (bluetoothManager.adapter.isEnabled) {
                send(BluetoothState.ENABLED)
            } else {
                send(BluetoothState.DISABLED)
            }
        }

        awaitClose {
            context.unregisterReceiver(receiver)
        }
    }

    operator fun invoke() = state

    private fun determineBluetoothState(bluetoothManager: BluetoothManager): BluetoothState {
        return if (bluetoothManager.adapter == null) {
            BluetoothState.UNSUPPORTED
        } else {
            if (bluetoothManager.adapter.isEnabled) {
                BluetoothState.ENABLED
            } else {
                BluetoothState.DISABLED
            }
        }
    }
}

enum class BluetoothState {
    ENABLED,
    DISABLED,
    UNSUPPORTED
}