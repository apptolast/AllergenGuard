package org.apptolast.menuadmin.domain.util

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.apptolast.menuadmin.data.util.JsonExporter
import org.apptolast.menuadmin.domain.model.AllergenType
import org.apptolast.menuadmin.domain.model.Dish
import org.apptolast.menuadmin.domain.model.Ingredient
import org.apptolast.menuadmin.domain.model.IngredientAllergen
import org.apptolast.menuadmin.domain.model.Menu
import org.apptolast.menuadmin.domain.model.Recipe
import org.apptolast.menuadmin.domain.model.RecipeIngredient
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Clock

class JsonExporterTest {
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    private val now = Clock.System.now()

    private val sampleMenu = Menu(
        id = "menu-test",
        name = "Test Menu",
        companyName = "Test Restaurant",
        dishes = listOf(
            Dish(
                id = "dish-001",
                name = "Croquetas Ibericas",
                description = "Croquetas cremosas",
                price = 12.50,
                category = "Entrantes",
                ingredients = listOf("ing-001", "ing-003"),
                allergens = setOf(AllergenType.GLUTEN, AllergenType.DAIRY),
                isAvailable = true,
            ),
            Dish(
                id = "dish-002",
                name = "Pollo al Ajillo",
                description = "Pollo campero al ajillo",
                price = 13.90,
                category = "Combinados",
                ingredients = listOf("ing-020"),
                allergens = emptySet(),
                isAvailable = true,
            ),
        ),
        isActive = true,
        createdAt = now,
        updatedAt = now,
    )

    private val sampleIngredients = listOf(
        Ingredient(
            id = "ing-001",
            name = "Harina de trigo",
            description = "Harina refinada",
            brand = "Harinera La Meta",
            allergens = listOf(
                IngredientAllergen(allergenCode = "GLUTEN", allergenName = "Gluten"),
            ),
            labelInfo = "Contiene gluten",
            createdAt = now,
            updatedAt = now,
        ),
        Ingredient(
            id = "ing-003",
            name = "Leche entera",
            description = "Leche pasteurizada",
            brand = "Central Lechera",
            allergens = listOf(
                IngredientAllergen(allergenCode = "MILK", allergenName = "Lácteos"),
            ),
            labelInfo = "Contiene lacteos",
            createdAt = now,
            updatedAt = now,
        ),
    )

    private val sampleRecipes = listOf(
        Recipe(
            id = "rec-001",
            name = "Croquetas Ibericas del Puchero",
            description = "Croquetas cremosas con jamon iberico",
            ingredients = listOf(
                RecipeIngredient("ing-001", "Harina de trigo", 200.0, "g"),
                RecipeIngredient("ing-003", "Leche entera", 500.0, "ml"),
            ),
            isActive = true,
            category = "Entrantes",
            createdAt = now,
            updatedAt = now,
        ),
    )

    @Test
    fun exportMenu_and_importMenu_roundTrip_producesSameMenu() {
        val exported = JsonExporter.exportMenu(sampleMenu, json)
        val imported = JsonExporter.importMenu(exported, json)

        assertEquals(sampleMenu, imported)
    }

    @Test
    fun exportAllData_and_importAllData_roundTrip_preservesAllData() {
        val exported = JsonExporter.exportAllData(
            ingredients = sampleIngredients,
            recipes = sampleRecipes,
            menus = listOf(sampleMenu),
            json = json,
        )

        val backup = JsonExporter.importAllData(exported, json)

        assertEquals(sampleIngredients, backup.ingredients)
        assertEquals(sampleRecipes, backup.recipes)
        assertEquals(listOf(sampleMenu), backup.menus)
    }

    @Test
    fun importMenu_withInvalidJson_throwsException() {
        val invalidJson = "{ this is not valid json }"

        assertFailsWith<SerializationException> {
            JsonExporter.importMenu(invalidJson, json)
        }
    }

    @Test
    fun importExternalData_parsesRealJsonFormat() {
        val externalJson =
            """
            {
                "ingredients": [
                    {"id": 1770378899314, "name": "Harina de trigo", "contains": ["gluten"]},
                    {"id": 1770378899315, "name": "Leche entera", "contains": ["milk"]}
                ],
                "recipes": [
                    {"id": 9990001, "name": "Croquetas", "ingredientIds": [1770378899314, 1770378899315], "active": true}
                ],
                "timestamp": "2026-02-24T10:00:00Z"
            }
            """.trimIndent()

        val result = JsonExporter.importExternalData(externalJson, json)

        assertEquals(2, result.ingredients.size)
        assertEquals(1, result.recipes.size)
        assertEquals("Harina de trigo", result.ingredients[0].name)
        assertEquals(setOf(AllergenType.GLUTEN), result.ingredients[0].allergenTypes)
        assertEquals(setOf(AllergenType.DAIRY), result.ingredients[1].allergenTypes)
        assertEquals("Croquetas", result.recipes[0].name)
        assertEquals(2, result.recipes[0].ingredients.size)
    }

    @Test
    fun exportExternalData_producesCompatibleFormat() {
        val exported = JsonExporter.exportExternalData(sampleIngredients, sampleRecipes, json)
        val result = JsonExporter.importExternalData(exported, json)

        assertEquals(sampleIngredients.size, result.ingredients.size)
        assertEquals(sampleRecipes.size, result.recipes.size)
        assertEquals(sampleIngredients[0].name, result.ingredients[0].name)
    }
}
