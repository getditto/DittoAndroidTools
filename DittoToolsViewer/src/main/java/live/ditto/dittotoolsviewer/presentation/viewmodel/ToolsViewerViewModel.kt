package live.ditto.dittotoolsviewer.presentation.viewmodel

import androidx.lifecycle.ViewModel
import live.ditto.dittotoolsviewer.R
import live.ditto.dittotoolsviewer.presentation.ToolMenuItem
import live.ditto.dittotoolsviewer.presentation.navigation.Screens

class ToolsViewerViewModel: ViewModel() {

    fun toolsMenuItems(): List<ToolMenuItem> {
        return listOf(
            ToolMenuItem(
                label = R.string.presence_viewer_tool_label,
                route = Screens.PresenceViewerScreen.route
            ),
            ToolMenuItem(
                label = R.string.data_browser_tool_label,
                route = Screens.DataBrowserScreen.route
            ),
            ToolMenuItem(
                label = R.string.export_logs_tool_label,
                route = Screens.ExportLogsScreen.route
            ),
            ToolMenuItem(
                label = R.string.disk_usage_tool_label,
                route = Screens.DiskUsageScreen.route
            ),
            ToolMenuItem(
                label = R.string.health_viewer_tool_label,
                route = Screens.HealthScreen.route
            ),
            ToolMenuItem(
                label = R.string.heartbeat_tool_label,
                route = Screens.HeartbeatScreen.route
            ),
            ToolMenuItem(
                label = R.string.presence_degradation_reporter_tool_label,
                route = Screens.PresenceDegradationReporterScreen.route
            ),
            ToolMenuItem(
                label = R.string.mesh_health_test,
                route = Screens.MeshHealthTestScreen.route
            )
        )
    }
}