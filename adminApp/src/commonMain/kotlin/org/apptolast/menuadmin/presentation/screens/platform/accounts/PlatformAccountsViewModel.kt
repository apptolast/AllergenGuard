package org.apptolast.menuadmin.presentation.screens.platform.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.apptolast.menuadmin.data.CurrentAccountHolder
import org.apptolast.menuadmin.domain.model.Account
import org.apptolast.menuadmin.domain.platform.FileHandler
import org.apptolast.menuadmin.domain.repository.PlatformAdminRepository

class PlatformAccountsViewModel(
    private val repository: PlatformAdminRepository,
    private val fileHandler: FileHandler,
    private val currentAccountHolder: CurrentAccountHolder,
) : ViewModel() {
    private val _formState = MutableStateFlow(PlatformAccountsUiState())

    val uiState: StateFlow<PlatformAccountsUiState> = combine(
        repository.getAccounts(),
        _formState,
    ) { accounts, form ->
        form.copy(isLoading = false, accounts = accounts)
    }
        .catch { e ->
            emit(_formState.value.copy(isLoading = false, error = e.message ?: "Error al cargar cuentas"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PlatformAccountsUiState())

    fun onNewAccount() {
        _formState.update {
            it.copy(
                isFormVisible = true,
                editingAccount = null,
                formName = "",
                formRegion = "EU",
                formLanguage = "es",
                formFirstAdminEmail = "",
                error = null,
            )
        }
    }

    fun onEditAccount(account: Account) {
        _formState.update {
            it.copy(
                isFormVisible = true,
                editingAccount = account,
                formName = account.name,
                formRegion = account.region,
                formLanguage = account.language,
                formFirstAdminEmail = "",
                error = null,
            )
        }
    }

    fun onFormNameChange(value: String) = _formState.update { it.copy(formName = value) }

    fun onFormRegionChange(value: String) = _formState.update { it.copy(formRegion = value) }

    fun onFormLanguageChange(value: String) = _formState.update { it.copy(formLanguage = value) }

    fun onFormFirstAdminEmailChange(value: String) = _formState.update { it.copy(formFirstAdminEmail = value) }

    fun onDismissForm() = _formState.update { it.copy(isFormVisible = false, editingAccount = null) }

    fun onSaveAccount() {
        val s = _formState.value
        if (s.formName.isBlank()) return
        viewModelScope.launch {
            _formState.update { it.copy(isSaving = true, error = null) }
            try {
                val editing = s.editingAccount
                if (editing != null) {
                    repository.updateAccount(
                        editing.copy(name = s.formName, region = s.formRegion, language = s.formLanguage),
                    )
                } else {
                    repository.createAccount(
                        s.formName,
                        s.formRegion,
                        s.formLanguage,
                        s.formFirstAdminEmail.ifBlank {
                            null
                        },
                    )
                }
                _formState.update {
                    it.copy(
                        isSaving = false,
                        isFormVisible = false,
                        editingAccount = null,
                        successMessage = if (editing != null) "Cuenta actualizada" else "Cuenta creada",
                    )
                }
            } catch (e: Exception) {
                _formState.update {
                    it.copy(isSaving = false, error = "Error al guardar: ${e.message ?: "desconocido"}")
                }
            }
        }
    }

    fun onRequestDelete(account: Account) =
        _formState.update { it.copy(deletingAccount = account, deleteConfirmText = "", error = null) }

    fun onDeleteConfirmTextChange(value: String) = _formState.update { it.copy(deleteConfirmText = value) }

    fun onDismissDelete() = _formState.update { it.copy(deletingAccount = null, deleteConfirmText = "") }

    fun onConfirmDelete() {
        val account = _formState.value.deletingAccount ?: return
        // Guard: the typed name must match exactly (the button is also disabled otherwise).
        if (_formState.value.deleteConfirmText.trim() != account.name.trim()) return
        viewModelScope.launch {
            _formState.update { it.copy(isDeleting = true, error = null) }
            try {
                // Safety backup first: download a JSON dump of everything about to be deleted.
                val backup = repository.exportAccountBackup(account.id)
                fileHandler.saveFile(backup, "backup_${account.id}_pre-delete.json")
                repository.deleteAccountCascade(account.id)
                _formState.update {
                    it.copy(
                        isDeleting = false,
                        deletingAccount = null,
                        deleteConfirmText = "",
                        successMessage = "Cuenta '${account.name}' eliminada (backup descargado)",
                    )
                }
            } catch (e: Exception) {
                _formState.update {
                    it.copy(isDeleting = false, error = "Error al eliminar: ${e.message ?: "desconocido"}")
                }
            }
        }
    }

    /** Enters "manage as account" mode for [account] (impersonated ACCOUNT_ADMIN). The caller then
     *  navigates into the scoped workspace (Dashboard). */
    fun onEnterAccount(account: Account) {
        currentAccountHolder.enterImpersonation(account)
    }

    fun dismissMessage() = _formState.update { it.copy(error = null, successMessage = null) }
}
