package com.apptolast.menufrontend.features.profile.data

import com.apptolast.menufrontend.domain.model.Allergen
import com.apptolast.menufrontend.domain.model.User

data class ProfileState(
    val user: User? = null,
    /** Allergens saved in the user's profile (read-only display on the screen). */
    val allergens: Set<Allergen> = emptySet(),
    /** Whether the allergen edit bottom sheet is open. */
    val isEditingAllergens: Boolean = false,
    /** Working copy of the selection while the edit sheet is open. */
    val sheetSelection: Set<Allergen> = emptySet(),
    val isSaving: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    /** Logout confirmation dialog visibility. */
    val showLogoutDialog: Boolean = false,
    /** Delete-account confirmation dialog visibility. */
    val showDeleteDialog: Boolean = false,
    /** Account deletion in progress (blocks the dialog buttons). */
    val isDeleting: Boolean = false,
)

sealed interface ProfileAction {
    data object EditAllergiesClicked : ProfileAction
    data class ToggleSheetAllergen(val allergen: Allergen) : ProfileAction
    data object SaveAllergies : ProfileAction
    data object DismissAllergenSheet : ProfileAction

    // Logout (reversible): tap opens a confirmation dialog, confirm signs out.
    data object LogoutClicked : ProfileAction
    data object ConfirmLogout : ProfileAction

    // Delete account (irreversible): tap opens a destructive confirmation dialog, confirm deletes.
    data object DeleteAccountClicked : ProfileAction
    data object ConfirmDeleteAccount : ProfileAction

    /** Dismiss whichever confirmation dialog is open. */
    data object DismissDialogs : ProfileAction
}
