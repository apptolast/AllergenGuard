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
)

sealed interface ProfileAction {
    data object EditAllergiesClicked : ProfileAction
    data class ToggleSheetAllergen(val allergen: Allergen) : ProfileAction
    data object SaveAllergies : ProfileAction
    data object DismissAllergenSheet : ProfileAction
    data object NotificationsClicked : ProfileAction
    data object LanguageClicked : ProfileAction
    data object FavoriteRestaurantsClicked : ProfileAction
    data object HelpClicked : ProfileAction
    data object LogoutClicked : ProfileAction
}
