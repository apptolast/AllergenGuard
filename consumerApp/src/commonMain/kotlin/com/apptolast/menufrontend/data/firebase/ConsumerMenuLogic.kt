package com.apptolast.menufrontend.data.firebase

import com.apptolast.menufrontend.domain.model.Allergen
import com.apptolast.menufrontend.domain.model.Dish

// Spec 005 — pure consumer menu/allergen logic, unit-tested without a backend.

/** Parsed allergens plus whether any code was unrecognized (fail-closed signal). */
data class ParsedAllergens(
    val allergens: Set<Allergen>,
    val hasUnknown: Boolean,
)

/** Maps a list of stored allergen codes to the consumer enum, flagging any unrecognized code. */
fun parseComputedAllergens(codes: List<String?>): ParsedAllergens {
    val allergens = mutableSetOf<Allergen>()
    var hasUnknown = false
    codes.forEach { code ->
        if (code == null) return@forEach
        val allergen = allergenFromCode(code)
        if (allergen == null) hasUnknown = true else allergens.add(allergen)
    }
    return ParsedAllergens(allergens, hasUnknown)
}

/** Dishes to show on the menu: those in the published menu's recipeIds and NOT sub-recipes. */
fun visibleMenuDishes(
    dishes: List<Dish>,
    recipeIds: Set<String>,
): List<Dish> = dishes.filter { it.id in recipeIds && !it.isSubRecipe }
