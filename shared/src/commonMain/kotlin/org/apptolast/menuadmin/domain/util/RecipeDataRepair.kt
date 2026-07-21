package org.apptolast.menuadmin.domain.util

import org.apptolast.menuadmin.domain.model.AllergenType
import org.apptolast.menuadmin.domain.model.Ingredient
import org.apptolast.menuadmin.domain.model.Recipe
import org.apptolast.menuadmin.domain.model.RecipeComponentType

/**
 * Pure repair for recipes already imported before Spec 003 (e.g. "Ingrediente <id>" placeholders where
 * the id is actually a sibling recipe). For each component whose id resolves to another recipe in the
 * set, sets its type to SUB_RECIPE and its name to the real recipe name; marks referenced recipes as
 * `isSubRecipe`; and recomputes `computedAllergens` via [RecipeAllergenAggregator]. No Firestore access:
 * the caller reads, applies this transform, and writes back.
 */
object RecipeDataRepair {
    fun repairAll(
        recipes: List<Recipe>,
        ingredientsById: Map<String, Ingredient>,
    ): List<Recipe> {
        val recipeIds = recipes.mapTo(mutableSetOf()) { it.id }

        // 1. Structural fix: a component whose id is another recipe is a sub-recipe reference.
        val structural = recipes.map { recipe ->
            val fixedComponents = recipe.ingredients.map { component ->
                val referenced = recipesById(recipes)[component.ingredientId]
                if (referenced != null && referenced.id != recipe.id && component.ingredientId in recipeIds) {
                    component.copy(
                        type = RecipeComponentType.SUB_RECIPE,
                        ingredientName = referenced.name,
                    )
                } else {
                    component
                }
            }
            recipe.copy(ingredients = fixedComponents)
        }

        // 2. Which recipes are referenced as sub-recipes (mark isSubRecipe).
        val subRecipeIds = structural
            .flatMap { it.ingredients }
            .filter { it.type == RecipeComponentType.SUB_RECIPE }
            .mapTo(mutableSetOf()) { it.ingredientId }
        val byIdFixed = structural.associateBy { it.id }

        // 3. Recompute allergens over the corrected graph.
        return structural.map { recipe ->
            val allergens = RecipeAllergenAggregator
                .computeAllergens(recipe, ingredientsById, byIdFixed)
                .keys
                .mapNotNull { AllergenType.fromApiCode(it) }
                .toSet()
            recipe.copy(
                isSubRecipe = recipe.id in subRecipeIds,
                computedAllergens = allergens,
                allergenCount = allergens.size,
            )
        }
    }

    private fun recipesById(recipes: List<Recipe>): Map<String, Recipe> = recipes.associateBy { it.id }
}
