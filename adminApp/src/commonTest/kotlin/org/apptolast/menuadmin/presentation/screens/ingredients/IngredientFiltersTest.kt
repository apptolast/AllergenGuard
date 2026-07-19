package org.apptolast.menuadmin.presentation.screens.ingredients

import org.apptolast.menuadmin.domain.model.AllergenType
import org.apptolast.menuadmin.domain.model.Ingredient
import org.apptolast.menuadmin.domain.model.IngredientAllergen
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Spec 002 Fase A — desambiguación por marca en el selector de receta y filtro de marca en la lista.
 */
class IngredientFiltersTest {
    private fun ing(
        name: String,
        brand: String = "",
        allergens: List<String> = emptyList(),
        restaurantId: String = "",
    ) = Ingredient(
        id = "$name-$brand-$restaurantId",
        name = name,
        brand = brand,
        restaurantId = restaurantId,
        allergens = allergens.map { IngredientAllergen(allergenCode = it) },
    )

    private val casera = ing("Mayonesa", "Casera")
    private val hellmanns = ing("Mayonesa", "Hellmann's")
    private val ketchup = ing("Ketchup", "Heinz", allergens = listOf("MUSTARD"))
    private val all = listOf(casera, hellmanns, ketchup)

    // AC-02: el picker filtra por nombre O marca.
    @Test
    fun pickerFiltersByBrand() {
        assertEquals(listOf(hellmanns), filterIngredientsForPicker("hellmann", all))
    }

    @Test
    fun pickerFiltersByName() {
        assertEquals(listOf(casera, hellmanns), filterIngredientsForPicker("mayonesa", all))
    }

    @Test
    fun pickerBlankQueryReturnsAll() {
        assertEquals(all, filterIngredientsForPicker("", all))
    }

    // AC-01: la etiqueta del picker incluye la marca para desambiguar.
    @Test
    fun pickerLabelIncludesBrand() {
        assertEquals("Mayonesa — Casera", ingredientPickerLabel(casera))
    }

    @Test
    fun pickerLabelWithoutBrandIsJustName() {
        assertEquals("Sal", ingredientPickerLabel(ing("Sal")))
    }

    // AC-03: la lista filtra por marca (estructurado), combinable con alérgenos.
    @Test
    fun listFiltersByBrand() {
        assertEquals(
            listOf(casera),
            filterIngredientsList(all, query = "", filterAllergens = emptySet(), filterBrands = setOf("Casera")),
        )
    }

    @Test
    fun listCombinesBrandAndAllergen() {
        assertEquals(
            listOf(ketchup),
            filterIngredientsList(all, "", setOf(AllergenType.MUSTARD), setOf("Heinz")),
        )
    }

    // Fase B — alcance por restaurante (un solo restaurante; blank = global/compartido).
    private val salGlobal = ing("Sal")
    private val salsaR1 = ing("Salsa", restaurantId = "r1")
    private val otraR2 = ing("Otra", restaurantId = "r2")
    private val scoped = listOf(salGlobal, salsaR1, otraR2)

    // AC-06: el picker de la receta del restaurante R muestra globales + específicos de R.
    @Test
    fun pickerIncludesGlobalAndRestaurantSpecific() {
        assertEquals(listOf(salGlobal, salsaR1), filterIngredientsForPicker("", scoped, restaurantId = "r1"))
    }

    @Test
    fun pickerBlankRestaurantShowsAll() {
        assertEquals(scoped, filterIngredientsForPicker("", scoped, restaurantId = ""))
    }

    // AC-05: la lista puede filtrarse por restaurante (globales + los de ese restaurante).
    @Test
    fun listFiltersByRestaurant() {
        assertEquals(
            listOf(salGlobal, salsaR1),
            filterIngredientsList(scoped, "", emptySet(), emptySet(), filterRestaurantId = "r1"),
        )
    }
}
