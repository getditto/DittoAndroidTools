package live.ditto.dittodatabrowser

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import live.ditto.Ditto

@Composable
fun DataBrowser(ditto: Ditto) {
    DittoHandler.ditto = ditto

    val navController2 = rememberNavController()

    // A surface container using the 'background' color from the theme
    Surface(color = MaterialTheme.colorScheme.background) {
        NavHost(navController = navController2, startDestination = "collections") {
            composable("collections") { Collections(navController = navController2)}


            composable("documents/{collectionName}/{isStandAlone}") { backStackEntry ->
                val collectionName: String = backStackEntry.arguments?.getString("collectionName").toString()
                val isStandAlone: Boolean = backStackEntry.arguments?.getString("isStandAlone").toBoolean()
                Documents(collectionName = collectionName, isStandAlone = isStandAlone)
            }

        }
    }
}