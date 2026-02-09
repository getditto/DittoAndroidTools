package live.ditto.tools.logviewer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import live.ditto.Ditto
import live.ditto.tools.LogUtils.Companion.getBackgroundColor
import live.ditto.tools.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogFileScreen(
    ditto: Ditto,
    logFileScreenViewModel: LogFileScreenViewModel = viewModel(factory = LogFileScreenViewModelFactory(ditto, context = LocalContext.current))
) {

    val lines by logFileScreenViewModel.filteredLines.collectAsState()
    val searchQuery by logFileScreenViewModel.query.collectAsState()
    val reverse by logFileScreenViewModel.reverse.collectAsState()
    val tail by logFileScreenViewModel.tail.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(lines, tail, reverse) {
        if (tail && lines.isNotEmpty()){
            val targetIndex = if (reverse) 0 else lines.lastIndex
            listState.scrollToItem(targetIndex)
        }
    }

    LaunchedEffect(listState.isScrollInProgress) {
        if(listState.isScrollInProgress && tail){
            logFileScreenViewModel.toggleTail()
        }
    }

    val innerPaddingValues = PaddingValues(start = 5.dp, end = 5.dp)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.log_details_label)) },
                actions = {
                    TailIndicator(tail)

                    LogDropdownMenu(
                        setExpandedNested = { value -> logFileScreenViewModel.setExpandedInnerMenu(value) },
                        tailEnabled = tail,
                        onToggleTail = logFileScreenViewModel::toggleTail,
                        onToggleReverse = logFileScreenViewModel::toggleReverse,
                    )

                    NestedMenu(
                        getExpanded = { logFileScreenViewModel.isExpandedInnerMenu},
                        setExpanded = { value -> logFileScreenViewModel.setExpandedInnerMenu(value) },
                        setMenuFilter = { value -> logFileScreenViewModel.setMenuFilter(value) },
                        onSearchQueryChange = { query -> logFileScreenViewModel.onQueryChange(query)}
                    )
                }
            )
        }
    ) { paddingValues ->
        if (lines.isEmpty()  && searchQuery.isBlank()){
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No log found.",
                    textAlign = TextAlign.Center
                )
            }
        }else{
            Column(
                modifier = Modifier
                    .padding(paddingValues = paddingValues)
                    .padding(innerPaddingValues)

            ) {
                //Search
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        logFileScreenViewModel.setMenuFilter(false)
                        logFileScreenViewModel.onQueryChange(it)
                    },
                    label = { Text(stringResource(R.string.log_search)) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()){
                            IconButton(onClick = {
                                logFileScreenViewModel.onQueryChange("")
                                logFileScreenViewModel.setMenuFilter(false)
                            }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Clear"
                                )
                            }
                        }
                    }
                )

                if (lines.isEmpty()){
                    Column (
                        modifier = Modifier.fillMaxSize(), // Make the Box fill the whole screen
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(imageVector = Icons.Default.Warning,
                            contentDescription = "Warning",
                            tint = colorResource(R.color.log_warn),
                            modifier = Modifier.size(100.dp).padding(bottom = 6.dp)
                        )
                        Text(
                            text = "No Result",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }else{
                    //Log Lines
                    Box(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            state = listState,
                            reverseLayout = reverse,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(lines) { logLine ->
                                LogCard(logLine = logLine)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LogCard(logLine: Map<String, Any>) {

    var expanded by remember { mutableStateOf(false) }

    val aString = buildAnnotatedString {
        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)){
            append("${logLine["level"]}")
        }
        append(" ${logLine["timestamp"]}\n")

        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)){
            append("Target: ")
        }
        append("${logLine["target"]}\n")

        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)){
            append("Message: ")
        }
        append("${logLine["message"]}")

        logLine["error"]?.let { it as String
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)){
                append("\nError: ")
            }
            append(it)
        }
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = colorResource( getBackgroundColor(logLine["level"] as String))
        ),
        modifier = Modifier
            .padding(all = 4.dp)
            .fillMaxWidth()
            .clickable { expanded = !expanded }
    ) {
        Text(text = aString, modifier = Modifier.padding(all = 8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(if (expanded) "Less" else "More")
            Icon(
                if (expanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight, contentDescription = "Expand"
            )

        }
        // Only show detailed content when expanded, with animation
        AnimatedVisibility(visible = expanded) {
            Text(getExpandedText(logLine), modifier = Modifier.padding(start = 8.dp, end = 8.dp))
        }
    }
}

@Composable
fun LogDropdownMenu(
    setExpandedNested: (Boolean) -> Unit,
    tailEnabled: Boolean,
    onToggleTail: () -> Unit,
    onToggleReverse: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .padding(16.dp)
    ) {
        IconButton(onClick = { expanded = !expanded }) {
            Icon(Icons.Default.MoreVert, contentDescription = "More options")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.log_tail)) },
                onClick = {
                    onToggleTail()
                    expanded = false
                },
                trailingIcon = {
                    if(tailEnabled){
                        Icon(imageVector = Icons.Default.Check, contentDescription = "Tail Logs" )
                    }
                }
            )

            DropdownMenuItem(
                text = { Text(stringResource(R.string.log_reverse)) },
                onClick = {
                    onToggleReverse()
                    expanded = false
                },
                trailingIcon = {
                    Icon(Icons.Default.SwapVert, contentDescription = "Reverse Logs" )
                }
            )

            DropdownMenuItem(
                text = { Text(stringResource(R.string.log_filter)) },
                onClick = {
                    expanded = false
                    setExpandedNested(true)
                },
                trailingIcon = {
                    Icon(Icons.Default.FilterAlt, contentDescription = "Filter" )
                }
            )
        }
    }
}

