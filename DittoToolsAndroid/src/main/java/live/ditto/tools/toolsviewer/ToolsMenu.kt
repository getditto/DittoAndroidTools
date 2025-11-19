package live.ditto.tools.toolsviewer

import androidx.annotation.StringRes
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import live.ditto.tools.toolsviewer.navigation.Screens
import live.ditto.tools.toolsviewer.theme.ToolsItemBackgroundDark
import live.ditto.tools.toolsviewer.theme.ToolsItemBackgroundLight
import live.ditto.tools.toolsviewer.theme.ToolsSectionHeaderDark
import live.ditto.tools.toolsviewer.theme.ToolsSectionHeaderLight


@Composable
fun ToolsMenu(
    navController: NavHostController,
    menuSections: List<ToolMenuSection>
) {
    // Colors that adapt to dark mode
    val isSystemInDarkTheme = androidx.compose.foundation.isSystemInDarkTheme()
    val itemBackgroundColor = if (isSystemInDarkTheme) ToolsItemBackgroundDark else ToolsItemBackgroundLight
    val itemTextColor = if (isSystemInDarkTheme) Color.White else Color.Black
    val sectionHeaderColor = if (isSystemInDarkTheme) ToolsSectionHeaderDark else ToolsSectionHeaderLight

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        menuSections.forEachIndexed { index, section ->
            // Add spacing before section headers (except the first one)
            if (index > 0) {
                Spacer(modifier = Modifier.padding(top = 4.dp))
            }

            // Section header
            Text(
                text = stringResource(id = section.title).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = sectionHeaderColor,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 0.dp)
            )

            // Section items
            section.items.forEach { toolMenuItem ->
                ToolMenuItem(
                    name = stringResource(id = toolMenuItem.label),
                    icon = toolMenuItem.icon,
                    containerColor = itemBackgroundColor,
                    textColor = itemTextColor,
                    onClick = {
                        navController.navigate(toolMenuItem.route) {
                            popUpTo(route = Screens.MainScreen.route)
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun ToolMenuItem(
    name: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector?,
    containerColor: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple()
            ) { onClick() }
            .onKeyEvent { keyEvent ->
                when (keyEvent.key) {
                    Key.Spacebar -> {
                        when (keyEvent.type) {
                            KeyEventType.KeyUp -> {
                                onClick()
                                true
                            }
                            else -> false
                        }
                    }
                    else -> false
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 6.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = name,
                    tint = textColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
            }
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                color = textColor
            )
        }
    }
}

@Preview
@Composable
private fun ToolMenuItemPreview() {
    ToolMenuItem(
        name = "Tool Menu Item",
        icon = androidx.compose.material.icons.Icons.Default.Settings,
        containerColor = Color.White,
        textColor = Color.Black,
        onClick = { }
    )
}

@Preview
@Composable
private fun ToolsMenuPreview() {
    ToolsMenu(
        navController = rememberNavController(),
        menuSections = emptyList()
    )
}

data class ToolMenuItem(
    @StringRes val label: Int,
    val route: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector? = null
)

data class ToolMenuSection(
    @StringRes val title: Int,
    val items: List<ToolMenuItem>
)