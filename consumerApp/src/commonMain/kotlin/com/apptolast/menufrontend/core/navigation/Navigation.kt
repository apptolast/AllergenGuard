package com.apptolast.menufrontend.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.apptolast.menufrontend.core.screenshot.ScreenshotMode
import com.apptolast.menufrontend.data.demo.ScreenshotDemoData
import com.apptolast.menufrontend.features.dishdetail.presentation.DishDetailScreenRoot
import com.apptolast.menufrontend.features.favorites.presentation.FavoritesScreenRoot
import com.apptolast.menufrontend.features.home.presentation.HomeScreenRoot
import com.apptolast.menufrontend.features.login.presentation.LoginScreenRoot
import com.apptolast.menufrontend.features.menu.presentation.MenuScreenRoot
import com.apptolast.menufrontend.features.profile.presentation.ProfileScreenRoot
import com.apptolast.menufrontend.features.scanner.presentation.ScannerScreenRoot

@Composable
fun Navigation() {
    val navController = rememberNavController()

    fun navigateToTab(route: Any) {
        navController.navigate(route) {
            popUpTo(HomeRoute) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    // Normally the app starts at Login; in ScreenshotMode it jumps straight to the requested screen
    // (fed by the fake demo repositories) so fastlane can capture authenticated screens unattended.
    val startDestination: Destination = if (ScreenshotMode.enabled) {
        when (ScreenshotMode.startScreen) {
            "home" -> HomeRoute
            "menu" -> MenuRoute(ScreenshotDemoData.RESTAURANT_ID)
            "dish" -> DishDetailRoute(ScreenshotDemoData.DISH_ID, ScreenshotDemoData.RESTAURANT_ID)
            "profile" -> ProfileRoute
            else -> LoginRoute
        }
    } else {
        LoginRoute
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        composable<LoginRoute> {
            LoginScreenRoot(
                onLoginSuccess = {
                    navController.navigate(HomeRoute) {
                        popUpTo(LoginRoute) { inclusive = true }
                    }
                },
                onNavigateToRegister = { /* TODO */ },
            )
        }

        composable<HomeRoute> {
            HomeScreenRoot(
                onRestaurantClick = { restaurantId ->
                    navController.navigate(MenuRoute(restaurantId))
                },
                onNavigateToFavorites = { navigateToTab(FavoritesRoute) },
                onNavigateToProfile = { navigateToTab(ProfileRoute) },
                onNavigateToScanner = { navController.navigate(ScannerRoute) },
            )
        }

        composable<FavoritesRoute> {
            FavoritesScreenRoot(
                onRestaurantClick = { restaurantId ->
                    navController.navigate(MenuRoute(restaurantId))
                },
                onNavigateToExplore = { navigateToTab(HomeRoute) },
                onNavigateToProfile = { navigateToTab(ProfileRoute) },
            )
        }

        composable<ProfileRoute> {
            ProfileScreenRoot(
                onLogout = {
                    navController.navigate(LoginRoute) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToExplore = { navigateToTab(HomeRoute) },
                onNavigateToFavorites = { navigateToTab(FavoritesRoute) },
            )
        }

        composable<MenuRoute> { backStackEntry ->
            val route: MenuRoute = backStackEntry.toRoute()
            MenuScreenRoot(
                restaurantId = route.restaurantId,
                onDishClick = { dishId ->
                    navController.navigate(
                        DishDetailRoute(dishId, route.restaurantId),
                    )
                },
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable<DishDetailRoute> { backStackEntry ->
            val route: DishDetailRoute = backStackEntry.toRoute()
            DishDetailScreenRoot(
                dishId = route.dishId,
                restaurantId = route.restaurantId,
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable<ScannerRoute> {
            ScannerScreenRoot(
                onNavigateToMenu = { restaurantId ->
                    navController.navigate(MenuRoute(restaurantId)) {
                        popUpTo(ScannerRoute) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() },
            )
        }
    }
}
