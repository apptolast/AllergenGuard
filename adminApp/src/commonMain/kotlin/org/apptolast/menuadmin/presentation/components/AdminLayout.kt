package org.apptolast.menuadmin.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.toRoute
import menuadmin.adminapp.generated.resources.Res
import menuadmin.adminapp.generated.resources.layout_exit
import menuadmin.adminapp.generated.resources.layout_managing_account
import org.apptolast.menuadmin.data.CurrentAccountHolder
import org.apptolast.menuadmin.navigation.BackupRestoreRoute
import org.apptolast.menuadmin.navigation.DashboardRoute
import org.apptolast.menuadmin.navigation.IngredientsRoute
import org.apptolast.menuadmin.navigation.PlatformAccountDetailRoute
import org.apptolast.menuadmin.navigation.PlatformAccountsRoute
import org.apptolast.menuadmin.navigation.ProfileRoute
import org.apptolast.menuadmin.navigation.RestaurantDetailRoute
import org.apptolast.menuadmin.navigation.RestaurantsRoute
import org.apptolast.menuadmin.navigation.SettingsRoute
import org.apptolast.menuadmin.presentation.screens.backup.BackupRestoreScreen
import org.apptolast.menuadmin.presentation.screens.dashboard.DashboardScreen
import org.apptolast.menuadmin.presentation.screens.ingredients.IngredientsScreen
import org.apptolast.menuadmin.presentation.screens.platform.accountdetail.PlatformAccountDetailScreen
import org.apptolast.menuadmin.presentation.screens.platform.accounts.PlatformAccountsScreen
import org.apptolast.menuadmin.presentation.screens.profile.ProfileScreen
import org.apptolast.menuadmin.presentation.screens.restaurants.RestaurantsListScreen
import org.apptolast.menuadmin.presentation.screens.restaurants.workspace.RestaurantWorkspace
import org.apptolast.menuadmin.presentation.screens.settings.SettingsScreen
import org.apptolast.menuadmin.presentation.theme.Blue600
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun AdminLayout(
    navController: NavHostController,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Role drives which sidebar tools are shown (admin-only tools are hidden for managers; the Platform
    // section only for platform owners).
    val accountHolder: CurrentAccountHolder = koinInject()
    val session by accountHolder.session.collectAsState()
    val isAccountAdmin = session?.isAccountAdmin ?: false
    val isSuperAdmin by accountHolder.isSuperAdmin.collectAsState()
    // When a super-admin is "managing" a tenant, a banner is shown and Exit restores their own session.
    val impersonatedAccount by accountHolder.impersonatedAccount.collectAsState()

    Row(modifier = modifier.fillMaxSize()) {
        Sidebar(
            currentDestination = currentDestination,
            isAccountAdmin = isAccountAdmin,
            isSuperAdmin = isSuperAdmin,
            onNavigate = { route ->
                navController.navigate(route) {
                    // Always pop back to Dashboard (the start destination) without removing it, so
                    // every top-level tab is one hop from the root and Dashboard is always reachable.
                    // No saveState/restoreState — deterministic on wasmJs.
                    popUpTo(DashboardRoute) { inclusive = false }
                    launchSingleTop = true
                }
            },
            onLogout = onLogout,
            modifier = Modifier
                .width(260.dp)
                .fillMaxHeight(),
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.background),
        ) {
            impersonatedAccount?.let { account ->
                ImpersonationBanner(
                    accountName = account.name,
                    onExit = {
                        accountHolder.exitImpersonation()
                        navController.navigate(PlatformAccountsRoute) {
                            popUpTo(DashboardRoute) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                )
            }
            NavHost(
                navController = navController,
                startDestination = DashboardRoute,
                modifier = Modifier.weight(1f),
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
                composable<PlatformAccountsRoute> {
                    PlatformAccountsScreen(
                        onOpenAccount = { accountId ->
                            navController.navigate(PlatformAccountDetailRoute(accountId)) {
                                launchSingleTop = true
                            }
                        },
                        onManageAccount = {
                            // Impersonation already entered in the VM; open the scoped workspace.
                            navController.navigate(DashboardRoute) {
                                popUpTo(DashboardRoute) { inclusive = false }
                                launchSingleTop = true
                            }
                        },
                    )
                }
                composable<PlatformAccountDetailRoute> { backStackEntry ->
                    val route = backStackEntry.toRoute<PlatformAccountDetailRoute>()
                    PlatformAccountDetailScreen(
                        accountId = route.accountId,
                        onBack = { navController.popBackStack() },
                    )
                }
            }
        }
    }
}

/** Banner shown while a super-admin is managing a tenant, with a clear way back to the Platform panel. */
@Composable
private fun ImpersonationBanner(
    accountName: String,
    onExit: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Blue600)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(Res.string.layout_managing_account, accountName),
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
        )
        TextButton(onClick = onExit) {
            Text(stringResource(Res.string.layout_exit), color = Color.White, fontWeight = FontWeight.SemiBold)
        }
    }
}
