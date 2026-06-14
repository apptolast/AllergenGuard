package com.apptolast.menufrontend.features.favorites.data

import com.apptolast.menufrontend.domain.model.Restaurant

data class FavoritesState(
    val restaurants: List<Restaurant> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

sealed interface FavoritesAction {
    data class RestaurantClicked(val restaurantId: String) : FavoritesAction
    data class RemoveFavorite(val restaurantId: String) : FavoritesAction
}
