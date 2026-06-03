package live.ditto.tools.diskusage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun DiskUsageInspectorView(
    storageViewModel: DiskStorageViewModel,
    inspectionViewModel: CollectionInspectionViewModel
) {
    val breakdown by storageViewModel.breakdown.collectAsState()
    val hasFirstSnapshot by storageViewModel.hasReceivedFirstSnapshot.collectAsState()
    val threshold by storageViewModel.healthThresholdBytes.collectAsState()

    val discoveredCollections by inspectionViewModel.discoveredCollections.collectAsState()
    val scanStates by inspectionViewModel.collectionScanStates.collectAsState()
    val selectedColl by inspectionViewModel.selectedCollection.collectAsState()
    val isScanning by inspectionViewModel.isScanningCollections.collectAsState()
    val hasScanned by inspectionViewModel.hasScannedCollections.collectAsState()
    val scanError by inspectionViewModel.scanError.collectAsState()
    val isSampling by inspectionViewModel.isSamplingCollection.collectAsState()
    val sampleError by inspectionViewModel.sampleError.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // OVERVIEW
        item { SectionHeader("Overview") }
        item {
            OverviewSection(
                totalBytes = breakdown.totalOnDiskBytes,
                hasFirstSnapshot = hasFirstSnapshot,
                status = storageViewModel.healthStatus,
                fillRatio = storageViewModel.fillRatio,
                collectionCount = inspectionViewModel.collectionCount,
                totalDocumentCount = inspectionViewModel.totalDocumentCount
            )
        }

        // HEALTH GROUP
        item { GroupHeader("Health", "Is usage okay & trending?", Icons.Default.Favorite, Color(0xFF4CAF50)) }
        item {
            HealthStatusSection(
                currentBytes = breakdown.totalOnDiskBytes,
                thresholdBytes = threshold,
                status = storageViewModel.healthStatus,
                hasFirstSnapshot = hasFirstSnapshot,
                onSetThreshold = { storageViewModel.setThreshold(it) }
            )
        }
        item {
            DiskGrowthSection(
                historyTotals = storageViewModel.historyTotalBytes,
                status = storageViewModel.healthStatus,
                growthRate = storageViewModel.growthRatePerSecond
            )
        }
        item {
            GrowthPredictionSection(
                estimatedSeconds = storageViewModel.estimatedSecondsToThreshold,
                remainingBytes = threshold - breakdown.totalOnDiskBytes
            )
        }

        // COLLECTIONS GROUP
        item { GroupHeader("Collections", "Per-collection detail", Icons.Default.Inbox, Color(0xFFFF9800)) }
        item {
            CollectionScanSection(
                discoveredCollections = discoveredCollections,
                scanStates = scanStates,
                selectedCollection = selectedColl,
                isScanning = isScanning,
                hasScanned = hasScanned,
                scanError = scanError,
                onScanTapped = { inspectionViewModel.scanCollections() },
                onSelectCollection = { inspectionViewModel.selectCollection(it) }
            )
        }
        item {
            CollectionRankingSection(
                discoveredCollections = discoveredCollections,
                scanStates = scanStates,
                hasScanned = hasScanned,
                scanError = scanError
            )
        }
        item {
            DocSizeDistributionSection(
                selectedCollection = selectedColl,
                totalDocCount = inspectionViewModel.totalDocsForSelected,
                sample = inspectionViewModel.sampleForSelected,
                sampleLimit = CollectionInspectionViewModel.SAMPLE_LIMIT,
                isSampling = isSampling,
                hasScannedCollections = hasScanned,
                scanFailed = scanError != null,
                sampleError = sampleError,
                onSampleTapped = { inspectionViewModel.sampleSelectedCollection() }
            )
        }

        // REFERENCE GROUP
        item { GroupHeader("Reference", "Definitions & help", Icons.Default.Info, MaterialTheme.colorScheme.onSurfaceVariant) }
        item { GlossarySection() }
        item { AboutSection() }
    }
}

// Section composable

