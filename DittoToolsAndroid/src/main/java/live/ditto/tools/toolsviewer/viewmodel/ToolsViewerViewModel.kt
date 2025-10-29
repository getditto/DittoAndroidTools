package live.ditto.tools.toolsviewer.viewmodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Warning
import androidx.lifecycle.ViewModel
import live.ditto.tools.R
import live.ditto.tools.toolsviewer.ToolMenuItem
import live.ditto.tools.toolsviewer.ToolMenuSection
import live.ditto.tools.toolsviewer.navigation.Screens

class ToolsViewerViewModel: ViewModel() {

    fun toolsMenuSections(): List<ToolMenuSection> {
        return listOf(
            ToolMenuSection(
                title = "Network",
                items = listOf(
                    ToolMenuItem(
                        label = R.string.presence_viewer_tool_label,
                        route = Screens.PresenceViewerScreen.route,
                        icon = Icons.Default.Hub
                    ),
                    ToolMenuItem(
                        label = R.string.peers_list_tool_label,
                        route = Screens.PeersListViewerScreen.route,
                        icon = Icons.AutoMirrored.Filled.List
                    ),
                    ToolMenuItem(
                        label = R.string.presence_degradation_reporter_tool_label,
                        route = Screens.PresenceDegradationReporterScreen.route,
                        icon = Icons.Default.Warning
                    ),
                    ToolMenuItem(
                        label = R.string.heartbeat_tool_label,
                        route = Screens.HeartbeatScreen.route,
                        icon = Icons.Default.MonitorHeart
                    )
                )
            ),
            ToolMenuSection(
                title = "System",
                items = listOf(
                    ToolMenuItem(
                        label = R.string.health_viewer_tool_label,
                        route = Screens.HealthScreen.route,
                        icon = Icons.Default.HealthAndSafety
                    ),
                    ToolMenuItem(
                        label = R.string.disk_usage_tool_label,
                        route = Screens.DiskUsageScreen.route,
                        icon = Icons.Default.Storage
                    )
                )
            ),
            ToolMenuSection(
                title = "Data & Debugging",
                items = listOf(
                    ToolMenuItem(
                        label = R.string.data_browser_tool_label,
                        route = Screens.DataBrowserScreen.route,
                        icon = Icons.Default.Search
                    ),
                    ToolMenuItem(
                        label = R.string.export_logs_to_portal_tool_label,
                        route = Screens.ExportLogsToPortalScreen.route,
                        icon = Icons.Default.CloudUpload
                    ),
                    ToolMenuItem(
                        label = R.string.export_logs_tool_label,
                        route = Screens.ExportLogsScreen.route,
                        icon = Icons.Default.Download
                    )
                )
            )
        )
    }
}