package live.ditto.tools.toolsviewer

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import live.ditto.Ditto
import live.ditto.tools.R
import live.ditto.tools.databrowser.DittoDataBrowser
import live.ditto.tools.diskusage.DittoDiskUsage
import live.ditto.tools.exportlogs.ExportLogs
import live.ditto.tools.exportlogs.ExportLogsToPortal
import live.ditto.tools.health.ui.composables.HealthScreen
import live.ditto.tools.peerslist.PeersListViewer
import live.ditto.tools.presencedegradationreporter.PresenceDegradationReporterScreen
import live.ditto.tools.presenceviewer.DittoPresenceViewer
import live.ditto.tools.toolsviewer.navigation.Screens
import live.ditto.tools.toolsviewer.viewmodel.ToolsViewerViewModel
import java.io.File

/**
 * A Composable that you can include in your app that will give a single entry point for all Ditto
 * Tools.
 *
 * @param modifier an optional modifier if you need to adjust the layout to fit the view
 * @param ditto your instance of [Ditto] that is required
 * @param onExitTools an optional lambda function that will be called whenever a user taps the
 * "Exit Tools" button. Use this to do any back navigation or dismissal/hiding of the Tools Viewer
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DittoToolsViewer(
    modifier: Modifier = Modifier,
    ditto: Ditto,
    onExitTools: () -> Unit = { },
    onExport: ((File) -> Unit)? = null
) {
    DittoToolsViewerScaffold(
        modifier = modifier,
        ditto = ditto,
        onExitTools = onExitTools,
        onExport = onExport
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun DittoToolsViewerScaffold(
    modifier: Modifier,
    ditto: Ditto,
    onExitTools: () -> Unit,
    viewModel: ToolsViewerViewModel = ToolsViewerViewModel(),
    onExport: ((File) -> Unit)? = null
) {

    val navController = rememberNavController()

    Scaffold(
        modifier = modifier,
        bottomBar = {
            BottomAppBar(
                actions = {
                    Button(onClick = { onExitTools() }) {
                        Text(text = "Exit Tools")
                    }
                },
                floatingActionButton = {
                    MenuFloatingActionButton {
                        if (navController.currentDestination?.route != Screens.MainScreen.route) {
                            navController.popBackStack()
                        }
                    }
                }
            )
        }
        ) { contentPadding ->
        ToolsViewerContent(
            navController = navController,
            viewModel = viewModel,
            contentPadding = contentPadding,
            ditto = ditto,
            onExport = onExport
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun ToolsViewerContent(
    navController: NavHostController,
    viewModel: ToolsViewerViewModel,
    contentPadding: PaddingValues,
    ditto: Ditto,
    onExport: ((File) -> Unit)? = null
) {
    ToolsViewerNavHost(
        navController = navController,
        contentPadding = contentPadding,
        ditto = ditto,
        toolMenuItems = viewModel.toolsMenuItems(),
        onExport = onExport
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun ToolsViewerNavHost(
    navController: NavHostController,
    contentPadding: PaddingValues,
    ditto: Ditto,
    toolMenuItems: List<ToolMenuItem>,
    onExport: ((File) -> Unit)? = null
) {
    NavHost(
        modifier = Modifier.padding(contentPadding),
        navController = navController,
        startDestination = Screens.MainScreen.route,
    ) {
        composable(Screens.MainScreen.route) {
            ToolsMenu(
                navController = navController,
                menuItems = toolMenuItems,
            )
        }
        composable(Screens.PresenceViewerScreen.route) {
            DittoPresenceViewer(ditto = ditto)
        }
        composable(Screens.PeersListViewerScreen.route) {
            PeersListViewer(ditto = ditto)
        }
        composable(Screens.DataBrowserScreen.route) {
            DittoDataBrowser(ditto = ditto)
        }
        composable(Screens.ExportLogsScreen.route) {
            ExportLogs(
                onDismiss = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screens.ExportLogsToPortalScreen.route) {
            ExportLogsToPortal(
                ditto = ditto,
                onDismiss = { navController.popBackStack() })
        }
        composable(Screens.DiskUsageScreen.route) {
            DittoDiskUsage(ditto = ditto, onExport = onExport)
        }
        composable(Screens.HealthScreen.route) {
            HealthScreen()
        }
        composable(Screens.HeartbeatScreen.route) {
            HeartbeatScreen(ditto = ditto)
        }
        composable(Screens.PresenceDegradationReporterScreen.route) {
            PresenceDegradationReporterScreen(ditto = ditto)
        }
    }
}

@Composable
private fun MenuFloatingActionButton(onClick: () -> Unit) {
    ExtendedFloatingActionButton(
        onClick = { onClick() },
        icon = { Icon(Icons.Filled.Build, stringResource(R.string.tools_menu_content_description)) },
        text = { Text(text = stringResource(R.string.tools_menu)) },
        modifier = Modifier.onKeyEvent { keyEvent ->
            when (keyEvent.key) {
                Key.Spacebar -> {
                    when (keyEvent.type) {
                        KeyEventType.KeyUp -> {
                            onClick()
                            true
                        }
                        else -> false
                    }
                }
                else -> false
            }
        }
    )
}

@Preview
@Composable
private fun MenuFloatingActionButtonPreview() {
    MenuFloatingActionButton(
        onClick = { }
    )
}
