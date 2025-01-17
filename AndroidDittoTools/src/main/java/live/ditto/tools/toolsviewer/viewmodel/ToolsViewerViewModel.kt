package live.ditto.tools.toolsviewer.viewmodel

import androidx.lifecycle.ViewModel
import live.ditto.androidtools.R
import live.ditto.tools.toolsviewer.ToolMenuItem
import live.ditto.tools.toolsviewer.navigation.Screens

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
        )
    }
}