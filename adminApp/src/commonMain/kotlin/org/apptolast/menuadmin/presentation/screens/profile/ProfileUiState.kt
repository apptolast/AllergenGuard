package org.apptolast.menuadmin.presentation.screens.profile

data class ProfileUiState(
    val isLoading: Boolean = false,
    val name: String? = null,
    val email: String? = null,
    val userId: String? = null,
    /** Human-readable role within the account (e.g. "Administrador", "Encargado de restaurante"). */
    val roleLabel: String? = null,
    val emailVerified: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
)