private fun getExpandedText(logLine: Map<String, Any>): AnnotatedString{

    val threadId = buildAnnotatedString {
        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)){
            append("ThreadId: ")
        }
        append("${logLine["threadId"]}\n")
    }

    val threadName = logLine["threadName"]?.let {
        buildAnnotatedString {
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)){
                append("ThreadName: ")
            }
            append("$it\n")
        }
    }

    val other = buildAnnotatedString {
        logLine.filter { map -> map.key != "message"
                && map.key != "level"
                && map.key != "error"
                && map.key != "timestamp"
                && map.key != "target"
                && map.key != "threadId"
                && map.key != "threadName"
                && map.key != "span"
                && map.key != "spans"
        }.forEach { (key, value) ->

            if (value is Map<*,*>){
                value.forEach { (key, value) ->
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)){
                        append(key as String)
                    }
                    append(": $value\n")
                }
            }else{
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)){
                    append(key)
                }
                append(": $value\n")
            }
        }
    }

    //NOTE: Seems that what is inside of span is also contained in spans??
    //Need to confirm.
    val span = buildAnnotatedString {
        val map = logLine["span"]
        if (map != null && map is Map<*,*>){
            map.forEach { (key, value) ->
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)){
                    append(key as String)
                }
                append(": $value\n")
            }
        }
    }

    val spans = buildAnnotatedString {
        val spansList = logLine["spans"]

        spansList?.let {
            if (it is List<*>){
                it.forEach { span ->
                    (span as Map<*, *>).forEach { (key, value) ->
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)){
                            append(key as String)
                        }
                        append(": $value\n")
                    }
                }
            }
        }
    }

    return threadName?.let { it + threadId + if (spans.isEmpty()) span else spans + other }
        ?: run { threadId + if (spans.isEmpty()) span else spans + other}
}

@Composable
private fun TailIndicator(isTailing: Boolean){
    if (!isTailing) return

    val alpha by rememberInfiniteTransition().animateFloat(
        initialValue = 0.4F,
        targetValue = 1F,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        )
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(end = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    color = colorResource(R.color.log_tail).copy(alpha = alpha),
                    shape = CircleShape
                )
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(stringResource(R.string.log_tail_live), style = MaterialTheme.typography.labelSmall, color = Color(0xFF4CAF50))
    }
}

@Composable
private fun NestedMenu(
    getExpanded: () -> Boolean,
    setExpanded: (Boolean) -> Unit,
    setMenuFilter: (Boolean) -> Unit,
    onSearchQueryChange: (String) -> Unit,
){

    val logLevels = listOf("FATAL","ERROR", "WARN", "INFO", "DEBUG")

    DropdownMenu(
        expanded = getExpanded(),
        onDismissRequest = { setExpanded(false) }
    ) {
        logLevels.forEach { logLevel ->
            DropdownMenuItem(
                text = { Text(logLevel) },
                onClick = {
                    setMenuFilter(true)
                    setExpanded(false)
                    onSearchQueryChange(logLevel)
                }
            )
        }
    }
}