package live.ditto.tools.databrowser

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun Documents(collectionName: String) {
    val viewModel: DocumentsViewModel = viewModel(
        key = "documents:$collectionName",
        factory = DocumentsViewModel.Factory(collectionName),
    )

    val totalCount by viewModel.totalCount.collectAsStateWithLifecycle()
    val pageState by viewModel.pageState.collectAsStateWithLifecycle()
    val selectedDoc by viewModel.selectedDoc.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Collection: $collectionName",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(12.dp))

        SearchBar(onSearch = viewModel::filterDocs)
        Spacer(modifier = Modifier.height(12.dp))

        pageState.error?.let { error ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(12.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        PageHeader(
            page = pageState.page,
            totalCount = totalCount,
            isLoading = pageState.isLoading,
            onRefresh = viewModel::refresh,
            onPrev = viewModel::previousPage,
            onNext = viewModel::nextPage,
        )
        Spacer(modifier = Modifier.height(12.dp))

        val page = pageState.page
        when {
            pageState.isLoading && page == null -> LoadingPlaceholder()
            page == null -> Unit
            page.docs.isEmpty() -> EmptyPlaceholder()
            else -> DocList(
                page = page,
                selectedDoc = selectedDoc,
                onSelect = viewModel::selectDoc,
            )
        }
    }
}

@Composable
private fun PageHeader(
    page: DocumentsViewModel.Page?,
    totalCount: Int?,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit,
) {
    val countText = when {
        totalCount == null -> "Loading…"
        page == null -> "of $totalCount"
        page.docs.isEmpty() -> "0 of $totalCount"
        else -> "Showing ${page.offset + 1}–${page.offset + page.docs.size} of $totalCount"
    }
    val stale = totalCount != null && page != null && totalCount != page.snapshotAtCount

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = countText, fontWeight = FontWeight.SemiBold)
        if (stale) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "(updated — refresh)",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 12.sp,
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(onClick = onRefresh, enabled = !isLoading) {
            Icon(Icons.Filled.Refresh, contentDescription = "Refresh page")
        }
    }
    Spacer(modifier = Modifier.height(4.dp))
    Row {
        OutlinedButton(
            onClick = onPrev,
            enabled = page != null && page.offset > 0 && !isLoading,
        ) {
            Text("← Prev")
        }
        Spacer(modifier = Modifier.width(8.dp))
        val canNext = page != null && totalCount != null &&
            page.offset + page.docs.size < totalCount && !isLoading
        OutlinedButton(onClick = onNext, enabled = canNext) {
            Text("Next →")
        }
    }
}

@Composable
private fun LoadingPlaceholder() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp))
        Text(text = "Loading…")
    }
}

@Composable
private fun EmptyPlaceholder() {
    Text(text = "No documents on this page.", color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun DocList(
    page: DocumentsViewModel.Page,
    selectedDoc: Document?,
    onSelect: (Document) -> Unit,
) {
    LazyColumn {
        items(page.docs, key = { it.id }) { doc ->
            DocRow(
                doc = doc,
                selected = doc.id == selectedDoc?.id,
                properties = page.docProperties,
                onSelect = { onSelect(doc) },
            )
            Divider()
        }
    }
}

@Composable
private fun DocRow(
    doc: Document,
    selected: Boolean,
    properties: List<String>,
    onSelect: () -> Unit,
) {
    val background =
        if (selected) MaterialTheme.colorScheme.secondaryContainer
        else MaterialTheme.colorScheme.surface
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(background)
            .clickable(onClick = onSelect)
            .padding(vertical = 8.dp, horizontal = 4.dp)
    ) {
        Text(
            text = doc.id,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 14.sp,
        )
        if (selected) {
            Spacer(modifier = Modifier.height(6.dp))
            for (key in properties) {
                Row(modifier = Modifier.padding(vertical = 2.dp)) {
                    Text(
                        text = "$key:",
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(text = formatDisplayValue(doc.properties[key]))
                }
            }
        }
    }
}

@Composable
private fun SearchBar(onSearch: (String) -> Unit) {
    var text by remember { mutableStateOf(TextFieldValue("")) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onSearch(text.text) }) {
                Icon(Icons.Filled.Search, contentDescription = "Search")
            }
            Spacer(modifier = Modifier.width(4.dp))
            BasicTextField(
                value = text,
                onValueChange = { text = it },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
