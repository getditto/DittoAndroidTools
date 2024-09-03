package live.ditto.dittomeshhealthtest

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import live.ditto.Ditto
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
            state = state,
            viewModel = viewModel
        )
    }
}

@Composable
private fun MeshHealthTestScreen(
    state: MeshHealthTestUIState,
    viewModel: MeshHealthTestViewModel
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

    return ObserveList(state, viewModel)
}

@Composable
private fun ObserveList(state: MeshHealthTestUIState, viewModel: MeshHealthTestViewModel){

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




        Button(onClick = {
            Log.d("FOO", ">>>>>>>>>>>>>>>>    ON CLICK")
            viewModel.onStartHealthCheck()
        }


        ) {
            Text(text = "START TEST - make sure all devices under test have this screen open")
        }
    }
}
