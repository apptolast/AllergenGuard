package com.apptolast.menufrontend.features.profile.data

import com.apptolast.menufrontend.domain.model.Allergen
import com.apptolast.menufrontend.domain.model.User

data class ProfileState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)

sealed interface ProfileAction {
    data class ToggleAllergen(val allergen: Allergen) : ProfileAction
    data object NotificationsClicked : ProfileAction
    data object LanguageClicked : ProfileAction
    data object FavoriteRestaurantsClicked : ProfileAction
    data object HelpClicked : ProfileAction
    data object LogoutClicked : ProfileAction
}
