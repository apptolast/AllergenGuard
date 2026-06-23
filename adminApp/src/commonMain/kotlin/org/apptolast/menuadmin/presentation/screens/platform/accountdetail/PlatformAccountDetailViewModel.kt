package org.apptolast.menuadmin.presentation.screens.platform.accountdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import menuadmin.adminapp.generated.resources.Res
import menuadmin.adminapp.generated.resources.error_unknown
import menuadmin.adminapp.generated.resources.platform_detail_error_deleting
import menuadmin.adminapp.generated.resources.platform_detail_error_loading
import menuadmin.adminapp.generated.resources.platform_detail_error_saving
import menuadmin.adminapp.generated.resources.platform_detail_invitation_created
import menuadmin.adminapp.generated.resources.platform_detail_user_removed
import menuadmin.adminapp.generated.resources.platform_detail_user_updated
import org.apptolast.menuadmin.domain.model.AccountRole
import org.apptolast.menuadmin.domain.model.AccountUser
import org.apptolast.menuadmin.domain.platform.EmailSender
import org.apptolast.menuadmin.domain.repository.PlatformAdminRepository
import org.apptolast.menuadmin.presentation.screens.platform.buildInvitationEmail
import org.jetbrains.compose.resources.getString

class PlatformAccountDetailViewModel(
    private val repository: PlatformAdminRepository,
    private val emailSender: EmailSender,
    private val accountId: String,
) : ViewModel() {
    private val _state = MutableStateFlow(PlatformAccountDetailUiState())
    val uiState: StateFlow<PlatformAccountDetailUiState> = _state.asStateFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val account = repository.getAccount(accountId)
                val users = repository.getAccountUsers(accountId)
                val restaurants = repository.getAccountRestaurants(accountId)
                _state.update {
                    it.copy(
                        isLoading = false,
                        accountName = account?.name ?: accountId,
                        accountLanguage = account?.language ?: "es",
                        users = users,
                        accountRestaurants = restaurants,
                    )
                }
            } catch (e: Exception) {
                val msg = e.message ?: getString(Res.string.platform_detail_error_loading)
                _state.update { it.copy(isLoading = false, error = msg) }
            }
        }
    }

    fun onInviteUser() {
        _state.update {
            it.copy(
                isFormVisible = true,
                editingUser = null,
                formEmail = "",
                formRole = AccountRole.ACCOUNT_ADMIN,
                formRestaurantIds = emptySet(),
                error = null,
            )
        }
    }

    fun onEditUser(user: AccountUser) {
        _state.update {
            it.copy(
                isFormVisible = true,
                editingUser = user,
                formEmail = user.email,
                formRole = user.role,
                formRestaurantIds = user.restaurantIds.toSet(),
                error = null,
            )
        }
    }

    fun onFormEmailChange(value: String) = _state.update { it.copy(formEmail = value) }

    fun onFormRoleChange(role: AccountRole) =
        _state.update {
            it.copy(
                formRole = role,
                formRestaurantIds = if (role ==
                    AccountRole.ACCOUNT_ADMIN
                ) {
                    emptySet()
                } else {
                    it.formRestaurantIds
                },
            )
        }

    fun onToggleRestaurant(restaurantId: String) =
        _state.update {
            val next = if (restaurantId in
                it.formRestaurantIds
            ) {
                it.formRestaurantIds - restaurantId
            } else {
                it.formRestaurantIds + restaurantId
            }
            it.copy(formRestaurantIds = next)
        }

    fun onDismissForm() = _state.update { it.copy(isFormVisible = false, editingUser = null) }

    fun onSaveUser() {
        val s = _state.value
        val email = s.formEmail.trim()
        if (email.isBlank()) return
        val restaurantIds = if (s.formRole ==
            AccountRole.RESTAURANT_MANAGER
        ) {
            s.formRestaurantIds.toList()
        } else {
            emptyList()
        }
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }
            try {
                val editing = s.editingUser
                if (editing != null) {
                    repository.updateUser(editing.uid, editing.email, accountId, s.formRole, restaurantIds)
                } else {
                    repository.inviteUser(accountId, email, s.formRole, restaurantIds)
                    // Best-effort invitation email: never fail the invitation if mail delivery fails.
                    runCatching {
                        emailSender.sendInvitationEmail(
                            buildInvitationEmail(
                                email = email,
                                accountName = s.accountName,
                                role = s.formRole,
                                accountLanguage = s.accountLanguage,
                            ),
                        )
                    }
                }
                val successMessage =
                    if (editing != null) {
                        getString(Res.string.platform_detail_user_updated)
                    } else {
                        getString(Res.string.platform_detail_invitation_created)
                    }
                _state.update {
                    it.copy(
                        isSaving = false,
                        isFormVisible = false,
                        editingUser = null,
                        successMessage = successMessage,
                    )
                }
                load()
            } catch (e: Exception) {
                val msg = getString(
                    Res.string.platform_detail_error_saving,
                    e.message ?: getString(Res.string.error_unknown),
                )
                _state.update { it.copy(isSaving = false, error = msg) }
            }
        }
    }

    fun onRequestRemove(user: AccountUser) = _state.update { it.copy(removingUser = user, error = null) }

    fun onDismissRemove() = _state.update { it.copy(removingUser = null) }

    fun onConfirmRemove() {
        val user = _state.value.removingUser ?: return
        viewModelScope.launch {
            _state.update { it.copy(isRemoving = true, error = null) }
            try {
                repository.removeUser(user.uid, user.email)
                val successMessage = getString(Res.string.platform_detail_user_removed)
                _state.update { it.copy(isRemoving = false, removingUser = null, successMessage = successMessage) }
                load()
            } catch (e: Exception) {
                val msg = getString(
                    Res.string.platform_detail_error_deleting,
                    e.message ?: getString(Res.string.error_unknown),
                )
                _state.update { it.copy(isRemoving = false, error = msg) }
            }
        }
    }

    fun dismissMessage() = _state.update { it.copy(error = null, successMessage = null) }
}
