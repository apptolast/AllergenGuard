package org.apptolast.menuadmin.presentation.screens.profile

data class ProfileUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
)
