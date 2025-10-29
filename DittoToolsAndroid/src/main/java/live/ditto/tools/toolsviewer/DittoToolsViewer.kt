package live.ditto.tools.toolsviewer

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
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
    onExitTools: () -> Unit = { }
) {
    DittoToolsViewerScaffold(
        modifier = modifier,
        ditto = ditto,
        onExitTools = onExitTools
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun DittoToolsViewerScaffold(
    modifier: Modifier,
    ditto: Ditto,
    onExitTools: () -> Unit,
    viewModel: ToolsViewerViewModel = ToolsViewerViewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isMainScreen = currentRoute == Screens.MainScreen.route

    // Read parent app's status bar color
    val view = LocalView.current
    val statusBarColor = remember {
        if (!view.isInEditMode) {
            val colorInt = (view.context as? android.app.Activity)?.window?.statusBarColor
            colorInt?.let { Color(it) } ?: Color(0xFF6200EE) // Fallback to material purple
        } else {
            Color(0xFF6200EE)
        }
    }

    // Determine if we should use light or dark content based on background luminance
    val useLightContent = statusBarColor.luminance() < 0.5f
    val contentColor = if (useLightContent) Color.White else Color.Black

    // Hardcoded background color that adapts to dark mode
    val isSystemInDarkTheme = androidx.compose.foundation.isSystemInDarkTheme()
    val backgroundColor = if (isSystemInDarkTheme) Color(0xFF1C1C1E) else Color(0xFFF2F2F7)

    val handleBackNavigation: () -> Unit = {
        if (isMainScreen) {
            onExitTools()
        } else {
            navController.popBackStack()
        }
    }

    // Handle system back button
    BackHandler(enabled = true, onBack = handleBackNavigation)

    Scaffold(
        modifier = modifier,
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentRoute) {
                            Screens.MainScreen.route -> stringResource(R.string.tools_menu_title)
                            Screens.PresenceViewerScreen.route -> stringResource(R.string.presence_viewer_tool_label)
                            Screens.PeersListViewerScreen.route -> stringResource(R.string.peers_list_tool_label)
                            Screens.DataBrowserScreen.route -> stringResource(R.string.data_browser_tool_label)
                            Screens.ExportLogsScreen.route -> stringResource(R.string.export_logs_tool_label)
                            Screens.ExportLogsToPortalScreen.route -> stringResource(R.string.export_logs_to_portal_tool_label)
                            Screens.DiskUsageScreen.route -> stringResource(R.string.disk_usage_tool_label)
                            Screens.HealthScreen.route -> stringResource(R.string.health_viewer_tool_label)
                            Screens.HeartbeatScreen.route -> stringResource(R.string.heartbeat_tool_label)
                            Screens.PresenceDegradationReporterScreen.route -> stringResource(R.string.presence_degradation_reporter_tool_label)
                            else -> stringResource(R.string.tools_menu_title)
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = handleBackNavigation) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = if (isMainScreen) {
                                stringResource(R.string.exit_tools_content_description)
                            } else {
                                stringResource(R.string.back_button_content_description)
                            }
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onExitTools) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.exit_tools_content_description)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = statusBarColor,
                    titleContentColor = contentColor,
                    navigationIconContentColor = contentColor,
                    actionIconContentColor = contentColor
                )
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

@RequiresApi(Build.VERSION_CODES.O)
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
        toolMenuSections = viewModel.toolsMenuSections()
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun ToolsViewerNavHost(
    navController: NavHostController,
    contentPadding: PaddingValues,
    ditto: Ditto,
    toolMenuSections: List<ToolMenuSection>
) {
    NavHost(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = contentPadding.calculateTopPadding()),
        navController = navController,
        startDestination = Screens.MainScreen.route,
    ) {
        composable(Screens.MainScreen.route) {
            ToolsMenu(
                navController = navController,
                menuSections = toolMenuSections,
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

