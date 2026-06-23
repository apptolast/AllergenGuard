package org.apptolast.menuadmin.presentation.screens.platform.accounts

import org.apptolast.menuadmin.domain.model.Account

data class PlatformAccountsUiState(
    val isLoading: Boolean = true,
    val accounts: List<Account> = emptyList(),
    // Create / edit form (editingAccount == null => create).
    val isFormVisible: Boolean = false,
    val editingAccount: Account? = null,
    val formName: String = "",
    val formRegion: String = "EU",
    val formLanguage: String = "es",
    val formFirstAdminEmail: String = "",
    val isSaving: Boolean = false,
    // Cascade-delete confirmation (deletingAccount != null => dialog open).
    val deletingAccount: Account? = null,
    val deleteConfirmText: String = "",
    val isDeleting: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
)
