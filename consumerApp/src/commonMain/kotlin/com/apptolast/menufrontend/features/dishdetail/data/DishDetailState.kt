package com.apptolast.menufrontend.features.dishdetail.data

import com.apptolast.menufrontend.domain.model.Allergen
import com.apptolast.menufrontend.domain.model.Dish

data class DishDetailState(
    val dish: Dish? = null,
    val restaurantName: String = "",
    val userAllergens: Set<Allergen> = emptySet(),
    val isLoading: Boolean = false,
    val error: String? = null,
) {
    val dangerousAllergens: Set<Allergen>
        get() = dish?.allergens?.intersect(userAllergens) ?: emptySet()

    val containsUserAllergens: Boolean
        get() = dangerousAllergens.isNotEmpty()
}
