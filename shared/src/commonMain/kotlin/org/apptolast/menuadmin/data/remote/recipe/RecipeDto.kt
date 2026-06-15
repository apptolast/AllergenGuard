package org.apptolast.menuadmin.data.remote.recipe

import kotlinx.serialization.Serializable

// --- Responses ---

@Serializable
data class RecipeSummaryResponseDto(
    val id: String,
    val name: String,
    val category: String? = null,
    val price: Double? = null,
    val active: Boolean = true,
    val ingredientCount: Int = 0,
    val allergenCount: Int = 0,
)

@Serializable
data class RecipeResponseDto(
    val id: String,
    val restaurantId: String,
    val name: String,
    val description: String? = null,
    val category: String? = null,
    val price: Double? = null,
    val active: Boolean = true,
    val ingredients: List<RecipeIngredientResponseDto> = emptyList(),
    val computedAllergens: List<ComputedAllergenResponseDto> = emptyList(),
    val createdAt: String? = null,
    val updatedAt: String? = null,
)

@Serializable
data class RecipeIngredientResponseDto(
    val ingredientId: String,
    val ingredientName: String = "",
    val quantity: Double? = null,
    val unit: String? = null,
)

@Serializable
data class ComputedAllergenResponseDto(
    val allergenId: Int,
    val allergenCode: String,
    val allergenName: String,
    val containmentLevel: String,
)

// --- Requests ---

@Serializable
data class CreateRecipeRequestDto(
    val restaurantId: String,
    val name: String,
    val description: String? = null,
    val category: String? = null,
    val price: Double? = null,
    val ingredients: List<RecipeIngredientRequestDto> = emptyList(),
)

@Serializable
data class UpdateRecipeRequestDto(
    val name: String? = null,
    val description: String? = null,
    val category: String? = null,
    val price: Double? = null,
    val active: Boolean? = null,
    val ingredients: List<RecipeIngredientRequestDto>? = null,
)

@Serializable
data class RecipeIngredientRequestDto(
    val ingredientId: String,
    val quantity: Double? = null,
    val unit: String? = null,
)
