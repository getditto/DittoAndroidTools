package live.ditto.tools.diskusage

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.material3.Divider

@Composable
fun DiskUsageInspectorView(
    viewModel: DiskUsageInspectorViewModel
) {
    val breakdown by viewModel.breakdown.collectAsState()
    val fileListing by viewModel.fileListing.collectAsState()
    val diskHistory by viewModel.diskUsageHistory.collectAsState()
    val attachHistory by viewModel.attachmentBytesHistory.collectAsState()
    val growthRate by viewModel.growthRatePerSecond.collectAsState()
    val gcEvents by viewModel.gcEventsDetected.collectAsState()
    val lastGCDate by viewModel.lastGCEventDate.collectAsState()
    val gcBytes by viewModel.gcBytesReclaimed.collectAsState()
    val estimatedSeconds by viewModel.estimatedSecondsToThreshold.collectAsState()
    val warnings by viewModel.parseWarnings.collectAsState()
    val threshold by viewModel.unhealthySizeInBytes.collectAsState()
    val collections by viewModel.collections.collectAsState()
    val selectedColl by viewModel.selectedCollection.collectAsState()
    val collCounts by viewModel.collectionCounts.collectAsState()
    val failedCounts by viewModel.collectionsWithFailedCount.collectAsState()
    val samples by viewModel.collectionSamples.collectAsState()
    val isScanning by viewModel.isScanningCollections.collectAsState()
    val isSampling by viewModel.isSamplingCollection.collectAsState()
    val lastScanDate by viewModel.lastCollectionScanDate.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // ---- OVERVIEW ----
        item { SectionHeader("Overview") }
        item { OverviewCard(breakdown, threshold) }

        // ---- STORAGE GROUP ----
        item { GroupHeader("Storage", "Where is space going?", Icons.Default.Storage, StoreColor) }
        item { StorageBreakdownCard(breakdown) }
        item {
            val slices = listOf(
                DonutSlice("Store", breakdown.storeBytes, StoreColor),
                DonutSlice("Attachments", breakdown.attachmentBytes, AttachmentColor),
                DonutSlice("Logs", breakdown.logsBytes, LogsColor),
                DonutSlice("Replication", breakdown.replicationBytes, ReplicationColor),
            )
            SectionCard("Storage Distribution") {
                DonutChartView(slices, Modifier.height(260.dp).fillMaxWidth(), lineWidth = 32f)
                Spacer(Modifier.height(12.dp))
                DonutLegendView(slices)
            }
        }
        item {
            SectionCard("Content vs Infrastructure") {
                StackedComparisonView(
                    leftLabel = "Content",
                    leftBytes = breakdown.storeBytes + breakdown.attachmentBytes,
                    leftColor = StoreColor,
                    rightLabel = "Infrastructure",
                    rightBytes = breakdown.logsBytes + breakdown.replicationBytes,
                    rightColor = ReplicationColor
                )
            }
        }
        item {
            SectionCard("Attachments") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Attachment Size", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(StorageBreakdown.formatBytes(breakdown.attachmentBytes), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AttachmentColor)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("% of Total", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        val pct = if (breakdown.totalOnDiskBytes > 0) "%.1f%%".format(breakdown.attachmentBytes.toDouble() / breakdown.totalOnDiskBytes * 100) else "0.0%"
                        Text(pct, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        item {
            SectionCard("Garbage Collection") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("GC Events Detected", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$gcEvents", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Space Reclaimed (est.)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(StorageBreakdown.formatBytes(gcBytes), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = LogsColor)
                    }
                }
                if (attachHistory.size >= 2) {
                    Spacer(Modifier.height(8.dp))
                    Text("Attachment Size Over Time", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    SparklineView(attachHistory, LogsColor)
                }
                if (lastGCDate != null) {
                    Spacer(Modifier.height(4.dp))
                    InfoRow("Last GC Event", viewModel.dateFormatter.format(lastGCDate!!))
                }
            }
        }

        // ---- HEALTH GROUP ----
        item { GroupHeader("Health", "Is usage okay & trending?", Icons.Default.Favorite, LogsColor) }
        item {
            SectionCard("Health Status") {
                HealthGaugeView(breakdown.totalOnDiskBytes, threshold)
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Threshold", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.weight(1f))
                    Text(StorageBreakdown.formatBytes(threshold), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = { viewModel.adjustThreshold(-50_000_000) }) {
                        Icon(Icons.Default.Remove, "Decrease")
                    }
                    IconButton(onClick = { viewModel.adjustThreshold(50_000_000) }) {
                        Icon(Icons.Default.Add, "Increase")
                    }
                }
            }
        }
        item {
            SectionCard("Disk Growth") {
                GrowthRateRow(growthRate)
                if (diskHistory.size >= 2) {
                    Spacer(Modifier.height(8.dp))
                    Text("Total Disk Size Over Time", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    SparklineView(diskHistory, StoreColor)
                }
            }
        }
        item {
            SectionCard("Growth Prediction") {
                GrowthPredictionContent(estimatedSeconds, growthRate, threshold, breakdown.totalOnDiskBytes)
            }
        }

        // ---- COLLECTIONS GROUP ----
        item { GroupHeader("Collections", "Opt-in per-collection scan", Icons.Default.Inbox, ReplicationColor) }
        item {
            SectionCard("Collection Scan") {
                Button(
                    onClick = { viewModel.scanCollections() },
                    enabled = !isScanning
                ) {
                    if (isScanning) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("Scanning…")
                    } else {
                        Text("Scan Collections")
                    }
                }
                if (collections.isEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Tap to discover collections and count documents. Uses one-shot queries — no subscriptions.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Spacer(Modifier.height(8.dp))
                    // Collection picker
                    var expanded by remember { mutableStateOf(false) }
                    Box {
                        Button(onClick = { expanded = true }) {
                            Text(selectedColl.ifEmpty { "Select Collection" })
                            Spacer(Modifier.width(4.dp))
                            Icon(Icons.Default.ArrowDropDown, "Select")
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            collections.forEach { name ->
                                DropdownMenuItem(
                                    text = { Text(name) },
                                    onClick = {
                                        viewModel.selectCollection(name)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.sampleSelectedCollection() },
                        enabled = !isSampling && !isScanning && selectedColl.isNotEmpty()
                    ) {
                        if (isSampling) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                            Text("Sampling…")
                        } else {
                            Text("Sample ${selectedColl.take(24)} (≤ ${DiskUsageInspectorViewModel.SAMPLE_LIMIT} docs)")
                        }
                    }
                    if (lastScanDate != null) {
                        Spacer(Modifier.height(4.dp))
                        InfoRow("Last Scan", viewModel.dateFormatter.format(lastScanDate!!))
                    }
                }
            }
        }
        if (collCounts.isNotEmpty()) {
            item {
                SectionCard("Collection Size Ranking (by document count)") {
                    val ranked = collections
                        .mapNotNull { name -> collCounts[name]?.let { name to it } }
                        .sortedByDescending { it.second }
                    HorizontalBarChartView(
                        items = ranked,
                        barColor = ReplicationColor,
                        valueFormatter = { if (it == 1) "1 doc" else "$it docs" }
                    )
                    if (failedCounts.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Count unavailable for ${failedCounts.size} collection${if (failedCounts.size == 1) "" else "s"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = ReplicationColor
                        )
                    }
                }
            }
        }
        if (selectedColl.isNotEmpty()) {
            item {
                SectionCard("Document Sizes: $selectedColl") {
                    val sample = samples[selectedColl]
                    if (sample != null) {
                        HistogramView(sample.buckets, ReplicationColor)
                        Spacer(Modifier.height(8.dp))
                        InfoRow("Sampled", "${sample.sampledCount} docs${if (sample.wasTruncated) " (truncated)" else " (all)"}")
                        InfoRow("Sample Payload", StorageBreakdown.formatBytes(sample.sampleBytes))
                    } else {
                        Text("Tap \"Sample ...\" above to build a size distribution.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // ---- REFERENCE GROUP ----
        item { GroupHeader("Reference", "Raw data & help", Icons.Default.Info, MaterialTheme.colorScheme.onSurfaceVariant) }

        // Parse warnings
        if (warnings.isNotEmpty()) {
            item {
                SectionCard("Parse Warnings") {
                    warnings.forEach { warning ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                            Icon(Icons.Default.Warning, "Warning", tint = ReplicationColor, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(warning, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

        // File listing
        item {
            SectionCard("File Listing") {
                fileListing?.let { listing ->
                    listing.children.forEach { child ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(child.relativePath, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1f))
                            Text(child.size, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total", fontWeight = FontWeight.Bold)
                        Text(listing.totalSize, fontWeight = FontWeight.Bold)
                    }
                } ?: Text(NO_DATA)
            }
        }
    }
}

// Helper composable

@Composable
fun GroupHeader(title: String, subtitle: String, icon: ImageVector, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, title, tint = Color.White, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title.uppercase(), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 4.dp))
}

@Composable
fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
fun OverviewCard(breakdown: StorageBreakdown, threshold: Int) {
    val ratio = if (threshold > 0) breakdown.totalOnDiskBytes.toDouble() / threshold else 0.0
    val (badgeText, badgeColor) = when {
        ratio >= 1.0 -> "Critical" to Color.Red
        ratio >= 0.75 -> "Warning" to Color(0xFFFF9800)
        else -> "Healthy" to Color(0xFF4CAF50)
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Total on Disk", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(StorageBreakdown.formatBytes(breakdown.totalOnDiskBytes), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                }
                Surface(shape = RoundedCornerShape(16.dp), color = badgeColor.copy(alpha = 0.12f)) {
                    Text(badgeText, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp), fontWeight = FontWeight.SemiBold, color = badgeColor)
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth()) {
                StatColumn("Store", StorageBreakdown.formatBytes(breakdown.storeBytes), Modifier.weight(1f))
                StatColumn("Attachments", StorageBreakdown.formatBytes(breakdown.attachmentBytes), Modifier.weight(1f))
                StatColumn("Logs", StorageBreakdown.formatBytes(breakdown.logsBytes), Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun StatColumn(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall)
        Text(value, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun GrowthRateRow(bytesPerSecond: Double?) {
    val (icon, color, text) = when {
        bytesPerSecond == null -> Triple(Icons.Default.HourglassEmpty, MaterialTheme.colorScheme.onSurfaceVariant, "Calculating...")
        bytesPerSecond > 1_000_000 -> Triple(Icons.Default.TrendingUp, Color.Red, "+${StorageBreakdown.formatBytes((bytesPerSecond * 60).toInt())} / min")
        bytesPerSecond > 100 -> Triple(Icons.Default.TrendingUp, Color(0xFFFF9800), "+${StorageBreakdown.formatBytes((bytesPerSecond * 60).toInt())} / min")
        bytesPerSecond < -100 -> Triple(Icons.Default.TrendingDown, Color(0xFF4CAF50), "-${StorageBreakdown.formatBytes((bytesPerSecond.times(-60)).toInt())} / min")
        else -> Triple(Icons.Default.TrendingFlat, MaterialTheme.colorScheme.onSurfaceVariant, "Stable")
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, "Growth", tint = color)
        Spacer(Modifier.width(8.dp))
        Column {
            Text("Growth Rate", style = MaterialTheme.typography.bodyMedium)
            Text(text, fontWeight = FontWeight.Bold, color = color, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun GrowthPredictionContent(estimatedSeconds: Double?, growthRate: Double?, threshold: Int, currentBytes: Int) {
    when {
        estimatedSeconds != null && estimatedSeconds <= 0 -> {
            Text("Threshold Exceeded", style = MaterialTheme.typography.titleMedium, color = Color.Red, fontWeight = FontWeight.Bold)
            Text("Disk usage has already surpassed the health threshold.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        estimatedSeconds != null && estimatedSeconds > 0 -> {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Time to Threshold", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(formatTimeInterval(estimatedSeconds), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Remaining", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(StorageBreakdown.formatBytes((threshold - currentBytes).coerceAtLeast(0)), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
        growthRate == null -> Text("Collecting data to estimate growth prediction…", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        else -> {
            Text("Stable or Shrinking", style = MaterialTheme.typography.titleMedium, color = LogsColor, fontWeight = FontWeight.Bold)
            Text("Disk usage is not growing — no threshold concern.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun StorageBreakdownCard(breakdown: StorageBreakdown) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Storage Breakdown", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            BreakdownRow("Store", breakdown.storeBytes, Icons.Default.Storage)
            BreakdownRow("Attachments", breakdown.attachmentBytes, Icons.Default.AttachFile)
            BreakdownRow("Logs", breakdown.logsBytes, Icons.Default.Description)
            BreakdownRow("Replication", breakdown.replicationBytes, Icons.Default.Sync)
        }
    }
}

@Composable
private fun BreakdownRow(label: String, bytes: Int, icon: ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, label, tint = StoreColor, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.weight(1f))
        Text(
            StorageBreakdown.formatBytes(bytes),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

fun formatTimeInterval(seconds: Double): String {
    if (seconds < 60) return "< 1 min"
    val minutes = seconds / 60
    if (minutes < 60) return "%.0f min".format(minutes)
    val hours = minutes / 60
    if (hours < 24) return "%.1f hours".format(hours)
    val days = hours / 24
    if (days < 365) return "%.1f days".format(days)
    return "%.1f years".format(days / 365)
}