package live.ditto.dittotoolsviewer.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import live.ditto.dittotoolsviewer.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DittoToolsViewer() {
    Scaffold(
        floatingActionButton = {
            MenuFloatingActionButton {
                //todo: toggle menu
            }
        }
    ) {
        MainScreen(modifier = Modifier.padding(it))
    }
}

@Composable
private fun MenuFloatingActionButton(onClick: () -> Unit) {
    ExtendedFloatingActionButton(
        onClick = { onClick() },
        icon = { Icon(Icons.Filled.Menu, stringResource(R.string.tools_menu_content_description)) },
        text = { Text(text = stringResource(R.string.tools_menu)) }
    )
}

@Preview
@Composable
private fun MenuFloatingActionButtonPreview() {
    MenuFloatingActionButton {
        // no op
    }
}

@Preview
@Composable
private fun DittoToolsViewerPreview() {
    DittoToolsViewer()
}