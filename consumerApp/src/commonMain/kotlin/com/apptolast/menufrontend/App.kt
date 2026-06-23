package com.apptolast.menufrontend

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import com.apptolast.menufrontend.core.i18n.AppEnvironment
import com.apptolast.menufrontend.core.navigation.Navigation
import com.apptolast.menufrontend.core.theme.AllergenGuardTheme
import org.apptolast.menuadmin.data.local.LanguagePreferences
import org.koin.compose.koinInject

@Composable
fun App() {
    // Coil singleton with a Ktor-based network fetcher so AsyncImage can load dish photos from
    // Firebase Storage URLs on every platform.
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components { add(KtorNetworkFetcherFactory()) }
            .build()
    }

    val languagePreferences: LanguagePreferences = koinInject()
    val language by languagePreferences.languageFlow.collectAsState()

    AppEnvironment(locale = language) {
        AllergenGuardTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {
                Navigation()
            }
        }
    }
}
