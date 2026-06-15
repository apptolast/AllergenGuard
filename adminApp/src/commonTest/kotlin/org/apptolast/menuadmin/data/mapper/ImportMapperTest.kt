package org.apptolast.menuadmin.data.mapper

import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.apptolast.menuadmin.data.dto.ImportDataDto
import org.apptolast.menuadmin.data.dto.ImportIngredientDto
import org.apptolast.menuadmin.data.dto.ImportRecipeDto
import org.apptolast.menuadmin.domain.model.AllergenType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock

class ImportMapperTest {
    private val now = Clock.System.now()

    @Test
    fun mapIngredient_mapsAllergenJsonKeysCorrectly() {
        val dto = ImportIngredientDto(
            id = JsonPrimitive("1770378899314"),
            name = "Harina de trigo",
            contains = listOf("gluten", "milk", "nuts"),
        )

        val ingredient = ImportMapper.mapIngredient(dto, now)

        assertEquals("1770378899314", ingredient.id)
        assertEquals("Harina de trigo", ingredient.name)
        assertEquals(
            setOf(AllergenType.GLUTEN, AllergenType.DAIRY, AllergenType.TREE_NUTS),
            ingredient.allergenTypes,
        )
    }

    @Test
    fun mapIngredient_acceptsNumericIds() {
        val dto = ImportIngredientDto(id = JsonPrimitive(123L), name = "Test", contains = listOf("gluten"))

        assertEquals("123", ImportMapper.mapIngredient(dto, now).id)
    }

    @Test
    fun mapIngredient_ignoresUnknownAllergens() {
        val dto = ImportIngredientDto(
            id = JsonPrimitive("123"),
            name = "Test",
            contains = listOf("gluten", "unknown_allergen", "eggs"),
        )

        val ingredient = ImportMapper.mapIngredient(dto, now)

        assertEquals(setOf(AllergenType.GLUTEN, AllergenType.EGGS), ingredient.allergenTypes)
    }

    @Test
    fun mapIngredient_handlesSpecialJsonKeys() {
        val dto = ImportIngredientDto(
            id = JsonPrimitive("1"),
            name = "Test",
            contains = listOf("sulphites", "molluscs"),
        )

        val ingredient = ImportMapper.mapIngredient(dto, now)

        assertEquals(setOf(AllergenType.SULFITES, AllergenType.MOLLUSKS), ingredient.allergenTypes)
    }

    @Test
    fun parseIngredientIds_handlesPlainNumbers() {
        val elements = listOf(JsonPrimitive(100L), JsonPrimitive(200L))

        val refs = ImportMapper.parseIngredientIds(elements)

        assertEquals(2, refs.size)
        assertEquals(ParsedIngredientRef("100", "ingredient"), refs[0])
        assertEquals(ParsedIngredientRef("200", "ingredient"), refs[1])
    }

    @Test
    fun parseIngredientIds_handlesQuotedStringsAndTypedObjects() {
        val elements = listOf(
            JsonPrimitive("100"),
            buildJsonObject {
                put("id", "200")
                put("type", "recipe")
            },
            buildJsonObject {
                put("id", "300")
                put("type", "ingredient")
            },
        )

        val refs = ImportMapper.parseIngredientIds(elements)

        assertEquals(3, refs.size)
        assertEquals(ParsedIngredientRef("100", "ingredient"), refs[0])
        assertEquals(ParsedIngredientRef("200", "recipe"), refs[1])
        assertEquals(ParsedIngredientRef("300", "ingredient"), refs[2])
    }

