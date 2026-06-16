package org.apptolast.menuadmin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.rememberNavController
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import org.apptolast.menuadmin.data.local.ThemePreferences
import org.apptolast.menuadmin.presentation.components.AdminLayout
import org.apptolast.menuadmin.presentation.screens.auth.AuthScreen
import org.apptolast.menuadmin.presentation.screens.auth.AuthViewModel
import org.apptolast.menuadmin.presentation.theme.MenuAdminTheme
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App() {
    // Coil singleton with a Ktor network fetcher so AsyncImage can preview dish photos from
    // Firebase Storage URLs (uses the wasmJs ktor-client-js engine).
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components { add(KtorNetworkFetcherFactory()) }
            .build()
    }

    val themePreferences: ThemePreferences = koinInject()
    val isDarkTheme by themePreferences.isDarkThemeFlow.collectAsState()

    MenuAdminTheme(darkTheme = isDarkTheme) {
        val authViewModel: AuthViewModel = koinViewModel()
        val authState by authViewModel.uiState.collectAsState()

        if (authState.isAuthenticated) {
            val navController = rememberNavController()
            AdminLayout(
                navController = navController,
                onLogout = authViewModel::onLogout,
            )
        } else {
            // Share the same AuthViewModel instance App observes, so a successful login flips
            // authState.isAuthenticated here and navigates to AdminLayout.
            AuthScreen(viewModel = authViewModel, onAuthenticated = {})
        }
    }
}
