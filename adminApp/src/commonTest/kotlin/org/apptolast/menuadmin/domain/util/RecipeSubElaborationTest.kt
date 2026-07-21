package org.apptolast.menuadmin.domain.util

import org.apptolast.menuadmin.domain.model.AllergenType
import org.apptolast.menuadmin.domain.model.ContainmentLevel
import org.apptolast.menuadmin.domain.model.Ingredient
import org.apptolast.menuadmin.domain.model.IngredientAllergen
import org.apptolast.menuadmin.domain.model.Recipe
import org.apptolast.menuadmin.domain.model.RecipeComponentType
import org.apptolast.menuadmin.domain.model.RecipeIngredient
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Spec 003 — agregación recursiva de alérgenos de subelaboraciones + reparación de datos importados.
 */
class RecipeSubElaborationTest {
    private fun ing(
        id: String,
        allergens: List<Pair<String, ContainmentLevel>> = emptyList(),
    ) = Ingredient(
        id = id,
        name = "ing-$id",
        allergens = allergens.map { (code, level) ->
            IngredientAllergen(allergenCode = code, containmentLevel = level)
        },
    )

    private fun component(
        id: String,
        type: RecipeComponentType,
        name: String = "",
    ) = RecipeIngredient(ingredientId = id, ingredientName = name, type = type)

    // AC-04: los alérgenos de la subelaboración se agregan (recursivamente) al plato padre.
    @Test
    fun `aggregator includes sub-recipe allergens recursively`() {
        val sub = Recipe(
            id = "900",
            name = "Salsa",
            ingredients = listOf(component("200", RecipeComponentType.INGREDIENT)),
        )
        val dish = Recipe(
            id = "10",
            name = "Plato",
            ingredients = listOf(
                component("100", RecipeComponentType.INGREDIENT),
                component("900", RecipeComponentType.SUB_RECIPE, "Salsa"),
            ),
        )
        val ingredientsById = mapOf(
            "100" to ing("100"),
            "200" to ing("200", listOf("MOLLUSCS" to ContainmentLevel.CONTAINS)),
        )
        val recipesById = mapOf("10" to dish, "900" to sub)

        val result = RecipeAllergenAggregator.computeAllergens(dish, ingredientsById, recipesById)

        assertTrue("MOLLUSCS" in result.keys, "el alérgeno de la subelaboración debe agregarse al plato")
    }

    // AC-04: se conserva el nivel de contención más fuerte (CONTAINS gana a MAY_CONTAIN).
    @Test
    fun `aggregator keeps strongest containment level`() {
        val dish = Recipe(
            id = "10",
            ingredients = listOf(
                component("1", RecipeComponentType.INGREDIENT),
                component("2", RecipeComponentType.INGREDIENT),
            ),
        )
        val ingredientsById = mapOf(
            "1" to ing("1", listOf("GLUTEN" to ContainmentLevel.MAY_CONTAIN)),
            "2" to ing("2", listOf("GLUTEN" to ContainmentLevel.CONTAINS)),
        )

        val result = RecipeAllergenAggregator.computeAllergens(dish, ingredientsById, mapOf("10" to dish))

        assertEquals(ContainmentLevel.CONTAINS, result["GLUTEN"])
    }

    // AC-05: recetas que se referencian en ciclo terminan sin desbordar la pila.
    @Test
    fun `aggregator terminates on cycle`() {
        val a = Recipe(id = "A", ingredients = listOf(component("B", RecipeComponentType.SUB_RECIPE)))
        val b = Recipe(id = "B", ingredients = listOf(component("A", RecipeComponentType.SUB_RECIPE)))

        val result = RecipeAllergenAggregator.computeAllergens(a, emptyMap(), mapOf("A" to a, "B" to b))

        assertEquals(emptyMap(), result)
    }

    // AC-06: la reparación arregla type + nombre de un componente contaminado y recomputa alérgenos.
    @Test
    fun `repair fixes contaminated component and recomputes allergens`() {
        val sub = Recipe(
            id = "900",
            name = "Mayonesa casera",
            ingredients = listOf(component("200", RecipeComponentType.INGREDIENT)),
        )
        val dish = Recipe(
            id = "10",
            name = "Plato",
            // Contaminado: id de sub-receta guardado como ingrediente con nombre placeholder.
            ingredients = listOf(component("900", RecipeComponentType.INGREDIENT, "Ingrediente 900")),
        )
        val ingredientsById = mapOf("200" to ing("200", listOf("EGGS" to ContainmentLevel.CONTAINS)))

        val repaired = RecipeDataRepair.repairAll(listOf(dish, sub), ingredientsById)
        val fixedDish = repaired.first { it.id == "10" }
        val comp = fixedDish.ingredients[0]

        assertEquals(RecipeComponentType.SUB_RECIPE, comp.type)
        assertEquals("Mayonesa casera", comp.ingredientName)
        assertTrue(AllergenType.EGGS in fixedDish.computedAllergens, "alérgeno de la subelaboración tras reparar")
    }

    // AC-07: la reparación marca isSubRecipe en la receta referida, no en el plato.
    @Test
    fun `repair marks referenced recipe as sub-recipe`() {
        val sub = Recipe(id = "900", name = "Salsa", ingredients = emptyList())
        val dish = Recipe(
            id = "10",
            name = "Plato",
            ingredients = listOf(component("900", RecipeComponentType.INGREDIENT, "Ingrediente 900")),
        )

        val repaired = RecipeDataRepair.repairAll(listOf(dish, sub), emptyMap())

        assertTrue(repaired.first { it.id == "900" }.isSubRecipe)
        assertFalse(repaired.first { it.id == "10" }.isSubRecipe)
    }
}
