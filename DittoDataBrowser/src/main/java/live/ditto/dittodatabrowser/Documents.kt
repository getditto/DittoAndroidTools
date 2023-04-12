package live.ditto.dittodatabrowser

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController

@Composable
fun Documents(navController: NavHostController? = null, collectionName: String, isStandAlone: Boolean) {

    val viewModel: DocumentsViewModel =
        viewModel(factory = DocumentsViewModel.MyViewModelFactory(collectionName, isStandAlone))
    var showMenu by remember { mutableStateOf(false) }

    val selectedDoc by viewModel.selectedDoc.observeAsState()
    val docsList by viewModel.docsList.observeAsState()
    var selectedIndex by remember { mutableStateOf(0) }
    var startUp by remember { mutableStateOf(true) }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(text = "Collection: $collectionName", style = MaterialTheme.typography.h4)
            Spacer(modifier = Modifier.height(16.dp))
            SearchBar(onSearch = { searchText ->
                // Call the search function in the view model
                viewModel.filterDocs(searchText)
                selectedIndex = 0
            })
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Docs count: ${docsList?.size}")
            Spacer(modifier = Modifier.height(16.dp))

        Row {
            Text(
                text = "Doc ID:  ",
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .clickable {

                    }
            )

            Box {
                // Show selected item or "None" if no item is selected
                (if ((startUp)) "None" else docsList?.get(selectedIndex)?.id)?.let {
                    Text(
                        text = it,
                        textAlign = TextAlign.Start,
                        color = Color.Blue,
                        modifier = Modifier
                            .clickable {
                                showMenu = true
                                startUp = false
                            }
                    )
                }

                // Dropdown menu
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    docsList?.forEachIndexed { index, item ->
                        DropdownMenuItem(onClick = {
                            selectedIndex = index
                            viewModel.selectedDoc.value = item
                        }) {
                            Text(text = item.id)
                        }
                    }
                }
            }
        }

        Divider()

        LazyColumn {
            items(viewModel.docProperties.value?.map { it } ?: emptyList()) { property ->
                selectedDoc?.let {
                    DocItem(
                        property = property,
                        viewModel = viewModel,
                        selectedDoc = it
                    )
                }
            }
        }
    }
}

@Composable
fun DocItem(property: String, viewModel: DocumentsViewModel, selectedDoc: Document) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
            }
            .padding(10.dp)
    ) {
        Text(
            text = property,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f)
        )
        val doc: Document? = viewModel.docsList.value?.find {
            it == selectedDoc
        }
        if (doc != null) {
            Text(doc.properties[property].toString())
        }
    }
}

@Composable
fun SearchBar(onSearch: (String) -> Unit) {
    var searchText by remember { mutableStateOf("") }

    Row(verticalAlignment = Alignment.CenterVertically) {
        TextField(
            modifier = Modifier.weight(1f),
            value = searchText,
            onValueChange = { searchText = it },
            placeholder = { Text("Search") }
        )

        IconButton(onClick = { onSearch(searchText) }) {
            Icon(Icons.Default.Search, contentDescription = "Search")
        }
    }
}
