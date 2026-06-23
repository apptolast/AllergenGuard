package org.apptolast.menuadmin.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.apptolast.menuadmin.data.local.LanguagePreferences
import org.apptolast.menuadmin.data.local.ThemePreferences

class SettingsViewModel(
    private val themePreferences: ThemePreferences,
    private val languagePreferences: LanguagePreferences,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        SettingsUiState(
            isDarkTheme = themePreferences.isDarkTheme,
            language = languagePreferences.language,
        ),
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        // Keep the radio selection in sync if the language is changed elsewhere.
        viewModelScope.launch {
            languagePreferences.languageFlow.collect { lang ->
                _uiState.update { it.copy(language = lang) }
            }
        }
    }

    fun onToggleDarkTheme(enabled: Boolean) {
        themePreferences.isDarkTheme = enabled
        _uiState.update { it.copy(isDarkTheme = enabled) }
    }

    /** [language] is "es"/"en", or null to follow the system locale. */
    fun onSelectLanguage(language: String?) {
        languagePreferences.setLanguage(language)
    }

    fun dismissMessage() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}
