package live.ditto.dittotoolsviewer.presentation.viewmodel

import androidx.lifecycle.ViewModel
import live.ditto.dittotoolsviewer.presentation.ToolMenuItem
import live.ditto.dittotoolsviewer.presentation.navigation.Screens

class ToolsViewerViewModel: ViewModel() {

    fun toolsMenuItems(): List<ToolMenuItem> {
        return listOf(
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
                route = Screens.ExportLogsScreen.route
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
    }
}