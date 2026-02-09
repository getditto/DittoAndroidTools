package live.ditto.tools.logviewer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DensitySmall
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import live.ditto.Ditto
import live.ditto.tools.R
import live.ditto.tools.toolsviewer.navigation.Screens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogDetailsScreen(
    navController: NavController,
    ditto: Ditto,
    logDetailsScreenViewModel: LogDetailsScreenViewModel = viewModel(factory = LogDetailsScreenViewModelFactory(ditto, context = LocalContext.current))
) {
    
    val logConfiguration = logDetailsScreenViewModel.logConfiguration
    val logDirectoryInfo = logDetailsScreenViewModel.logDirectoryInfo

    LaunchedEffect(logConfiguration) {
        logDetailsScreenViewModel.getLogConfigurationInfo()
    }

    LaunchedEffect(logDirectoryInfo) {
        logDetailsScreenViewModel.getLogDirInfo()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.log_details)) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .fillMaxHeight()
                .padding(padding)
                .padding(all = 18.dp)
        ) {
            LogInfoCard(stringResource(R.string.log_config), logConfiguration.value)
            LogInfoCard(stringResource(R.string.log_dir_info), logDirectoryInfo.value)

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { navController.navigate(route = Screens.LogViewerScreen.route) }
            ) {
                Icon(Icons.Default.DensitySmall, contentDescription = "Logs")
                Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                Text(stringResource(R.string.log_view))
            }
        }
    }
}

@Composable
fun LogInfoCard(title: String, info: AnnotatedString){
    ElevatedCard(
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        ),
        modifier = Modifier
            .padding(all = 8.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(all = 8.dp)
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = title,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                style = TextStyle(color = Color(0xFF0000FF), fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.padding(3.dp))
            Text(text = info)        }
    }
}