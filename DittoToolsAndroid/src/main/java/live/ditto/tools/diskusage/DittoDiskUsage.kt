package live.ditto.tools.diskusage

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import live.ditto.Ditto

@Composable
fun DittoDiskUsage(ditto: Ditto) {
    DittoHandler.ditto = ditto

    val navController = rememberNavController()

    // A surface container using the 'background' color from the theme
    Surface(color = MaterialTheme.colorScheme.background) {
        NavHost(navController = navController, startDestination = "diskusage") {
            composable("diskusage") { DiskUsageScreen() }
            composable("diskUsageView") { DiskUsageView(ditto = ditto) }
        }
    }
}