package com.apptolast.menufrontend.domain.model

data class Dish(
    val id: String,
    val restaurantId: String,
    val name: String,
    val description: String,
    val category: String = "",
    val price: Double,
    val imageUrl: String? = null,
    val ingredients: List<String>,
    val allergens: Set<Allergen>,
    // True when this recipe is a sub-elaboration; such recipes must never surface as a menu dish (Spec 005).
    val isSubRecipe: Boolean = false,
    // True when the dish's stored allergens contained a code the consumer doesn't recognize. A dish with an
    // unknown allergen must NOT be presented as safe (fail-closed for food safety).
    val hasUnknownAllergen: Boolean = false,
) {
    /**
     * Safe for a user only when it contains none of their allergens AND all its allergen codes were
     * recognized (fail-closed: an unknown allergen never reads as safe).
     */
    fun isSafeFor(userAllergens: Set<Allergen>): Boolean =
        !hasUnknownAllergen && allergens.none { it in userAllergens }
}
