package com.apptolast.menufrontend.data.firebase

import com.apptolast.menufrontend.domain.model.Allergen
import com.apptolast.menufrontend.domain.model.Dish
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Spec 005 — consumerApp: defensive menu filter, allergen-parse hardening, fail-closed safety.
 */
class ConsumerMenuLogicTest {
    private fun dish(
        id: String,
        allergens: Set<Allergen> = emptySet(),
        isSubRecipe: Boolean = false,
        hasUnknownAllergen: Boolean = false,
    ) = Dish(
        id = id,
        restaurantId = "r",
        name = "d-$id",
        description = "",
        price = 0.0,
        ingredients = emptyList(),
        allergens = allergens,
        isSubRecipe = isSubRecipe,
        hasUnknownAllergen = hasUnknownAllergen,
    )

    // AC-04: a sub-recipe never surfaces as a menu dish, even if its id is in recipeIds.
    @Test
    fun `menu excludes sub-recipes`() {
        val plate = dish("10")
        val sub = dish("900", isSubRecipe = true)
        val result = visibleMenuDishes(listOf(plate, sub), recipeIds = setOf("10", "900"))
        assertEquals(listOf(plate), result)
    }

    @Test
    fun `menu keeps only dishes in recipeIds`() {
        val a = dish("1")
        val b = dish("2")
        val result = visibleMenuDishes(listOf(a, b), recipeIds = setOf("1"))
        assertEquals(listOf(a), result)
    }

    // AC-06: a recognized code maps; an unknown code is flagged (never silently dropped as "safe").
    @Test
    fun `parse flags unknown allergen code`() {
        val parsed = parseComputedAllergens(listOf("GLUTEN", "WEIRD_NEW_CODE", "MOLLUSCS"))
        assertEquals(setOf(Allergen.GLUTEN, Allergen.MOLLUSKS), parsed.allergens)
        assertTrue(parsed.hasUnknown, "un código no reconocido debe marcarse")
    }

    @Test
    fun `parse without unknowns has no flag`() {
        val parsed = parseComputedAllergens(listOf("GLUTEN", "EGGS"))
        assertFalse(parsed.hasUnknown)
        assertEquals(setOf(Allergen.GLUTEN, Allergen.EGGS), parsed.allergens)
    }

    // AC-06: fail-closed — a dish with an unknown allergen is NOT safe, even without matching user allergens.
    @Test
    fun `dish with unknown allergen is not safe`() {
        assertFalse(dish("x", hasUnknownAllergen = true).isSafeFor(emptySet()))
    }

    @Test
    fun `dish is safe only when no user allergen and all known`() {
        val d = dish("x", allergens = setOf(Allergen.GLUTEN))
        assertTrue(d.isSafeFor(setOf(Allergen.FISH)))
        assertFalse(d.isSafeFor(setOf(Allergen.GLUTEN)))
    }
}
