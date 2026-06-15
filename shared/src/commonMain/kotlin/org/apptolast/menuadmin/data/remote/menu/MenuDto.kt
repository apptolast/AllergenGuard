package org.apptolast.menuadmin.data.remote.menu

import kotlinx.serialization.Serializable

@Serializable
data class MenuResponseDto(
    val id: String,
    val name: String,
    val description: String = "",
    val displayOrder: Int = 0,
    val published: Boolean = false,
    val archived: Boolean = false,
    val restaurantLogoUrl: String? = null,
    val companyLogoUrl: String? = null,
    val sections: List<SectionResponseDto> = emptyList(),
    val recipes: List<MenuRecipeResponseDto> = emptyList(),
    val updatedAt: String? = null,
)

@Serializable
data class MenuRecipeResponseDto(
    val id: String,
    val name: String,
)

@Serializable
data class SectionResponseDto(
    val id: String,
    val name: String,
    val displayOrder: Int = 0,
)

@Serializable
data class MenuRequestDto(
    val name: String,
    val description: String? = null,
    val displayOrder: Int? = null,
    val restaurantLogoUrl: String? = null,
    val companyLogoUrl: String? = null,
    val recipeIds: List<String>? = null,
)

@Serializable
data class SectionRequestDto(
    val name: String,
    val displayOrder: Int? = null,
)

@Serializable
data class MenuPublishRequestDto(
    val published: Boolean,
)

@Serializable
data class UpdateMenuRecipesRequestDto(
    val recipeIds: List<String>,
)
