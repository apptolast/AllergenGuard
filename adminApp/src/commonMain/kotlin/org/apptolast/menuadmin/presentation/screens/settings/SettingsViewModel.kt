package org.apptolast.menuadmin.presentation.screens.settings

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.apptolast.menuadmin.data.local.ThemePreferences

class SettingsViewModel(
    private val themePreferences: ThemePreferences,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        SettingsUiState(isDarkTheme = themePreferences.isDarkTheme),
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun onToggleDarkTheme(enabled: Boolean) {
        themePreferences.isDarkTheme = enabled
        _uiState.update { it.copy(isDarkTheme = enabled) }
    }

    fun dismissMessage() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}
