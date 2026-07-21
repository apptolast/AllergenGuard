package org.apptolast.menuadmin.domain.util

import org.apptolast.menuadmin.domain.model.ContainmentLevel
import org.apptolast.menuadmin.domain.model.Ingredient
import org.apptolast.menuadmin.domain.model.Recipe
import org.apptolast.menuadmin.domain.model.RecipeComponentType

/**
 * Pure allergen aggregation for a recipe (Spec 003). Walks the recipe's components: catalog
 * ingredients contribute their own allergens; sub-recipe components are expanded recursively against
 * [recipesById], with a cycle guard so mutually-referencing recipes terminate. FREE_OF is ignored and
 * the strongest containment level (lowest ordinal, i.e. CONTAINS) wins per allergen code.
 *
 * Extracted out of the Firestore repository so it can be unit-tested without a backend, and so
 * sub-recipe allergens are never silently under-reported.
 */
object RecipeAllergenAggregator {
    fun computeAllergens(
        recipe: Recipe,
        ingredientsById: Map<String, Ingredient>,
        recipesById: Map<String, Recipe>,
    ): Map<String, ContainmentLevel> {
        val strongest = mutableMapOf<String, ContainmentLevel>()
        val visited = mutableSetOf<String>()

        fun visit(current: Recipe) {
            if (current.id.isNotEmpty() && !visited.add(current.id)) return // cycle guard
            current.ingredients.forEach { component ->
                when (component.type) {
                    RecipeComponentType.SUB_RECIPE ->
                        recipesById[component.ingredientId]?.let { visit(it) }

                    RecipeComponentType.INGREDIENT ->
                        ingredientsById[component.ingredientId]?.allergens.orEmpty().forEach { allergen ->
                            if (allergen.containmentLevel == ContainmentLevel.FREE_OF) return@forEach
                            val cur = strongest[allergen.allergenCode]
                            if (cur == null || allergen.containmentLevel.ordinal < cur.ordinal) {
                                strongest[allergen.allergenCode] = allergen.containmentLevel
                            }
                        }
                }
            }
        }

        visit(recipe)
        return strongest
    }
}
