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
    val price: Double = 0.0,
    val isActive: Boolean = true,
    val ingredients: List<RecipeIngredient> = emptyList(),
    val computedAllergens: Set<AllergenType> = emptySet(),
    val ingredientCount: Int = 0,
    val allergenCount: Int = 0,
    val createdAt: Instant = Instant.DISTANT_PAST,
    val updatedAt: Instant = Instant.DISTANT_PAST,
)

@Serializable
data class RecipeIngredient(
    val ingredientId: String,
    val ingredientName: String = "",
    val quantity: Double = 0.0,
    val unit: String = "",
)
