package live.ditto.tools.diskusage

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


val StoreColor = Color(0xFF2196F3)       // Blue
val AttachmentColor = Color(0xFFE91E63)  // Pink
val LogsColor = Color(0xFF4CAF50)        // Green
val ReplicationColor = Color(0xFFFF9800) // Orange

data class DonutSlice(
    val label: String,
    val bytes: Int,
    val color: Color
)

@Composable
fun DonutChartView(
    slices: List<DonutSlice>,
    modifier: Modifier = Modifier,
    lineWidth: Float = 44f
) {
    val total = slices.sumOf { it.bytes }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .aspectRatio(1f)
        ) {
            if (total > 0) {
                var startAngle = -90f
                for (slice in slices) {
                    val sweep = (slice.bytes.toFloat() / total) * 360f
                    drawArc(
                        color = slice.color,
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        style = Stroke(width = lineWidth, cap = StrokeCap.Butt)
                    )
                    startAngle += sweep
                }
            } else {
                drawArc(
                    color = Color.Gray.copy(alpha = 0.2f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = lineWidth)
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = StorageBreakdown.formatBytes(total),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Total",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun DonutLegendView(slices: List<DonutSlice>) {
    val total = slices.sumOf { it.bytes }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        slices.forEach { slice ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(slice.color)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(slice.label, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    StorageBreakdown.formatBytes(slice.bytes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (total > 0) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "(${(slice.bytes.toDouble() / total * 100).toInt()}%)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun HealthGaugeView(currentBytes: Int, thresholdBytes: Int) {
    val fraction = if (thresholdBytes > 0) {
        (currentBytes.toDouble() / thresholdBytes).coerceAtMost(1.5)
    } else 0.0
    val displayFraction = fraction.coerceAtMost(1.0)
    val gaugeColor = when {
        fraction >= 1.0 -> Color.Red
        fraction >= 0.75 -> Color(0xFFFF9800)
        else -> Color(0xFF4CAF50)
    }
    val animatedFraction by animateFloatAsState(
        targetValue = displayFraction.toFloat(),
        animationSpec = tween(500),
        label = "gauge"
    )

    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Disk Health", style = MaterialTheme.typography.bodyMedium)
            Text(
                "${(fraction.coerceAtMost(1.5) * 100).toInt()}%",
                fontWeight = FontWeight.Bold,
                color = gaugeColor
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color.Gray.copy(alpha = 0.15f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedFraction)
                    .clip(RoundedCornerShape(6.dp))
                    .background(gaugeColor)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("0", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                "Threshold: ${StorageBreakdown.formatBytes(thresholdBytes)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SparklineView(dataPoints: List<Int>, color: Color, modifier: Modifier = Modifier) {
    if (dataPoints.size < 2) {
        Text(
            "Collecting trend data...",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }

    val minVal = dataPoints.min()
    val maxVal = dataPoints.max()

    Canvas(modifier = modifier.fillMaxWidth().height(50.dp)) {
        val range = (maxVal - minVal).coerceAtLeast(1).toFloat()
        val stepX = size.width / (dataPoints.size - 1)
        val padding = 4f

        val path = Path()
        val fillPath = Path()

        dataPoints.forEachIndexed { index, value ->
            val x = index * stepX
            val normalised = (value - minVal) / range
            val y = size.height - padding - normalised * (size.height - padding * 2)
            if (index == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, y)
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
        }

        // Fill gradient area
        fillPath.lineTo((dataPoints.size - 1) * stepX, size.height)
        fillPath.lineTo(0f, size.height)
        fillPath.close()
        drawPath(fillPath, color.copy(alpha = 0.15f))

        // Line
        drawPath(path, color, style = Stroke(
            width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round
        ))
    }
}

@Composable
fun StackedComparisonView(
    leftLabel: String, leftBytes: Int, leftColor: Color,
    rightLabel: String, rightBytes: Int, rightColor: Color
) {
    val total = leftBytes + rightBytes
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        if (total > 0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
            ) {
                Box(
                    modifier = Modifier
                        .weight(leftBytes.toFloat().coerceAtLeast(1f))
                        .fillMaxHeight()
                        .background(leftColor)
                )
                Box(
                    modifier = Modifier
                        .weight(rightBytes.toFloat().coerceAtLeast(1f))
                        .fillMaxHeight()
                        .background(rightColor)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(leftColor))
                Spacer(modifier = Modifier.width(4.dp))
                Text(leftLabel, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.width(4.dp))
                Text(StorageBreakdown.formatBytes(leftBytes), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(rightColor))
                Spacer(modifier = Modifier.width(4.dp))
                Text(rightLabel, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.width(4.dp))
                Text(StorageBreakdown.formatBytes(rightBytes), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun HistogramView(buckets: List<DocSizeBucket>, barColor: Color) {
    val maxCount = buckets.maxOfOrNull { it.count } ?: 1
    val totalDocs = buckets.sumOf { it.count }
    if (totalDocs == 0) {
        Text("No documents to analyze.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().height(80.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            buckets.forEach { bucket ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (bucket.count > 0) {
                        Text(
                            "${bucket.count}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = barColor
                        )
                    }
                    val height = if (maxCount > 0) {
                        (bucket.count.toFloat() / maxCount * 60).coerceAtLeast(2f)
                    } else 2f
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(height.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(barColor)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            buckets.forEach { bucket ->
                Text(
                    bucket.label,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun HorizontalBarChartView(
    items: List<Pair<String, Int>>,
    barColor: Color,
    valueFormatter: (Int) -> String = { StorageBreakdown.formatBytes(it) }
) {
    val maxBytes = items.maxOfOrNull { it.second } ?: 1
    if (items.isEmpty()) {
        Text("No collection data available.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 4.dp)) {
        items.take(10).forEachIndexed { index, (label, bytes) ->
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(label, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                    Text(valueFormatter(bytes), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                val fraction = if (maxBytes > 0) bytes.toFloat() / maxBytes else 0f
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction.coerceAtLeast(0.01f))
                        .height(8.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(barColor.copy(alpha = 1f - index * 0.07f))
                )
            }
        }
    }
}