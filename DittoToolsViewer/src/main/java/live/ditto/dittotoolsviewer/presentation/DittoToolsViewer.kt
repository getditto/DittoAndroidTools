package live.ditto.dittotoolsviewer.presentation

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import live.ditto.tools.presenceviewer.DittoPresenceViewer
import live.ditto.Ditto
import live.ditto.dittodatabrowser.DittoDataBrowser
import live.ditto.dittodiskusage.DittoDiskUsage
import live.ditto.dittoexportlogs.ExportLogs
import live.ditto.dittotoolsviewer.R
import live.ditto.dittotoolsviewer.presentation.navigation.Screens
import live.ditto.dittotoolsviewer.presentation.viewmodel.ToolsViewerViewModel
import live.ditto.health.ui.composables.HealthScreen
import live.ditto.presencedegradationreporter.PresenceDegradationReporterScreen

/**
 * A Composable that you can include in your app that will give a single entry point for all Ditto
 * Tools.
 *
 * @param modifier an optional modifier if you need to adjust the layout to fit the view
 * @param ditto your instance of [Ditto] that is required
 * @param onExitTools an optional lambda function that will be called whenever a user taps the
 * "Exit Tools" button. Use this to do any back navigation or dismissal/hiding of the Tools Viewer
 */
@Composable
fun DittoToolsViewer(
    modifier: Modifier = Modifier,
    ditto: Ditto,
    onExitTools: () -> Unit = { }
) {
    DittoToolsViewerScaffold(
        modifier = modifier,
        ditto = ditto,
        onExitTools = onExitTools
    )
}

@Composable
private fun DittoToolsViewerScaffold(
    modifier: Modifier,
    ditto: Ditto,
    onExitTools: () -> Unit,
    viewModel: ToolsViewerViewModel = ToolsViewerViewModel()
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
            ditto = ditto
        )
    }
}

@Composable
private fun ToolsViewerContent(
    navController: NavHostController,
    viewModel: ToolsViewerViewModel,
    contentPadding: PaddingValues,
    ditto: Ditto,
) {
    ToolsViewerNavHost(
        navController = navController,
        contentPadding = contentPadding,
        ditto = ditto,
        toolMenuItems = viewModel.toolsMenuItems()
    )
}

@Composable
private fun ToolsViewerNavHost(
    navController: NavHostController,
    contentPadding: PaddingValues,
    ditto: Ditto,
    toolMenuItems: List<ToolMenuItem>
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
            live.ditto.tools.presenceviewer.DittoPresenceViewer(ditto = ditto)
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
        composable(Screens.DiskUsageScreen.route) {
            DittoDiskUsage(ditto = ditto)
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
        text = { Text(text = stringResource(R.string.tools_menu)) }
    )
}

@Preview
@Composable
private fun MenuFloatingActionButtonPreview() {
    MenuFloatingActionButton(
        onClick = { }
    )
}