    @Test
    fun mapRecipe_flattensSubRecipeIngredients() {
        // Parent uses ingredient 100 directly and sub-recipe 900; the sub-recipe owns ingredient 200.
        val parent = ImportRecipeDto(
            id = JsonPrimitive("10"),
            name = "Tosta con Jamón",
            ingredientIds = listOf(
                JsonPrimitive("100"),
                buildJsonObject {
                    put("id", "900")
                    put("type", "recipe")
                },
            ),
        )
        val sub = ImportRecipeDto(
            id = JsonPrimitive("900"),
            name = "Salmorejo",
            ingredientIds = listOf(JsonPrimitive("200")),
        )
        val ingredientLookup = mapOf(
            "100" to ImportMapper.mapIngredient(ImportIngredientDto(JsonPrimitive("100"), "Jamón"), now),
            "200" to ImportMapper.mapIngredient(
                ImportIngredientDto(JsonPrimitive("200"), "Tomate", contains = listOf("sulphites")),
                now,
            ),
        )
        val recipeLookup = mapOf("10" to parent, "900" to sub)

        val recipe = ImportMapper.mapRecipe(parent, ingredientLookup, recipeLookup, "rest-1", now)

        assertEquals("rest-1", recipe.restaurantId)
        assertEquals(listOf("100", "200"), recipe.ingredients.map { it.ingredientId })
        assertEquals("Tomate", recipe.ingredients[1].ingredientName)
    }

    @Test
    fun mapRecipe_deduplicatesIngredientsSharedWithSubRecipe() {
        val parent = ImportRecipeDto(
            id = JsonPrimitive("10"),
            name = "Plato",
            ingredientIds = listOf(
                JsonPrimitive("100"),
                buildJsonObject {
                    put("id", "900")
                    put("type", "recipe")
                },
            ),
        )
        val sub = ImportRecipeDto(
            id = JsonPrimitive("900"),
            name = "Sub",
            ingredientIds = listOf(JsonPrimitive("100")),
        )
        val recipeLookup = mapOf("10" to parent, "900" to sub)

        val recipe = ImportMapper.mapRecipe(parent, emptyMap(), recipeLookup, "rest-1", now)

        assertEquals(listOf("100"), recipe.ingredients.map { it.ingredientId })
    }

    @Test
    fun mapAll_processesFullImportDataAndScopesRestaurant() {
        val dto = ImportDataDto(
            ingredients = listOf(
                ImportIngredientDto(JsonPrimitive("1"), "Harina", contains = listOf("gluten")),
                ImportIngredientDto(JsonPrimitive("2"), "Leche", contains = listOf("milk")),
            ),
            recipes = listOf(
                ImportRecipeDto(
                    id = JsonPrimitive("10"),
                    name = "Croquetas",
                    ingredientIds = listOf(JsonPrimitive("1"), JsonPrimitive("2")),
                    active = true,
                ),
            ),
            timestamp = "2026-02-24T10:00:00Z",
        )

        val result = ImportMapper.mapAll(dto, "rest-9")

        assertEquals(2, result.ingredients.size)
        assertEquals(1, result.recipes.size)
        assertEquals("Croquetas", result.recipes[0].name)
        assertEquals(2, result.recipes[0].ingredients.size)
        assertEquals("rest-9", result.recipes[0].restaurantId)
        assertTrue(result.recipes[0].isActive)
    }

    @Test
    fun allergenType_fromJsonKey_allMappingsWork() {
        val mappings = mapOf(
            "gluten" to AllergenType.GLUTEN,
            "crustaceans" to AllergenType.CRUSTACEANS,
            "eggs" to AllergenType.EGGS,
            "fish" to AllergenType.FISH,
            "peanuts" to AllergenType.PEANUTS,
            "soy" to AllergenType.SOY,
            "milk" to AllergenType.DAIRY,
            "nuts" to AllergenType.TREE_NUTS,
            "celery" to AllergenType.CELERY,
            "mustard" to AllergenType.MUSTARD,
            "sesame" to AllergenType.SESAME,
            "sulphites" to AllergenType.SULFITES,
            "lupins" to AllergenType.LUPINS,
            "molluscs" to AllergenType.MOLLUSKS,
        )

        mappings.forEach { (key, expected) ->
            assertEquals(expected, AllergenType.fromJsonKey(key), "Failed for key: $key")
        }
    }
}
