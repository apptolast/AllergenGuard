package org.apptolast.menuadmin.data.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ImportDataDto(
    val ingredients: List<ImportIngredientDto> = emptyList(),
    val recipes: List<ImportRecipeDto> = emptyList(),
    val timestamp: String = "",
)

@Serializable
data class ImportIngredientDto(
    val id: Long,
    val name: String,
    val contains: List<String> = emptyList(),
)

@Serializable
data class ImportRecipeDto(
    val id: Long,
    val name: String,
    val ingredientIds: List<JsonElement> = emptyList(),
    val active: Boolean = true,
)
