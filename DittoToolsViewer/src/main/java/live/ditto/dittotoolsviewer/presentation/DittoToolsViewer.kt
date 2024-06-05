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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dittodiskusage.DittoDiskUsage
import ditto.live.dittopresenceviewer.DittoPresenceViewer
import live.ditto.Ditto
import live.ditto.dittodatabrowser.DittoDataBrowser
import live.ditto.dittoexportlogs.ExportLogs
import live.ditto.dittotoolsviewer.R
import live.ditto.dittotoolsviewer.presentation.navigation.Screens
import live.ditto.health.HealthScreen
import live.ditto.presencedegradationreporter.PresenceDegradationReporterScreen

@Composable
fun DittoToolsViewer(ditto: Ditto) {
    DittoToolsViewerScaffold(ditto = ditto)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DittoToolsViewerScaffold(ditto: Ditto) {
    var showMenu by remember {
        mutableStateOf(false)
    }
    var showExportLogs by remember {
        mutableStateOf(false)
    }

    val navController = rememberNavController()

    val menuItems = listOf(
        ToolMenuItem(
            label = "Presence Viewer",
            route = Screens.PresenceViewerScreen.route
        ),
        ToolMenuItem(
            label = "Data Browser",
            route = Screens.DataBrowserScreen.route
        ),
        ToolMenuItem(
            label = "Export Logs",
            route = Screens.ExportLogsScreen.route,
            onClick = {
                showExportLogs = true
            }
        ),
        ToolMenuItem(
            label = "Disk Usage",
            route = Screens.DiskUsageScreen.route
        ),
        ToolMenuItem(
            label = "Health Viewer",
            route = Screens.HealthScreen.route
        ),
        ToolMenuItem(
            label = "Heartbeat",
            route = Screens.HeartbeatScreen.route
        ),
        ToolMenuItem(
            label = "Presence Degradation Reporter",
            route = Screens.PresenceDegradationReporterScreen.route
        ),
    )

    Scaffold(
        floatingActionButton = {
            MenuFloatingActionButton {
                showMenu = true
            }
        }
    ) { contentPadding ->
        if (showMenu) {
            ToolsMenu(
                navController = navController,
                menuItems = menuItems,
                onDismissRequest = { showMenu = false }
            )
        }

        NavHost(navController = navController, startDestination = Screens.MainScreen.route) {
            composable(Screens.MainScreen.route) {
                MainScreen(
                    modifier = Modifier.padding(contentPadding)
                )
            }
            composable(Screens.PresenceViewerScreen.route) {
                DittoPresenceViewer(ditto = ditto)
            }
            composable(Screens.DataBrowserScreen.route) {
                DittoDataBrowser(ditto = ditto)
            }
            composable(Screens.ExportLogsScreen.route) {
                if (showExportLogs) {
                    ExportLogs(
                        onDismiss = {
                            showExportLogs = false
                            navController.popBackStack()
                        }
                    )
                }
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
    MenuFloatingActionButton(
        onClick = { }
    )
}