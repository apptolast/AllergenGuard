package org.apptolast.menuadmin

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import org.apptolast.menuadmin.data.local.ThemePreferences
import org.apptolast.menuadmin.presentation.components.AdminLayout
import org.apptolast.menuadmin.presentation.components.AppSnackbarHost
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

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                // Resolving the tenant membership of an already-signed-in user before entering the app,
                // so scoped repositories always have a resolved account.
                authState.isResolvingSession -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                authState.isAuthenticated -> {
                    val navController = rememberNavController()
                    AdminLayout(
                        navController = navController,
                        onLogout = authViewModel::onLogout,
                    )
                }

                else -> {
                    // Share the same AuthViewModel instance App observes, so a successful login flips
                    // authState.isAuthenticated here and navigates to AdminLayout.
                    AuthScreen(viewModel = authViewModel, onAuthenticated = {})
                }
            }

            // Single app-wide snackbar host: every screen's errors surface here instead of red text.
            AppSnackbarHost(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
            )
        }
    }
}
