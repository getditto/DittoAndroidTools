package live.ditto.dittodatabrowser

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import live.ditto.Ditto

@Composable
fun DataBrowser(navController: NavHostController, ditto: Ditto) {
    DittoHandler.ditto = ditto

    val navController2 = rememberNavController()

    // A surface container using the 'background' color from the theme
    Surface(color = MaterialTheme.colors.background) {
        NavHost(navController = navController2, startDestination = "collections") {
            composable("collections") { Collections(navController = navController2)}


            composable("documents/{collectionName}/{isStandAlone}") { backStackEntry ->
                val collectionName: String = backStackEntry.arguments?.getString("collectionName").toString()
                val isStandAlone: Boolean = backStackEntry.arguments?.getString("isStandAlone").toBoolean()
                Documents(navController = navController2, collectionName = collectionName, isStandAlone = isStandAlone)
            }

        }
    }
}