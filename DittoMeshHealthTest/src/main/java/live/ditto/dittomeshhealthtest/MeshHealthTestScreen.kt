package live.ditto.dittomeshhealthtest

import android.util.Log
import android.widget.TextView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import live.ditto.Ditto
import live.ditto.dittomeshhealthtest.components.Greeting
import live.ditto.dittomeshhealthtest.components.Loading
import live.ditto.dittomeshhealthtest.theme.MeshHealthTestTheme


@Composable
fun MeshHealthTestScreen(
    ditto: Ditto,
    viewModel: MeshHealthTestViewModel = MeshHealthTestViewModel(
        ditto = ditto,
    ),
) {
    val state by viewModel.state.collectAsState()

    MeshHealthTestTheme {
        MeshHealthTestScreen(
            state = state
        )

    }
}

@Composable
private fun MeshHealthTestScreen(
    state: MeshHealthTestUIState
) {
//    if (state.testState == MeshHealthTestUIState.TestState.OBSERVING) {
//        return ObserveList()
//    }
//    else if(state.testState == MeshHealthTestUIState.TestState.CONFIGURING){
//        return ConfigurationView()
//    }
//    else if(state.testState == MeshHealthTestUIState.TestState.TEST_RUNNING){
//        return TestRunningView()
//    }
//
//    else if(state.testState == MeshHealthTestUIState.TestState.TEST_COMPLETE){
//        return TestCompleteView()
//    }

    return ObserveList(state)
}

@Composable
private fun ObserveList(state: MeshHealthTestUIState){

    Column {

        Row {
            Text(text = "Number of devices present:")
            Text(text = "${state.remotePeers.size}")
        }
        


        //List of peers
        Column {
            Column {
                state.remotePeers.forEach { peer ->
                    Text(text = peer.deviceName)
                    Text(text = peer.peerKeyString)
                }
            }
        }


        Button(onClick = { Log.d("FOO", ">>>>>>>>>>>>>>>>    ON CLICK") }) {
            Text(text = "START TEST")
        }
    }
}
