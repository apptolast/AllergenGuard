package org.apptolast.menuadmin.domain.util

import org.apptolast.menuadmin.domain.model.AllergenType
import org.apptolast.menuadmin.domain.model.Dish
import org.apptolast.menuadmin.domain.model.Ingredient
import org.apptolast.menuadmin.domain.model.Recipe

object AllergenFilter {
    fun filterSafeDishes(
        dishes: List<Dish>,
        userAllergens: Set<AllergenType>,
    ): Pair<List<Dish>, List<Dish>> {
        if (userAllergens.isEmpty()) return dishes to emptyList()
        return dishes.partition { dish ->
            dish.allergens.none { it in userAllergens }
        }
    }

    fun aggregateAllergens(
        recipe: Recipe,
        ingredientLookup: Map<String, Ingredient>,
    ): Set<AllergenType> =
        aggregateAllergensFromIds(
            ingredientIds = recipe.ingredients.map { it.ingredientId },
            ingredientLookup = ingredientLookup,
        )

    fun aggregateAllergensFromIds(
        ingredientIds: List<String>,
        ingredientLookup: Map<String, Ingredient>,
    ): Set<AllergenType> =
        ingredientIds.flatMapTo(mutableSetOf()) { id ->
            ingredientLookup[id]?.allergenTypes.orEmpty()
        }
}
