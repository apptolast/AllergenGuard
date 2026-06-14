package org.apptolast.menuadmin.data.remote.ingredient

import kotlinx.serialization.Serializable

@Serializable
data class IngredientResponseDto(
    val id: String,
    val name: String,
    val description: String? = null,
    val brand: String? = null,
    val labelInfo: String? = null,
    val allergens: List<IngredientAllergenResponseDto> = emptyList(),
    val createdAt: String? = null,
    val updatedAt: String? = null,
)

@Serializable
data class IngredientAllergenResponseDto(
    val allergenId: Int,
    val allergenCode: String,
    val allergenName: String = "",
    val containmentLevel: String = "CONTAINS",
)

@Serializable
data class CreateIngredientRequestDto(
    val name: String,
    val description: String? = null,
    val brand: String? = null,
    val labelInfo: String? = null,
    val allergens: List<IngredientAllergenRequestDto> = emptyList(),
)

@Serializable
data class UpdateIngredientRequestDto(
    val name: String? = null,
    val description: String? = null,
    val brand: String? = null,
    val labelInfo: String? = null,
    val allergens: List<IngredientAllergenRequestDto>? = null,
)

@Serializable
data class IngredientAllergenRequestDto(
    val allergenCode: String,
    val containmentLevel: String,
)
