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
            id = 1770378899314,
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
    fun mapIngredient_ignoresUnknownAllergens() {
        val dto = ImportIngredientDto(
            id = 123,
            name = "Test",
            contains = listOf("gluten", "unknown_allergen", "eggs"),
        )

        val ingredient = ImportMapper.mapIngredient(dto, now)

        assertEquals(setOf(AllergenType.GLUTEN, AllergenType.EGGS), ingredient.allergenTypes)
    }

    @Test
    fun mapIngredient_handlesSpecialJsonKeys() {
        val dto = ImportIngredientDto(
            id = 1,
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
        assertEquals(ParsedIngredientRef(100L, "ingredient"), refs[0])
        assertEquals(ParsedIngredientRef(200L, "ingredient"), refs[1])
    }

    @Test
    fun parseIngredientIds_handlesMixedFormat() {
        val elements = listOf(
            JsonPrimitive(100L),
            buildJsonObject {
                put("id", 200L)
                put("type", "recipe")
            },
            buildJsonObject {
                put("id", 300L)
                put("type", "ingredient")
            },
        )

        val refs = ImportMapper.parseIngredientIds(elements)

        assertEquals(3, refs.size)
        assertEquals(ParsedIngredientRef(100L, "ingredient"), refs[0])
        assertEquals(ParsedIngredientRef(200L, "recipe"), refs[1])
        assertEquals(ParsedIngredientRef(300L, "ingredient"), refs[2])
    }

    @Test
    fun mapRecipe_separatesIngredientsAndSubRecipes() {
        val dto = ImportRecipeDto(
            id = 999,
            name = "Test Recipe",
            ingredientIds = listOf(
                JsonPrimitive(100L),
                buildJsonObject {
                    put("id", 200L)
                    put("type", "recipe")
                },
            ),
            active = false,
        )

        val ingredientLookup = mapOf(
            "100" to ImportMapper.mapIngredient(
                ImportIngredientDto(100, "Ingredient A", listOf("gluten")),
                now,
            ),
        )

        val recipe = ImportMapper.mapRecipe(dto, ingredientLookup, now)

        assertEquals("999", recipe.id)
        assertEquals("Test Recipe", recipe.name)
        assertEquals(false, recipe.isActive)
        assertEquals(1, recipe.ingredients.size)
        assertEquals("100", recipe.ingredients[0].ingredientId)
        assertEquals("Ingredient A", recipe.ingredients[0].ingredientName)
    }

    @Test
    fun mapAll_processesFullImportData() {
        val dto = ImportDataDto(
            ingredients = listOf(
                ImportIngredientDto(1, "Harina", listOf("gluten")),
                ImportIngredientDto(2, "Leche", listOf("milk")),
            ),
            recipes = listOf(
                ImportRecipeDto(
                    id = 10,
                    name = "Croquetas",
                    ingredientIds = listOf(JsonPrimitive(1L), JsonPrimitive(2L)),
                    active = true,
                ),
            ),
            timestamp = "2026-02-24T10:00:00Z",
        )

        val result = ImportMapper.mapAll(dto)

        assertEquals(2, result.ingredients.size)
        assertEquals(1, result.recipes.size)
        assertEquals("Croquetas", result.recipes[0].name)
        assertEquals(2, result.recipes[0].ingredients.size)
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
