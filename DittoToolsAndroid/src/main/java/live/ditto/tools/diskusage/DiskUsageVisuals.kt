package live.ditto.tools.diskusage

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp

// Health Gauge

@Composable
fun HealthGaugeView(currentBytes: Int, thresholdBytes: Int) {
    val status = HealthStatus.from(currentBytes, thresholdBytes)
    val fraction = if (thresholdBytes > 0) {
        (currentBytes.toDouble() / thresholdBytes).coerceIn(0.0, 1.0)
    } else 0.0

    val animatedFraction by animateFloatAsState(
        targetValue = fraction.toFloat(),
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
                "${(fraction * 100).toInt()}%",
                fontWeight = FontWeight.Bold,
                color = status.tint
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
                    .background(status.tint)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("0", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                "Threshold: ${Format.bytes(thresholdBytes)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Sparkline

@Composable
fun SparklineView(dataPoints: List<Int>, color: Color, modifier: Modifier = Modifier) {
    if (dataPoints.size < 2) {
        Text(
            "Collecting trend data…",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }

    val minVal = dataPoints.min()
    val maxVal = dataPoints.max()
    // If range < 1 KB, render flat instead of making noise look dramatic
    val effectiveRange = (maxVal - minVal).coerceAtLeast(1024)

    Canvas(modifier = modifier.fillMaxWidth().height(50.dp)) {
        val range = effectiveRange.toFloat()
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

        fillPath.lineTo((dataPoints.size - 1) * stepX, size.height)
        fillPath.lineTo(0f, size.height)
        fillPath.close()
        drawPath(fillPath, color.copy(alpha = 0.15f))

        drawPath(path, color, style = Stroke(
            width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round
        ))
    }
}

// Horizontal Bar Chart

@Composable
fun HorizontalBarChartView(
    items: List<Pair<String, Int>>,
    barColor: Color,
    valueFormatter: (Int) -> String = { Format.bytes(it) }
) {
    val maxValue = items.maxOfOrNull { it.second } ?: 1
    if (items.isEmpty()) {
        Text(
            "No data available.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        items.take(10).forEachIndexed { index, (label, value) ->
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(label, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                    Text(
                        valueFormatter(value),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                val fraction = if (maxValue > 0) value.toFloat() / maxValue else 0f
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

// Growth Rate

@Composable
fun GrowthRateView(bytesPerSecond: Double?) {
    val perMinute = bytesPerSecond?.let { it * 60 }
    val (text, color) = when {
        bytesPerSecond == null -> "Collecting data…" to MaterialTheme.colorScheme.onSurfaceVariant
        bytesPerSecond > 1_000_000 -> "+${Format.bytes(perMinute!!.toInt())} / min" to Color.Red
        bytesPerSecond > 100 -> "+${Format.bytes(perMinute!!.toInt())} / min" to Color(0xFFFF9800)
        bytesPerSecond < -100 -> "-${Format.bytes((-perMinute!!).toInt())} / min" to Color(0xFF4CAF50)
        else -> "Stable" to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Text(
        text,
        fontWeight = FontWeight.Bold,
        color = color,
        style = MaterialTheme.typography.bodySmall
    )
}

// Time to Threshold

@Composable
fun TimeToThresholdView(estimatedSeconds: Double?, remainingBytes: Int) {
    val isPast = (estimatedSeconds != null && estimatedSeconds <= 0) || remainingBytes <= 0

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                if (isPast) "Threshold exceeded" else "Time to threshold",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            val primaryText = when {
                isPast -> "Over threshold"
                estimatedSeconds != null -> formatTimeInterval(estimatedSeconds)
                else -> "Not enough data yet"
            }
            val primaryColor = when {
                isPast -> Color.Red
                estimatedSeconds == null -> MaterialTheme.colorScheme.onSurfaceVariant
                estimatedSeconds < 3600 -> Color.Red
                estimatedSeconds < 86400 -> Color(0xFFFF9800)
                else -> Color(0xFF4CAF50)
            }
            Text(
                primaryText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = primaryColor
            )
        }
        if (remainingBytes != 0) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    if (isPast) "Over by" else "Remaining",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    Format.bytes(kotlin.math.abs(remainingBytes)),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
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