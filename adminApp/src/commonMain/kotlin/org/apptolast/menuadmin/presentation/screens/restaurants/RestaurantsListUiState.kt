package org.apptolast.menuadmin.presentation.screens.restaurants

import org.apptolast.menuadmin.domain.model.Restaurant

data class RestaurantsListUiState(
    val isLoading: Boolean = true,
    val restaurants: List<Restaurant> = emptyList(),
    val isFormVisible: Boolean = false,
    val editingRestaurant: Restaurant? = null,
    val formName: String = "",
    val formSlug: String = "",
    val formDescription: String = "",
    val formAddress: String = "",
    val formPhone: String = "",
    val isSaving: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
)
