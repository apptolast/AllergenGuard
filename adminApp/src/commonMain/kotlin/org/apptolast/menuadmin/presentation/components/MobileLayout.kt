package org.apptolast.menuadmin.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import org.apptolast.menuadmin.presentation.screens.mobile.MobileExploreScreen
import org.apptolast.menuadmin.presentation.screens.mobile.MobileFavoritesScreen
import org.apptolast.menuadmin.presentation.screens.mobile.MobileProfileScreen
import org.apptolast.menuadmin.presentation.theme.Blue500
import org.apptolast.menuadmin.presentation.theme.MenuAdminTheme

private enum class MobileTab(
    val label: String,
    val icon: ImageVector,
) {
    EXPLORE("Explorar", Icons.Outlined.Explore),
    FAVORITES("Favoritos", Icons.Outlined.FavoriteBorder),
    PROFILE("Perfil", Icons.Outlined.Person),
}

@Composable
fun MobileLayout(modifier: Modifier = Modifier) {
    var currentTab by remember { mutableStateOf(MobileTab.EXPLORE) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                MobileTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = currentTab == tab,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label,
                            )
                        },
                        label = { Text(tab.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Blue500,
                            selectedTextColor = Blue500,
                            unselectedIconColor = MenuAdminTheme.colors.textMuted,
                            unselectedTextColor = MenuAdminTheme.colors.textMuted,
                            indicatorColor = MaterialTheme.colorScheme.surface,
                        ),
                    )
                }
            }
        },
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (currentTab) {
                MobileTab.EXPLORE -> MobileExploreScreen()
                MobileTab.FAVORITES -> MobileFavoritesScreen()
                MobileTab.PROFILE -> MobileProfileScreen()
            }
        }
    }
}
