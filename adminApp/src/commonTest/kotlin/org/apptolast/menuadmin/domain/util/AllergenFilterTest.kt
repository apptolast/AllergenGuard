package org.apptolast.menuadmin.domain.util

import org.apptolast.menuadmin.domain.model.AllergenType
import org.apptolast.menuadmin.domain.model.Dish
import org.apptolast.menuadmin.domain.model.Ingredient
import org.apptolast.menuadmin.domain.model.IngredientAllergen
import org.apptolast.menuadmin.domain.model.Recipe
import org.apptolast.menuadmin.domain.model.RecipeIngredient
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AllergenFilterTest {
    private val dishNoAllergens = Dish(
        id = "dish-safe",
        name = "Butifarra a la Brasa",
        description = "Butifarra artesana con patatas panaderas",
        price = 14.50,
        category = "Combinados",
        ingredients = listOf("ing-022", "ing-015"),
        allergens = emptySet(),
    )

    private val dishWithGluten = Dish(
        id = "dish-gluten",
        name = "Croquetas Ibericas",
        description = "Croquetas cremosas con jamon iberico",
        price = 12.50,
        category = "Entrantes",
        ingredients = listOf("ing-001", "ing-003", "ing-004"),
        allergens = setOf(AllergenType.GLUTEN, AllergenType.DAIRY, AllergenType.EGGS),
    )

    private val dishWithSeafood = Dish(
        id = "dish-seafood",
        name = "Arroz Caldoso Marinero",
        description = "Arroz caldoso con gambas y calamares",
        price = 18.90,
        category = "Arroces",
        ingredients = listOf("ing-019", "ing-002", "ing-014"),
        allergens = setOf(AllergenType.CRUSTACEANS, AllergenType.MOLLUSKS),
    )

    private val allDishes = listOf(dishNoAllergens, dishWithGluten, dishWithSeafood)

    @Test
    fun filterSafeDishes_withNoUserAllergens_returnsAllDishesAsSafe() {
        val (safe, unsafe) = AllergenFilter.filterSafeDishes(allDishes, emptySet())

        assertEquals(3, safe.size)
        assertTrue(unsafe.isEmpty())
        assertEquals(allDishes, safe)
    }

    @Test
    fun filterSafeDishes_correctlySeparatesSafeAndUnsafeDishes() {
        val userAllergens = setOf(AllergenType.GLUTEN)

        val (safe, unsafe) = AllergenFilter.filterSafeDishes(allDishes, userAllergens)

        assertEquals(2, safe.size)
        assertEquals(1, unsafe.size)
        assertTrue(safe.contains(dishNoAllergens))
        assertTrue(safe.contains(dishWithSeafood))
        assertTrue(unsafe.contains(dishWithGluten))
    }

    @Test
    fun filterSafeDishes_withAllAllergensSelected_returnsNoSafeDishes() {
        val dishesWithAllergens = listOf(dishWithGluten, dishWithSeafood)
        val userAllergens = AllergenType.entries.toSet()

        val (safe, unsafe) = AllergenFilter.filterSafeDishes(dishesWithAllergens, userAllergens)

        assertTrue(safe.isEmpty())
        assertEquals(2, unsafe.size)
    }

    @Test
    fun aggregateAllergens_resolvesAllergensFromIngredients() {
        val flour = Ingredient(
            id = "ing-001",
            name = "Harina de trigo",
            allergens = listOf(
                IngredientAllergen(allergenCode = "GLUTEN", allergenName = "Gluten"),
            ),
        )
        val milk = Ingredient(
            id = "ing-003",
            name = "Leche entera",
            allergens = listOf(
                IngredientAllergen(allergenCode = "MILK", allergenName = "Lácteos"),
            ),
        )
        val oliveOil = Ingredient(
            id = "ing-018",
            name = "Aceite de oliva",
        )

        val recipe = Recipe(
            id = "rec-test",
            name = "Test Recipe",
            ingredients = listOf(
                RecipeIngredient("ing-001", "Harina de trigo", 200.0, "g"),
                RecipeIngredient("ing-003", "Leche entera", 500.0, "ml"),
                RecipeIngredient("ing-018", "Aceite de oliva", 50.0, "ml"),
            ),
        )

        val lookup = mapOf(
            "ing-001" to flour,
            "ing-003" to milk,
            "ing-018" to oliveOil,
        )

        val allergens = AllergenFilter.aggregateAllergens(recipe, lookup)

        assertEquals(setOf(AllergenType.GLUTEN, AllergenType.DAIRY), allergens)
    }

    @Test
    fun aggregateAllergens_withUnknownIngredientIds_returnsEmptySet() {
        val recipe = Recipe(
            id = "rec-unknown",
            name = "Unknown Ingredients Recipe",
            ingredients = listOf(
                RecipeIngredient("ing-999", "Unknown A", 100.0, "g"),
                RecipeIngredient("ing-998", "Unknown B", 200.0, "ml"),
            ),
        )

        val emptyLookup = emptyMap<String, Ingredient>()

        val allergens = AllergenFilter.aggregateAllergens(recipe, emptyLookup)

        assertTrue(allergens.isEmpty())
    }
}
