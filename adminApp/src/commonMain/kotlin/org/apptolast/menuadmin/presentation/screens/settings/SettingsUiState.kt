package org.apptolast.menuadmin.presentation.screens.settings

data class SettingsUiState(
    val isLoading: Boolean = false,
    val isDarkTheme: Boolean = false,
    // Selected UI language: "es" / "en", or null to follow the system locale.
    val language: String? = null,
    val error: String? = null,
    val successMessage: String? = null,
)
