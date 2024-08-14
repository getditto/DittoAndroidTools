package live.ditto.dittotoolsviewer.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import live.ditto.dittotoolsviewer.presentation.navigation.Screens
import live.ditto.dittotoolsviewer.presentation.ui.theme.MenuCardContainerColor
import live.ditto.dittotoolsviewer.presentation.ui.theme.MenuItemSelectedBackgroundColor
import live.ditto.dittotoolsviewer.presentation.ui.theme.MenuItemTextColor
import live.ditto.dittotoolsviewer.presentation.ui.theme.ToolsMenuHeaderBackground
import live.ditto.dittotoolsviewer.presentation.ui.theme.ToolsMenuHeaderTextColor

@Composable
fun ToolsMenu(
    navController: NavHostController,
    menuItems: List<ToolMenuItem>,
    onExit: () -> Unit,
    onDismissRequest: () -> Unit
) {
    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MenuCardContainerColor
            )
        ) {
            ToolsMenuHeader(
                onExit = onExit
            )
            ToolsMenuItems(
                menuItems = menuItems,
                onDismissRequest = onDismissRequest,
                navController = navController
            )
        }
    }
}

@Composable
private fun ToolsMenuItems(
    navController: NavHostController,
    menuItems: List<ToolMenuItem>,
    onDismissRequest: () -> Unit
) {
    Column(
        modifier = Modifier.padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        menuItems.forEach { toolMenuItem ->
            ToolMenuItem(
                name = toolMenuItem.label,
                onClick = {
                    onDismissRequest()
                    navController.navigate(toolMenuItem.route) {
                        popUpTo(route = Screens.MainScreen.route)
                    }
                }
            )
        }
    }
}

@Composable
private fun ToolMenuItem(
    name: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MenuItemSelectedBackgroundColor
        ),
        shape = RoundedCornerShape(24.dp),
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                color = MenuItemTextColor
            )
        }
    }
}

@Composable
private fun ToolsMenuHeader(
    onExit: () -> Unit = { }
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = ToolsMenuHeaderBackground)
            .padding(8.dp)
    ) {
        Row {
            IconButton(onClick = { onExit() }) {
                Icon(Icons.Filled.Close, "Close")
            }
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "Ditto Tools",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                color = ToolsMenuHeaderTextColor
            )
        }
    }
}

@Preview
@Composable
private fun ToolMenuItemPreview() {
    ToolMenuItem(
        name = "Tool Menu Item",
        onClick = { }
    )
}

@Preview
@Composable
private fun ToolsMenuHeaderPreview() {
    ToolsMenuHeader()
}

@Preview
@Composable
private fun ToolsMenuPreview() {
    ToolsMenu(
        navController = rememberNavController(),
        menuItems = emptyList(),
        onExit = { },
        onDismissRequest = { }
    )
}

data class ToolMenuItem(
    val label: String,
    val route: String
)