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
import org.apptolast.menuadmin.domain.model.RecipeComponentType
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

        // Mark recipes that are referenced as a sub-recipe (component) by any other recipe (Spec 003).
        val subRecipeIds = recipes
            .flatMap { it.ingredients }
            .filter { it.type == RecipeComponentType.SUB_RECIPE }
            .mapTo(mutableSetOf()) { it.ingredientId }
        val marked = recipes.map { it.copy(isSubRecipe = it.id in subRecipeIds) }

        return ImportResult(ingredients = ingredients, recipes = marked)
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
        // Sub-recipes are first-class (Spec 003): a `type == "recipe"` ref — or a bare id that resolves
        // to a sibling recipe — becomes a SUB_RECIPE component that keeps the sub-recipe's real name,
        // instead of being flattened into anonymous "Ingrediente <id>" entries. Allergens are aggregated
        // recursively later (RecipeAllergenAggregator), so nothing is under-reported.
        val seen = linkedSetOf<String>()
        val components = parseIngredientIds(dto.ingredientIds).mapNotNull { ref ->
            if (!seen.add(ref.id)) return@mapNotNull null // dedup exact duplicate refs
            resolveComponent(ref, ingredientLookup, recipeLookup)
        }

        return Recipe(
            id = dto.id.content,
            restaurantId = restaurantId,
            name = dto.name,
            category = dto.category,
            ingredients = components,
            ingredientCount = components.size,
            isActive = dto.active,
            createdAt = fallbackTimestamp,
            updatedAt = fallbackTimestamp,
        )
    }

    /**
     * Resolves a single reference to a recipe component. It is a sub-recipe when the ref is explicitly
     * `type == "recipe"`, or when a bare/untyped id is not a known ingredient but IS a known recipe
     * (the "Ingrediente N" scenario B). The placeholder `Ingrediente <id>` is used only when the id is
     * unknown in both catalogs.
     */
    private fun resolveComponent(
        ref: ParsedIngredientRef,
        ingredientLookup: Map<String, Ingredient>,
        recipeLookup: Map<String, ImportRecipeDto>,
    ): RecipeIngredient {
        val isSubRecipe = ref.type == "recipe" ||
            (ref.id !in ingredientLookup && ref.id in recipeLookup)
        return if (isSubRecipe) {
            RecipeIngredient(
                ingredientId = ref.id,
                ingredientName = recipeLookup[ref.id]?.name
                    ?: ingredientLookup[ref.id]?.name
                    ?: "Ingrediente ${ref.id}",
                type = RecipeComponentType.SUB_RECIPE,
            )
        } else {
            RecipeIngredient(
                ingredientId = ref.id,
                ingredientName = ingredientLookup[ref.id]?.name
                    ?: recipeLookup[ref.id]?.name
                    ?: "Ingrediente ${ref.id}",
                type = RecipeComponentType.INGREDIENT,
            )
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
