package org.apptolast.menuadmin.data.mapper

import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.apptolast.menuadmin.data.dto.ImportDataDto
import org.apptolast.menuadmin.data.dto.ImportIngredientDto
import org.apptolast.menuadmin.data.dto.ImportRecipeDto
import org.apptolast.menuadmin.domain.model.AllergenType
import org.apptolast.menuadmin.domain.model.RecipeComponentType
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

    // AC-01: la sub-receta (type:"recipe") se conserva como componente SUB_RECIPE con su nombre real,
    // NO se aplana en sus ingredientes hoja ni se pierde su nombre.
    @Test
    fun mapRecipe_keepsSubRecipeAsComponentWithName() {
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
        )
        val recipeLookup = mapOf("10" to parent, "900" to sub)

        val recipe = ImportMapper.mapRecipe(parent, ingredientLookup, recipeLookup, "rest-1", now)

        assertEquals("rest-1", recipe.restaurantId)
        assertEquals(listOf("100", "900"), recipe.ingredients.map { it.ingredientId })
        assertEquals(RecipeComponentType.INGREDIENT, recipe.ingredients[0].type)
        val subComp = recipe.ingredients[1]
        assertEquals(RecipeComponentType.SUB_RECIPE, subComp.type)
        assertEquals("Salmorejo", subComp.ingredientName)
    }

    // AC-02: un ref a sub-receta SIN type:"recipe" (id pelado) que coincide con una receta hermana se
    // resuelve como SUB_RECIPE con su nombre real (nunca "Ingrediente <id>").
    @Test
    fun mapRecipe_resolvesBareIdRefToSubRecipe() {
        val parent = ImportRecipeDto(
            id = JsonPrimitive("10"),
            name = "Plato",
            ingredientIds = listOf(JsonPrimitive("100"), JsonPrimitive("900")),
        )
        val sub = ImportRecipeDto(id = JsonPrimitive("900"), name = "Mayonesa casera")
        val ingredientLookup = mapOf(
            "100" to ImportMapper.mapIngredient(ImportIngredientDto(JsonPrimitive("100"), "Patata"), now),
        )
        val recipeLookup = mapOf("10" to parent, "900" to sub)

        val recipe = ImportMapper.mapRecipe(parent, ingredientLookup, recipeLookup, "rest-1", now)

        val comp = recipe.ingredients.first { it.ingredientId == "900" }
        assertEquals(RecipeComponentType.SUB_RECIPE, comp.type)
        assertEquals("Mayonesa casera", comp.ingredientName)
    }

    // AC-03: el placeholder "Ingrediente <id>" solo aparece cuando el id no está NI en ingredientes NI
    // en recetas (único caso aceptable); si es una receta, se usa su nombre.
    @Test
    fun mapRecipe_placeholderNameOnlyWhenUnknownEverywhere() {
        val parent = ImportRecipeDto(
            id = JsonPrimitive("10"),
            name = "Plato",
            ingredientIds = listOf(JsonPrimitive("999")),
        )

        val recipe = ImportMapper.mapRecipe(parent, emptyMap(), mapOf("10" to parent), "rest-1", now)

        assertEquals("Ingrediente 999", recipe.ingredients[0].ingredientName)
        assertEquals(RecipeComponentType.INGREDIENT, recipe.ingredients[0].type)
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
