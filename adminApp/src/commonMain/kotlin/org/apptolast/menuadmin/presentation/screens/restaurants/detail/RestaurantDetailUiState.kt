package org.apptolast.menuadmin.presentation.screens.restaurants.detail

import org.apptolast.menuadmin.domain.model.Restaurant

data class RestaurantDetailUiState(
    val isLoading: Boolean = true,
    val restaurant: Restaurant? = null,
    val recipesCount: Int = 0,
    val menusCount: Int = 0,
    val publishedMenusCount: Int = 0,
    val isEditing: Boolean = false,
    val editName: String = "",
    val editDescription: String = "",
    val editAddress: String = "",
    val editPhone: String = "",
    val isSaving: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
)
