package org.apptolast.menuadmin.data.remote.dish

import kotlinx.serialization.Serializable

@Serializable
data class DishResponseDto(
    val id: String,
    val name: String,
    val description: String = "",
    val price: Double = 0.0,
    val sectionId: String? = null,
    val imageUrl: String? = null,
    val safetyLevel: String? = null,
    val matchedAllergens: List<String> = emptyList(),
    val allergens: List<DishAllergenResponseDto> = emptyList(),
    val updatedAt: String? = null,
    val available: Boolean = true,
)

@Serializable
data class DishAllergenResponseDto(
    val allergenId: Int,
    val code: String,
    val name: String = "",
    val containmentLevel: String = "CONTAINS",
    val notes: String = "",
)

@Serializable
data class DishRequestDto(
    val name: String,
    val sectionId: String,
    val description: String? = null,
    val imageUrl: String? = null,
    val available: Boolean? = null,
    val displayOrder: Int? = null,
    val allergens: List<DishAllergenRequestDto>? = null,
)

@Serializable
data class DishAllergenRequestDto(
    val allergenCode: String,
    val containmentLevel: String? = null,
    val notes: String? = null,
)
