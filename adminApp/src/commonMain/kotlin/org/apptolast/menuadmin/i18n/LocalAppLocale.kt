package org.apptolast.menuadmin.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.key

/**
 * Lets the app override the locale used by Compose `stringResource` at runtime, independently of the
 * system locale. Follows the official JetBrains "manage local resource environment" pattern
 * (https://kotlinlang.org/docs/multiplatform/compose-resource-environment.html).
 *
 * A `null` locale means "follow the device/system locale".
 */
expect object LocalAppLocale {
    val current: String
        @Composable get

    @Composable
    infix fun provides(value: String?): ProvidedValue<*>
}

/**
 * Wraps [content] so every `stringResource` inside resolves against [locale] (e.g. "es" / "en"), or
 * the system locale when [locale] is `null`. The `key(locale)` forces the subtree to recompose when
 * the language changes so already-read strings are re-resolved.
 */
@Composable
fun AppEnvironment(
    locale: String?,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalAppLocale provides locale) {
        key(locale) {
            content()
        }
    }
}
