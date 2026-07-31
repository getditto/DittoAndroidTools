package com.ditto.tools.databrowser

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
internal fun Documents(collectionName: String) {

    val viewModel: DocumentsViewModel =
        viewModel(factory = DocumentsViewModel.DocumentsViewModelFactory(collectionName))
    var showMenu by remember { mutableStateOf(false) }

    val selectedDoc by viewModel.selectedDoc.observeAsState()
    val docsList by viewModel.docsList.observeAsState()
    val errorMessage by viewModel.errorMessage.observeAsState()
    val queryStatus by viewModel.queryStatus.observeAsState("Loading documents…")
    var selectedIndex by remember { mutableStateOf(0) }

    LaunchedEffect(docsList) {
        val documents = docsList.orEmpty()
        selectedIndex = selectedDocumentIndex(documents, selectedDoc?.id) ?: 0
        viewModel.selectedDoc.value = documents.getOrNull(selectedIndex)
    }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Collection: $collectionName",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Query documents",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            SearchBar(onSearch = { searchText ->
                viewModel.filterDocs(searchText)
            })
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Enter a DQL WHERE expression, for example: color = 'blue'. " +
                    "Leave it blank to show all documents.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = queryStatus,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))

            errorMessage?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Text(text = "Docs count: ${docsList?.size ?: "Loading..."}")
            Spacer(modifier = Modifier.height(16.dp))

            Row {
                Text(
                    text = "Doc ID:  ",
                    textAlign = TextAlign.Start
                )

                if (!docsList.isNullOrEmpty()) {
                    Box {
                        docsList?.getOrNull(selectedIndex)?.id?.let { docId ->
                            Text(
                                text = docId,
                                textAlign = TextAlign.Start,
                                color = Color.Blue,
                                modifier = Modifier
                                    .clickable {
                                        showMenu = true
                                    }
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            docsList?.forEachIndexed { index, item ->
                                DropdownMenuItem(onClick = {
                                    selectedIndex = index
                                    viewModel.selectedDoc.value = item
                                    showMenu = false
                                }, text = {
                                    Text(text = item.id)
                                })
                            }
                        }
                    }
                } else {
                    Text(
                        text = "No Docs",
                        textAlign = TextAlign.Start,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Divider()

            LazyColumn {
                selectedDoc?.let { document ->
                    items(document.properties.keys.sorted()) { property ->
                        DocItem(
                            property = property,
                            selectedDoc = document
                        )
                    }
                }
            }
        }
}

@Composable
internal fun DocItem(property: String, selectedDoc: Document) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
    ) {
        Text(
            text = property,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = formatDisplayValue(selectedDoc.properties[property]),
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
internal fun SearchBar(onSearch: (String) -> Unit) {
    var searchText by remember { mutableStateOf(TextFieldValue("")) }
    val focusManager = LocalFocusManager.current
    val runSearch = {
        onSearch(searchText.text)
        focusManager.clearFocus()
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicTextField(
            modifier = Modifier
                .weight(1f)
                .height(56.dp)
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outline,
                    RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 16.dp),
            value = searchText,
            onValueChange = { searchText = it },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            keyboardOptions = KeyboardOptions.Default.copy(
                capitalization = KeyboardCapitalization.None,
                autoCorrect = false,
                keyboardType = KeyboardType.Ascii,
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = { runSearch() }
            ),
            decorationBox = { innerTextField ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (searchText.text.isEmpty()) {
                        Text(
                            text = "color = 'blue'",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    innerTextField()
                }
            }
        )
        Spacer(modifier = Modifier.width(8.dp))
        Button(onClick = runSearch) {
            Text("Run")
        }
    }
}
