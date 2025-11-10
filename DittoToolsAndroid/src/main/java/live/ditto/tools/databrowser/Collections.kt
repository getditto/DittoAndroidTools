package live.ditto.tools.databrowser

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import live.ditto.DittoCollection

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter", "UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Collections(navController: NavHostController? = null) {
    val collectionsViewModel: CollectionsViewModel = viewModel()
    val collections: List<DittoCollection> by collectionsViewModel.collections.observeAsState(emptyList())
    var showDialog by remember { mutableStateOf(false) }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Data Browser")
                }
            )
        },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp)
            ) {
                Text(
                    text = "Collections",
                    fontWeight = FontWeight.Bold,
                    fontSize = 30.sp
                )
                Spacer(modifier = Modifier.height(5.dp))
                Button(
                    onClick = {
                        showDialog = true
                    },
                    modifier = Modifier.align(Alignment.Start)
                ) {
                    Text(text = "Start Subscriptions")
                }
                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn {
                    items(collections) { collection ->
                        if (navController != null) {
                            ListItem(
                                collectionName = collection.name,
                                navController = navController,
                                isStandAlone = collectionsViewModel.isStandAlone
                            )
                        }
                    }
                }

                if (showDialog) {
                    AlertDialog(
                        onDismissRequest = { showDialog = false },
                        title = { Text(text = "Stand Alone App?")},
                        text = { Text(text = "Only start subscriptions if using the Data Browser in a stand alone application") },
                        confirmButton = {
                            Button(
                                onClick = {
                                    collectionsViewModel.startSubscription()
                                    showDialog = false }
                            ) {
                                Text(text = "Start")
                            }
                        },
                        dismissButton = {
                            Button(
                                onClick = { showDialog = false }
                            ) {
                                Text(text = "Cancel")
                            }
                        }
                    )
                }
            }
        }
    )
}

@Composable
fun ListItem(collectionName: String, navController: NavHostController, isStandAlone: Boolean) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                navController.navigate("documents/$collectionName/$isStandAlone")
            }
            .padding(10.dp)
    ) {
        Text(
            text = collectionName,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Filled.ArrowForward,
            contentDescription = "Navigate to Item screen",
            modifier = Modifier.padding(end = 16.dp)
        )
    }
}



