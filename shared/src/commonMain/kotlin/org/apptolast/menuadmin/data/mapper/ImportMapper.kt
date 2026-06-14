package org.apptolast.menuadmin.data.mapper

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import org.apptolast.menuadmin.data.dto.ImportDataDto
import org.apptolast.menuadmin.data.dto.ImportIngredientDto
import org.apptolast.menuadmin.data.dto.ImportRecipeDto
import org.apptolast.menuadmin.domain.model.AllergenType
import org.apptolast.menuadmin.domain.model.ContainmentLevel
import org.apptolast.menuadmin.domain.model.Ingredient
import org.apptolast.menuadmin.domain.model.IngredientAllergen
import org.apptolast.menuadmin.domain.model.Recipe
import org.apptolast.menuadmin.domain.model.RecipeIngredient
import kotlin.time.Instant

data class ParsedIngredientRef(
    val id: Long,
    val type: String,
)

data class ImportResult(
    val ingredients: List<Ingredient>,
    val recipes: List<Recipe>,
)

object ImportMapper {
    fun mapAll(dto: ImportDataDto): ImportResult {
        val fallbackTimestamp = if (dto.timestamp.isNotBlank()) {
            Instant.parse(dto.timestamp)
        } else {
            kotlin.time.Clock.System.now()
        }

        val ingredients = dto.ingredients.map { mapIngredient(it, fallbackTimestamp) }
        val ingredientLookup = ingredients.associateBy { it.id }

        val recipes = dto.recipes.map { mapRecipe(it, ingredientLookup, fallbackTimestamp) }

        return ImportResult(ingredients = ingredients, recipes = recipes)
    }

    fun mapIngredient(
        dto: ImportIngredientDto,
        fallbackTimestamp: Instant,
    ): Ingredient {
        val allergens = dto.contains
            .mapNotNull { AllergenType.fromJsonKey(it) }
            .map { type ->
                IngredientAllergen(
                    allergenCode = type.apiCode,
                    allergenName = type.nameEs,
                    containmentLevel = ContainmentLevel.CONTAINS,
                )
            }

        return Ingredient(
            id = dto.id.toString(),
            name = dto.name,
            allergens = allergens,
            createdAt = fallbackTimestamp,
            updatedAt = fallbackTimestamp,
        )
    }

    fun mapRecipe(
        dto: ImportRecipeDto,
        ingredientLookup: Map<String, Ingredient>,
        fallbackTimestamp: Instant,
    ): Recipe {
        val refs = parseIngredientIds(dto.ingredientIds)

        val recipeIngredients = refs
            .filter { it.type == "ingredient" }
            .map { ref ->
                val ingredientName = ingredientLookup[ref.id.toString()]?.name ?: "Ingrediente ${ref.id}"
                RecipeIngredient(
                    ingredientId = ref.id.toString(),
                    ingredientName = ingredientName,
                    quantity = 0.0,
                    unit = "",
                )
            }

        return Recipe(
            id = dto.id.toString(),
            name = dto.name,
            ingredients = recipeIngredients,
            ingredientCount = recipeIngredients.size,
            isActive = dto.active,
            createdAt = fallbackTimestamp,
            updatedAt = fallbackTimestamp,
        )
    }

    fun parseIngredientIds(raw: List<JsonElement>): List<ParsedIngredientRef> {
        return raw.mapNotNull { element ->
            when (element) {
                is JsonPrimitive -> {
                    val id = element.jsonPrimitive.long
                    ParsedIngredientRef(id = id, type = "ingredient")
                }

                is JsonObject -> {
                    val obj = element.jsonObject
                    val id = obj["id"]?.jsonPrimitive?.long ?: return@mapNotNull null
                    val type = obj["type"]?.jsonPrimitive?.content ?: "ingredient"
                    ParsedIngredientRef(id = id, type = type)
                }

                else -> null
            }
        }
    }
}
