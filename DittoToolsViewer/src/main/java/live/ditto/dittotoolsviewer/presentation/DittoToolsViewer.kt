package live.ditto.dittotoolsviewer.presentation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ditto.live.dittopresenceviewer.DittoPresenceViewer
import live.ditto.Ditto
import live.ditto.dittodatabrowser.DittoDataBrowser
import live.ditto.dittodiskusage.DittoDiskUsage
import live.ditto.dittoexportlogs.ExportLogs
import live.ditto.dittotoolsviewer.R
import live.ditto.dittotoolsviewer.presentation.navigation.Screens
import live.ditto.dittotoolsviewer.presentation.viewmodel.ToolsViewerViewModel
import live.ditto.health.HealthScreen
import live.ditto.presencedegradationreporter.PresenceDegradationReporterScreen

@Composable
fun DittoToolsViewer(
    ditto: Ditto,
    onExitTools: () -> Unit
) {
    DittoToolsViewerScaffold(
        ditto = ditto,
        onExitTools = onExitTools
    )
}

@Composable
private fun DittoToolsViewerScaffold(
    ditto: Ditto,
    onExitTools: () -> Unit,
    viewModel: ToolsViewerViewModel = ToolsViewerViewModel()
) {
    var showMenu by remember {
        mutableStateOf(false)
    }

    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            BottomAppBar(
                actions = {},
                floatingActionButton = {
                    MenuFloatingActionButton {
                        showMenu = true
                    }
                }
            )
        }
        ) { contentPadding ->
        ToolsViewerContent(
            showMenu = showMenu,
            onToolsMenuDismiss = { showMenu = false },
            navController = navController,
            viewModel = viewModel,
            contentPadding = contentPadding,
            ditto = ditto,
            onExitTools = onExitTools
        )
    }
}

@Composable
private fun ToolsViewerContent(
    showMenu: Boolean,
    onToolsMenuDismiss: () -> Unit,
    navController: NavHostController,
    viewModel: ToolsViewerViewModel,
    contentPadding: PaddingValues,
    ditto: Ditto,
    onExitTools: () -> Unit
) {
    if (showMenu) {
        ToolsMenu(
            navController = navController,
            menuItems = viewModel.toolsMenuItems(),
            onExit = onExitTools,
            onDismissRequest = {
                onToolsMenuDismiss()
            }
        )
    }

    ToolsViewerNavHost(
        navController = navController,
        contentPadding = contentPadding,
        ditto = ditto
    )
}

@Composable
private fun ToolsViewerNavHost(
    navController: NavHostController,
    contentPadding: PaddingValues,
    ditto: Ditto
) {
    NavHost(
        modifier = Modifier.padding(contentPadding),
        navController = navController,
        startDestination = Screens.MainScreen.route,
    ) {
        composable(Screens.MainScreen.route) {
            MainScreen()
        }
        composable(Screens.PresenceViewerScreen.route) {
            DittoPresenceViewer(ditto = ditto)
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