@Composable
fun SectionHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

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
fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall)
        Text(value, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// Overview

@Composable
fun OverviewSection(
    totalBytes: Int,
    hasFirstSnapshot: Boolean,
    status: HealthStatus,
    fillRatio: Double,
    collectionCount: Int?,
    totalDocumentCount: Int?
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Counter row + health badge
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Total on disk", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (hasFirstSnapshot) {
                        Text(Format.bytes(totalBytes), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    } else {
                        Text("—", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                if (hasFirstSnapshot) {
                    Surface(shape = RoundedCornerShape(16.dp), color = status.tint.copy(alpha = 0.12f)) {
                        Text(status.label, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp), fontWeight = FontWeight.SemiBold, color = status.tint, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            // Stat strip
            Row(Modifier.fillMaxWidth()) {
                StatColumn("Collections", collectionCount?.let { Format.count(it) } ?: "—", Modifier.weight(1f))
                StatColumn("Documents", totalDocumentCount?.let { Format.count(it) } ?: "—", Modifier.weight(1f))
                StatColumn("Used", if (hasFirstSnapshot) "%.0f%%".format(fillRatio * 100) else "—", Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
            Text(
                if (hasFirstSnapshot) "Reported by the Ditto SDK's disk usage publisher."
                else "Waiting for the first disk usage report…",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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

// Health Status

@Composable
fun HealthStatusSection(
    currentBytes: Int,
    thresholdBytes: Int,
    status: HealthStatus,
    hasFirstSnapshot: Boolean,
    onSetThreshold: (Int) -> Unit
) {
    SectionCard("Health Status") {
        if (hasFirstSnapshot) {
            // Health indicator
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    when (status) {
                        HealthStatus.HEALTHY -> Icons.Default.CheckCircle
                        HealthStatus.WARNING -> Icons.Default.Warning
                        HealthStatus.UNHEALTHY -> Icons.Default.Error
                    },
                    status.label,
                    tint = status.tint
                )
                Spacer(Modifier.width(8.dp))
                Text(status.label, fontWeight = FontWeight.SemiBold, color = status.tint)
            }
            Spacer(Modifier.height(12.dp))
            HealthGaugeView(currentBytes, thresholdBytes)
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.HourglassEmpty, "Waiting", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(8.dp))
                Text("Waiting for the first disk usage report…", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(12.dp))
        // Threshold row — always shown
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Threshold", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.weight(1f))
            Text(Format.bytes(thresholdBytes), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = { onSetThreshold(thresholdBytes - THRESHOLD_STEP_BYTES) }) {
                Icon(Icons.Default.Remove, "Decrease")
            }
            IconButton(onClick = { onSetThreshold(thresholdBytes + THRESHOLD_STEP_BYTES) }) {
                Icon(Icons.Default.Add, "Increase")
            }
        }
    }
}

// Disk Growth

@Composable
fun DiskGrowthSection(
    historyTotals: List<Int>,
    status: HealthStatus,
    growthRate: Double?
) {
    SectionCard("Disk Growth") {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Total over time", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            historyTotals.lastOrNull()?.let {
                Text(Format.bytes(it), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(4.dp))
        GrowthRateView(growthRate)
        Spacer(Modifier.height(4.dp))
        SparklineView(historyTotals, status.tint)
    }
}

// Growth Prediction

@Composable
fun GrowthPredictionSection(estimatedSeconds: Double?, remainingBytes: Int) {
    SectionCard("Growth Prediction") {
        TimeToThresholdView(estimatedSeconds, remainingBytes)
    }
}

// Collection Scan

@Composable
fun CollectionScanSection(
    discoveredCollections: List<String>,
    scanStates: Map<String, CollectionScanState>,
    selectedCollection: String?,
    isScanning: Boolean,
    hasScanned: Boolean,
    scanError: String?,
    onScanTapped: () -> Unit,
    onSelectCollection: (String) -> Unit
) {
    SectionCard("Collection Scan") {
        // Scan button
        Button(onClick = onScanTapped, enabled = !isScanning) {
            if (isScanning) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(8.dp))
                Text("Scanning…")
            } else {
                Text(if (hasScanned) "Re-scan collections" else "Scan collections")
            }
        }
        Spacer(Modifier.height(8.dp))

        // Content
        if (scanError != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, "Error", tint = Color(0xFFFF9800), modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("Couldn't list collections: $scanError", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else if (!hasScanned) {
            Text(
                "Tap \"Scan collections\" to list local collections and their document counts.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else if (discoveredCollections.isEmpty()) {
            Text("No collections found in the local store.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            // Collection picker
            var expanded by remember { mutableStateOf(false) }
            Box {
                Button(onClick = { expanded = true }) {
                    Text(selectedCollection ?: "Select Collection")
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.Default.ArrowDropDown, "Select")
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    discoveredCollections.forEach { name ->
                        DropdownMenuItem(
                            text = { Text(name) },
                            onClick = {
                                onSelectCollection(name)
                                expanded = false
                            }
                        )
                    }
                }
            }
            // Doc count for selected
            if (selectedCollection != null) {
                val scanState = scanStates[selectedCollection]
                if (scanState != null) {
                    Spacer(Modifier.height(4.dp))
                    val countText = when (scanState) {
                        is CollectionScanState.Pending -> "Counting…"
                        is CollectionScanState.Counted -> "${Format.count(scanState.count)} docs"
                        is CollectionScanState.Failed -> "Failed"
                    }
                    InfoRow("Documents", countText)
                }
            }
        }
    }
}

// Collection Ranking

@Composable
fun CollectionRankingSection(
    discoveredCollections: List<String>,
    scanStates: Map<String, CollectionScanState>,
    hasScanned: Boolean,
    scanError: String?
) {
    SectionCard("Collection Ranking") {
        when {
            scanError != null -> {
                Text("Run a successful scan to see the ranking.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            !hasScanned -> {
                Text("Tap \"Scan collections\" above to see how collections rank by document count.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            discoveredCollections.isEmpty() -> {
                Text("No collections to rank.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            else -> {
                val ranked = discoveredCollections
                    .mapNotNull { name ->
                        val state = scanStates[name]
                        if (state is CollectionScanState.Counted) name to state.count else null
                    }
                    .sortedByDescending { it.second }

                if (ranked.isEmpty()) {
                    val anyPending = scanStates.values.any { it is CollectionScanState.Pending }
                    Text(
                        if (anyPending) "Counts are still in flight…" else "No counts available — every collection failed.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    HorizontalBarChartView(
                        items = ranked,
                        barColor = MaterialTheme.colorScheme.primary,
                        valueFormatter = { Format.count(it) }
                    )
                }
            }
        }
    }
}

// Document Size Distribution

@Composable
fun DocSizeDistributionSection(
    selectedCollection: String?,
    totalDocCount: Int?,
    sample: CollectionSample?,
    sampleLimit: Int,
    isSampling: Boolean,
    hasScannedCollections: Boolean,
    scanFailed: Boolean,
    sampleError: String?,
    onSampleTapped: () -> Unit
) {
    SectionCard("Document Size Distribution") {
        when {
            scanFailed -> Text("Scan failed — sample is unavailable until the scan succeeds.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            !hasScannedCollections -> Text("Scan collections first to sample one.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            selectedCollection == null -> Text("No collection available to sample.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            else -> {
                // Sample button
                Button(onClick = onSampleTapped, enabled = !isSampling) {
                    if (isSampling) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("Sampling…")
                    } else {
                        Text(if (sample != null) "Re-sample documents" else "Sample documents")
                    }
                }
                Spacer(Modifier.height(8.dp))

                if (sampleError != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, "Error", tint = Color(0xFFFF9800), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Couldn't sample collection: $sampleError", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else if (sample != null) {
                    // Summary
                    val summaryText = buildSummaryText(sample, totalDocCount)
                    InfoRow("Sampled", summaryText)
                    Spacer(Modifier.height(8.dp))
                    // Histogram
                    HorizontalBarChartView(
                        items = sample.buckets.map { it.label to it.count },
                        barColor = MaterialTheme.colorScheme.primary,
                        valueFormatter = { Format.count(it) }
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Approximation based on JSON serialization. Actual on-disk size includes CRDT metadata and indexes.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        "Tap Sample to group up to ${Format.count(sampleLimit)} documents by JSON byte size.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun buildSummaryText(sample: CollectionSample, totalDocCount: Int?): String {
    val sampled = Format.count(sample.sampledCount)
    if (totalDocCount != null) {
        if (sample.sampledCount == totalDocCount) return "All ${Format.count(totalDocCount)} docs"
        if (sample.sampledCount < totalDocCount && sample.reachedLimit) return "$sampled of ${Format.count(totalDocCount)} docs"
        return "$sampled docs"
    }
    return if (sample.reachedLimit) "$sampled docs (collection may be larger)" else "$sampled docs"
}

// Glossary

@Composable
fun GlossarySection() {
    SectionCard("Glossary") {
        StorageCategory.entries.forEach { category ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.Top
            ) {
                val (icon, tint) = when (category) {
                    StorageCategory.STORE -> Icons.Default.Storage to Color(0xFF2196F3)
                    StorageCategory.ATTACHMENTS -> Icons.Default.AttachFile to Color(0xFFE91E63)
                    StorageCategory.LOGS -> Icons.Default.Description to Color(0xFF4CAF50)
                    StorageCategory.REPLICATION -> Icons.Default.Sync to Color(0xFF9C27B0)
                }
                Icon(icon, category.displayName, tint = tint, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(category.displayName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Text(category.glossary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            "Reference only. The Inspector doesn't group bytes by category yet — that needs a typed signal from the SDK.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// About

@Composable
fun AboutSection() {
    SectionCard("About This Tool") {
        AboutBullet(Icons.Default.Lock, "Public APIs only", "All data comes from the disk usage publisher and one-shot DQL queries. No file-system access, no SDK internals.")
        AboutBullet(Icons.Default.VisibilityOff, "No live observers", "Counts and samples run only when you tap. Nothing watches your collections in the background.")
        AboutBullet(Icons.Default.Memory, "Bounded memory", "Document sampling is capped at ${Format.count(CollectionInspectionViewModel.SAMPLE_LIMIT)} items and released as soon as each item is measured.")
    }
}

@Composable
private fun AboutBullet(icon: ImageVector, title: String, detail: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(icon, title, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Column {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}