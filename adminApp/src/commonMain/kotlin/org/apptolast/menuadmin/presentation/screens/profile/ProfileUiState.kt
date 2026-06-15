package org.apptolast.menuadmin.presentation.screens.profile

data class ProfileUiState(
    val isLoading: Boolean = false,
    val email: String? = null,
    val userId: String? = null,
    val emailVerified: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
)
