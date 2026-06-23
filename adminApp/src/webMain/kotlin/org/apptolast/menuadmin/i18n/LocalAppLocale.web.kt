package org.apptolast.menuadmin.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.intl.Locale
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.js

/**
 * Web actual: the locale is exposed to `navigator.languages` via `window.__customLocale`, which the
 * `locale-override.js` script (loaded in index.html before the app) reads. Setting it and then
 * re-reading [Locale.current] inside [provides] makes Compose resources pick up the new language.
 */
@OptIn(ExperimentalWasmJsInterop::class)
actual object LocalAppLocale {
    private val delegate = staticCompositionLocalOf { Locale.current }

    actual val current: String
        @Composable get() = delegate.current.toString()

    @Composable
    actual infix fun provides(value: String?): ProvidedValue<*> {
        setCustomLocale(value?.replace('_', '-').orEmpty())
        return delegate.provides(Locale.current)
    }
}

/** Empty string clears the override (follow system); otherwise pins `navigator.languages`. */
@OptIn(ExperimentalWasmJsInterop::class)
private fun setCustomLocale(value: String): Unit =
    js("(function() { window.__customLocale = value && value.length ? value : null; })()")
