package org.apptolast.menuadmin.presentation.screens.settings

data class SettingsUiState(
    val isLoading: Boolean = false,
    val isDarkTheme: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
)
