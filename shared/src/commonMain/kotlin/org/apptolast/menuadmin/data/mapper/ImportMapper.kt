package org.apptolast.menuadmin.data.mapper

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
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
    val id: String,
    val type: String,
)

data class ImportResult(
    val ingredients: List<Ingredient>,
    val recipes: List<Recipe>,
)

object ImportMapper {
    fun mapAll(
        dto: ImportDataDto,
        restaurantId: String,
    ): ImportResult {
        val fallbackTimestamp = if (dto.timestamp.isNotBlank()) {
            Instant.parse(dto.timestamp)
        } else {
            kotlin.time.Clock.System.now()
        }

        val ingredients = dto.ingredients.map { mapIngredient(it, fallbackTimestamp) }
        val ingredientLookup = ingredients.associateBy { it.id }
        val recipeLookup = dto.recipes.associateBy { it.id.content }

        val recipes = dto.recipes.map {
            mapRecipe(it, ingredientLookup, recipeLookup, restaurantId, fallbackTimestamp)
        }

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
            id = dto.id.content,
            name = dto.name,
            brand = dto.brand,
            allergens = allergens,
            createdAt = fallbackTimestamp,
            updatedAt = fallbackTimestamp,
        )
    }

    fun mapRecipe(
        dto: ImportRecipeDto,
        ingredientLookup: Map<String, Ingredient>,
        recipeLookup: Map<String, ImportRecipeDto>,
        restaurantId: String,
        fallbackTimestamp: Instant,
    ): Recipe {
        // The domain model has no sub-recipes, so a `type == "recipe"` reference is flattened into its
        // own ingredients (recursively). This preserves the allergens the sub-recipe contributes —
        // dropping the link would silently under-report allergens on the parent dish.
        val ingredientIds = linkedSetOf<String>()
        collectIngredientIds(dto, recipeLookup, ingredientIds, mutableSetOf())

        val recipeIngredients = ingredientIds.map { id ->
            RecipeIngredient(
                ingredientId = id,
                ingredientName = ingredientLookup[id]?.name ?: "Ingrediente $id",
                quantity = 0.0,
                unit = "",
            )
        }

        return Recipe(
            id = dto.id.content,
            restaurantId = restaurantId,
            name = dto.name,
            category = dto.category,
            ingredients = recipeIngredients,
            ingredientCount = recipeIngredients.size,
            isActive = dto.active,
            createdAt = fallbackTimestamp,
            updatedAt = fallbackTimestamp,
        )
    }

    /** Walks [dto]'s references, following `type == "recipe"` refs into their ingredients. */
    private fun collectIngredientIds(
        dto: ImportRecipeDto,
        recipeLookup: Map<String, ImportRecipeDto>,
        out: LinkedHashSet<String>,
        visited: MutableSet<String>,
    ) {
        if (!visited.add(dto.id.content)) return // cycle guard
        parseIngredientIds(dto.ingredientIds).forEach { ref ->
            if (ref.type == "recipe") {
                recipeLookup[ref.id]?.let { collectIngredientIds(it, recipeLookup, out, visited) }
            } else {
                out.add(ref.id)
            }
        }
    }

    fun parseIngredientIds(raw: List<JsonElement>): List<ParsedIngredientRef> {
        return raw.mapNotNull { element ->
            when (element) {
                is JsonPrimitive -> ParsedIngredientRef(id = element.content, type = "ingredient")

                is JsonObject -> {
                    val obj = element.jsonObject
                    val id = obj["id"]?.jsonPrimitive?.content ?: return@mapNotNull null
                    val type = obj["type"]?.jsonPrimitive?.content ?: "ingredient"
                    ParsedIngredientRef(id = id, type = type)
                }

                else -> null
            }
        }
    }
}
