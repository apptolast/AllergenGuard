package org.apptolast.menuadmin.presentation.screens.platform.accountdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.apptolast.menuadmin.domain.model.AccountRole
import org.apptolast.menuadmin.domain.model.AccountUser
import org.apptolast.menuadmin.domain.repository.PlatformAdminRepository

class PlatformAccountDetailViewModel(
    private val repository: PlatformAdminRepository,
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
                        users = users,
                        accountRestaurants = restaurants,
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message ?: "Error al cargar usuarios") }
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
                }
                _state.update {
                    it.copy(
                        isSaving = false,
                        isFormVisible = false,
                        editingUser = null,
                        successMessage = if (editing !=
                            null
                        ) {
                            "Usuario actualizado"
                        } else {
                            "Invitación creada"
                        },
                    )
                }
                load()
            } catch (e: Exception) {
                _state.update { it.copy(isSaving = false, error = "Error al guardar: ${e.message ?: "desconocido"}") }
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
                _state.update { it.copy(isRemoving = false, removingUser = null, successMessage = "Usuario eliminado") }
                load()
            } catch (e: Exception) {
                _state.update {
                    it.copy(isRemoving = false, error = "Error al eliminar: ${e.message ?: "desconocido"}")
                }
            }
        }
    }

    fun dismissMessage() = _state.update { it.copy(error = null, successMessage = null) }
}
