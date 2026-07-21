package org.apptolast.menuadmin.presentation.screens.ingredients

import org.apptolast.menuadmin.domain.model.AllergenType
import org.apptolast.menuadmin.domain.model.Ingredient

// Spec 002 — funciones puras de filtrado/etiquetado de ingredientes (name/brand/restaurante).

/** True si [ingredient] casa con [query] por nombre O marca (case-insensitive). Query en blanco = todo. */
fun ingredientMatchesQuery(
    ingredient: Ingredient,
    query: String,
): Boolean =
    query.isBlank() ||
        ingredient.name.contains(query, ignoreCase = true) ||
        ingredient.brand.contains(query, ignoreCase = true)

/**
 * Alcance por restaurante (un solo restaurante). Con [restaurantId] en blanco no hay filtro (pasa todo);
 * si está informado, pasan los ingredientes globales (restaurantId en blanco) y los de ese restaurante.
 */
fun ingredientInRestaurantScope(
    ingredient: Ingredient,
    restaurantId: String,
): Boolean =
    restaurantId.isBlank() ||
        ingredient.restaurantId.isBlank() ||
        ingredient.restaurantId == restaurantId

/**
 * Candidatos del selector de ingredientes de la receta: filtra por nombre O marca, y por alcance de
 * restaurante (globales + los específicos del [restaurantId] de la receta).
 */
fun filterIngredientsForPicker(
    query: String,
    ingredients: List<Ingredient>,
    restaurantId: String = "",
): List<Ingredient> =
    ingredients.filter {
        ingredientMatchesQuery(it, query) && ingredientInRestaurantScope(it, restaurantId)
    }

/** Etiqueta del selector: "nombre — marca" cuando el ingrediente tiene marca, si no solo el nombre. */
fun ingredientPickerLabel(ingredient: Ingredient): String =
    if (ingredient.brand.isNotBlank()) "${ingredient.name} — ${ingredient.brand}" else ingredient.name

/** Filtro completo de la lista: texto (name|brand) + tipos de alérgeno + marcas + alcance de restaurante. */
fun filterIngredientsList(
    ingredients: List<Ingredient>,
    query: String,
    filterAllergens: Set<AllergenType>,
    filterBrands: Set<String>,
    filterRestaurantId: String = "",
): List<Ingredient> =
    ingredients
        .filter { ingredientMatchesQuery(it, query) }
        .filter { filterAllergens.isEmpty() || it.allergenTypes.any { a -> a in filterAllergens } }
        .filter { filterBrands.isEmpty() || it.brand in filterBrands }
        .filter { ingredientInRestaurantScope(it, filterRestaurantId) }
