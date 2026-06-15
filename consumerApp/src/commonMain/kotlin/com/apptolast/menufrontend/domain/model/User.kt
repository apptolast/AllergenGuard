package com.apptolast.menufrontend.domain.model

data class User(
    val id: String,
    val name: String,
    val email: String,
    val allergens: Set<Allergen> = emptySet(),
    val favoriteRestaurantIds: Set<String> = emptySet(),
)
