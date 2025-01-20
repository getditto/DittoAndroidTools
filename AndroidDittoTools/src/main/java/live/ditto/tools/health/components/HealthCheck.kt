package live.ditto.tools.health.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import live.ditto.health.theme.HealthTypography
import live.ditto.health.theme.healthyBackgroundColor
import live.ditto.health.theme.healthyIconColor
import live.ditto.health.theme.notHealthyBackgroundColor
import live.ditto.health.theme.notHealthyIconColor

@Composable
internal fun HealthCheckWithNoAction(
    header: String,
    isHealthy: Boolean,
    description: String,
    modifier: Modifier = Modifier,
) {
    HealthCheck(
        header = header,
        isHealthy = isHealthy,
        modifier = modifier,
    ) {
        Text(
            text = description,
            style = HealthTypography.bodyMedium
        )
    }
}

@Composable
internal fun HealthCheckWithAction(
    header: String,
    isHealthy: Boolean,
    description: String,
    actionText: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    HealthCheck(
        header = header,
        isHealthy = isHealthy,
        modifier = modifier,
    ) {
        Text(
            text = description,
            style = HealthTypography.bodyMedium
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        OutlinedButton(
            onClick = onAction,
            shape = RoundedCornerShape(12)
        ) {
            Text(text = actionText)
        }
    }
}

@Composable
private fun HealthCheck(
    header: String,
    isHealthy: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    ElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(4)
    ) {
        val headerBackgroundColor = headerBackgroundColor(isHealthy)

        Row(
            modifier = Modifier
                .background(color = headerBackgroundColor)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = header,
                color = Color.White,
                style = HealthTypography.titleSmall
            )

            Spacer(modifier = Modifier.weight(1f))

            HealthIndicator(isHealthy = isHealthy)
        }

        Spacer(modifier = Modifier.height(4.dp))

        Column(modifier = Modifier.padding(horizontal = 8.dp)) {
            content()
        }

        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
private fun HealthIndicator(isHealthy: Boolean) {
    val icon = if (isHealthy) Icons.Default.Check else Icons.Default.Warning
    val iconTintColor = if (isHealthy) healthyIconColor else notHealthyIconColor

    Icon(
        painter = rememberVectorPainter(image = icon),
        contentDescription = null,
        tint = iconTintColor
    )
}

private fun headerBackgroundColor(isHealthy: Boolean) = if (isHealthy) {
    healthyBackgroundColor
} else {
    notHealthyBackgroundColor
}

@Preview
@Composable
private fun HealthyCheckWithNoActionPreview() {
    HealthCheckWithNoAction(
        header = "Healthy",
        isHealthy = true,
        description = "Content",
    )
}

@Preview
@Composable
private fun NotHealthyCheckWithNoActionPreview() {
    HealthCheckWithNoAction(
        header = "Not Healthy",
        isHealthy = false,
        description = "Content",
    )
}

@Preview
@Composable
private fun HealthCheckWithActionPreview() {
    HealthCheckWithAction(
        header = "Header",
        isHealthy = false,
        description = "Content",
        actionText = "Request Permissions",
        onAction = { }
    )
}
