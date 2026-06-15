package org.apptolast.menuadmin.data.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

@Serializable
data class ImportDataDto(
    val ingredients: List<ImportIngredientDto> = emptyList(),
    val recipes: List<ImportRecipeDto> = emptyList(),
    val timestamp: String = "",
)

/**
 * `id` is a [JsonPrimitive] (read via `.content`) because legacy backups serialize ids as quoted
 * strings (`"1778149024105"`) while the app's own exports use bare numbers — both decode here without
 * a lenient parser.
 */
@Serializable
data class ImportIngredientDto(
    val id: JsonPrimitive,
    val name: String,
    val brand: String = "",
    val contains: List<String> = emptyList(),
)

@Serializable
data class ImportRecipeDto(
    val id: JsonPrimitive,
    val name: String,
    val category: String = "",
    val ingredientIds: List<JsonElement> = emptyList(),
    val active: Boolean = true,
)
