package org.apptolast.menuadmin.domain.model

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class Recipe(
    val id: String = "",
    val restaurantId: String = "",
    val name: String = "",
    val description: String = "",
    val category: String = "",
    val imageUrl: String? = null,
    val price: Double = 0.0,
    val isActive: Boolean = true,
    val ingredients: List<RecipeIngredient> = emptyList(),
    val computedAllergens: Set<AllergenType> = emptySet(),
    val ingredientCount: Int = 0,
    val allergenCount: Int = 0,
    // True when this recipe is used as a sub-elaboration (component) of another recipe (Spec 003).
    val isSubRecipe: Boolean = false,
    val createdAt: Instant = Instant.DISTANT_PAST,
    val updatedAt: Instant = Instant.DISTANT_PAST,
)

/** Kind of recipe component: a plain catalog ingredient, or a sub-recipe (sub-elaboration). */
enum class RecipeComponentType { INGREDIENT, SUB_RECIPE }

@Serializable
data class RecipeIngredient(
    val ingredientId: String,
    val ingredientName: String = "",
    val quantity: Double = 0.0,
    val unit: String = "",
    // INGREDIENT = catalog ingredient; SUB_RECIPE = reference to another Recipe (Spec 003).
    val type: RecipeComponentType = RecipeComponentType.INGREDIENT,
)
