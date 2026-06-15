package org.apptolast.menuadmin.data.local

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemePreferences(
    private val settings: Settings = Settings(),
) {
    private val _isDarkTheme = MutableStateFlow(settings.getBoolean(KEY_DARK_THEME, false))
    val isDarkThemeFlow: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    var isDarkTheme: Boolean
        get() = _isDarkTheme.value
        set(value) {
            settings.putBoolean(KEY_DARK_THEME, value)
            _isDarkTheme.value = value
        }

    private companion object {
        const val KEY_DARK_THEME = "dark_theme"
    }
}
