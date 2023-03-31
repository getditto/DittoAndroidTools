package com.example.dittodiskusage

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import live.ditto.Ditto
import com.example.dittodiskusage.DiskUsageScreen

@Composable
fun DittoDiskUsage(navController: NavHostController, ditto: Ditto) {
    DittoHandler.ditto = ditto

    val navController2 = rememberNavController()

    // A surface container using the 'background' color from the theme
    Surface(color = MaterialTheme.colors.background) {
        NavHost(navController = navController2, startDestination = "diskusage") {
            composable("diskusage") { DiskUsageScreen(navController = navController) }
            composable("diskUsageView") { DiskUsageView(navController = navController) }

        }
    }
}