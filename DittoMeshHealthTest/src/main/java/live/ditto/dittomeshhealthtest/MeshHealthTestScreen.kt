package live.ditto.dittomeshhealthtest

import android.widget.TextView
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
    return Greeting("Hello world")
}

