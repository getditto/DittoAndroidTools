package live.ditto.tools.toolsviewer

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import live.ditto.tools.R
import live.ditto.tools.toolsviewer.navigation.Screens
import live.ditto.tools.toolsviewer.theme.MenuCardContainerColor
import live.ditto.tools.toolsviewer.theme.MenuItemSelectedBackgroundColor
import live.ditto.tools.toolsviewer.theme.MenuItemTextColor
import live.ditto.tools.toolsviewer.theme.ToolsMenuHeaderBackground
import live.ditto.tools.toolsviewer.theme.ToolsMenuHeaderTextColor


@Composable
fun ToolsMenu(
    navController: NavHostController,
    menuItems: List<ToolMenuItem>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MenuCardContainerColor
        )
    ) {
        ToolsMenuHeader()
        ToolsMenuItems(
            menuItems = menuItems,
            navController = navController
        )
    }
}

@Composable
private fun ToolsMenuItems(
    navController: NavHostController,
    menuItems: List<ToolMenuItem>
) {
    Column(
        modifier = Modifier.padding(8.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        menuItems.forEach { toolMenuItem ->
            ToolMenuItem(
                name = stringResource(id = toolMenuItem.label),
                onClick = {
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
    containerColor: Color = MenuItemSelectedBackgroundColor,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
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
private fun ToolsMenuHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = ToolsMenuHeaderBackground)
            .padding(8.dp)
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(R.string.tools_menu_title),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            color = ToolsMenuHeaderTextColor
        )
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
        menuItems = emptyList()
    )
}

data class ToolMenuItem(
    @StringRes val label: Int,
    val route: String
)