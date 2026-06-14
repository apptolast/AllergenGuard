package com.apptolast.menufrontend.domain.model

data class Dish(
    val id: String,
    val restaurantId: String,
    val name: String,
    val description: String,
    val price: Double,
    val imageUrl: String? = null,
    val ingredients: List<String>,
    val allergens: Set<Allergen>,
)
