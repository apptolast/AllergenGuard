package org.apptolast.menuadmin.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.toRoute
import org.apptolast.menuadmin.navigation.BackupRestoreRoute
import org.apptolast.menuadmin.navigation.DashboardRoute
import org.apptolast.menuadmin.navigation.IngredientsRoute
import org.apptolast.menuadmin.navigation.ProfileRoute
import org.apptolast.menuadmin.navigation.RestaurantDetailRoute
import org.apptolast.menuadmin.navigation.RestaurantsRoute
import org.apptolast.menuadmin.navigation.SettingsRoute
import org.apptolast.menuadmin.presentation.screens.backup.BackupRestoreScreen
import org.apptolast.menuadmin.presentation.screens.dashboard.DashboardScreen
import org.apptolast.menuadmin.presentation.screens.ingredients.IngredientsScreen
import org.apptolast.menuadmin.presentation.screens.profile.ProfileScreen
import org.apptolast.menuadmin.presentation.screens.restaurants.RestaurantsListScreen
import org.apptolast.menuadmin.presentation.screens.restaurants.workspace.RestaurantWorkspace
import org.apptolast.menuadmin.presentation.screens.settings.SettingsScreen

@Composable
fun AdminLayout(
    navController: NavHostController,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Row(modifier = modifier.fillMaxSize()) {
        Sidebar(
            currentRoute = currentRoute,
            onNavigate = { route ->
                navController.navigate(route) {
                    popUpTo(navController.graph.startDestinationId) {
                        saveState = route !is RestaurantsRoute
                    }
                    launchSingleTop = true
                    restoreState = route !is RestaurantsRoute
                }
            },
            onLogout = onLogout,
            modifier = Modifier
                .width(260.dp)
                .fillMaxHeight(),
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.background),
        ) {
            NavHost(
                navController = navController,
                startDestination = DashboardRoute,
            ) {
                composable<DashboardRoute> {
                    DashboardScreen()
                }
                composable<IngredientsRoute> {
                    IngredientsScreen()
                }
                composable<RestaurantsRoute> {
                    RestaurantsListScreen(
                        onNavigateToRestaurant = { restaurantId ->
                            navController.navigate(RestaurantDetailRoute(restaurantId)) {
                                launchSingleTop = true
                            }
                        },
                    )
                }
                composable<RestaurantDetailRoute> { backStackEntry ->
                    val route = backStackEntry.toRoute<RestaurantDetailRoute>()
                    RestaurantWorkspace(
                        restaurantId = route.restaurantId,
                        onBack = { navController.popBackStack() },
                    )
                }
                composable<SettingsRoute> {
                    SettingsScreen()
                }
                composable<BackupRestoreRoute> {
                    BackupRestoreScreen()
                }
                composable<ProfileRoute> {
                    ProfileScreen(onAccountDeleted = onLogout)
                }
            }
        }
    }
}
