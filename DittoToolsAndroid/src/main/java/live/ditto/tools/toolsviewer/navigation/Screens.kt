package live.ditto.tools.toolsviewer.navigation

interface Screen {
    val route: String
}

sealed class Screens {
    object MainScreen: Screen {
        override val route = "mainScreen"
    }
    object PresenceViewerScreen: Screen {
        override val route = "presenceViewer"
    }
    object DataBrowserScreen: Screen {
        override val route = "dataBrowser"
    }
    object ExportLogsScreen: Screen {
        override val route = "exportLogs"
    }
    object DiskUsageScreen: Screen {
        override val route = "diskUsage"
    }
    object HealthScreen: Screen {
        override val route = "health"
    }
    object HeartbeatScreen: Screen {
        override val route = "heartbeat"
    }
    object PresenceDegradationReporterScreen: Screen {
        override val route = "presenceDegradationReporter"
    }
}