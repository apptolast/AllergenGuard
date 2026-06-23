package org.apptolast.menuadmin.presentation.screens.platform.accountdetail

import org.apptolast.menuadmin.domain.model.AccountRole
import org.apptolast.menuadmin.domain.model.AccountUser
import org.apptolast.menuadmin.domain.model.Restaurant

data class PlatformAccountDetailUiState(
    val isLoading: Boolean = true,
    val accountName: String = "",
    val users: List<AccountUser> = emptyList(),
    val accountRestaurants: List<Restaurant> = emptyList(),
    // Invite / edit form (editingUser == null => invite a new email).
    val isFormVisible: Boolean = false,
    val editingUser: AccountUser? = null,
    val formEmail: String = "",
    val formRole: AccountRole = AccountRole.ACCOUNT_ADMIN,
    val formRestaurantIds: Set<String> = emptySet(),
    val isSaving: Boolean = false,
    // Remove-user confirmation.
    val removingUser: AccountUser? = null,
    val isRemoving: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
)
