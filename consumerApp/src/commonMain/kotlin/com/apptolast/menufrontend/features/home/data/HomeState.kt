package com.apptolast.menufrontend.features.home.data

import com.apptolast.menufrontend.domain.model.Restaurant

data class HomeState(
    val restaurants: List<Restaurant> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
)

sealed interface HomeAction {
    data class SearchQueryChanged(val query: String) : HomeAction
    data class RestaurantClicked(val restaurantId: String) : HomeAction
}
