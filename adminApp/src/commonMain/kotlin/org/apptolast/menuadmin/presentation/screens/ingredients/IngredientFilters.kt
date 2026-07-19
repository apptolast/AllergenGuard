package org.apptolast.menuadmin.presentation.screens.ingredients

import org.apptolast.menuadmin.domain.model.AllergenType
import org.apptolast.menuadmin.domain.model.Ingredient

// Spec 002 Fase A — funciones puras de filtrado/etiquetado de ingredientes (name/brand).

/** True si [ingredient] casa con [query] por nombre O marca (case-insensitive). Query en blanco = todo. */
fun ingredientMatchesQuery(
    ingredient: Ingredient,
    query: String,
): Boolean =
    query.isBlank() ||
        ingredient.name.contains(query, ignoreCase = true) ||
        ingredient.brand.contains(query, ignoreCase = true)

/**
 * Candidatos del selector de ingredientes de la receta: filtra por nombre O marca, de modo que dos
 * "Mayonesa" de marcas distintas se puedan encontrar tecleando la marca.
 */
fun filterIngredientsForPicker(
    query: String,
    ingredients: List<Ingredient>,
): List<Ingredient> = ingredients.filter { ingredientMatchesQuery(it, query) }

/** Etiqueta del selector: "nombre — marca" cuando el ingrediente tiene marca, si no solo el nombre. */
fun ingredientPickerLabel(ingredient: Ingredient): String =
    if (ingredient.brand.isNotBlank()) "${ingredient.name} — ${ingredient.brand}" else ingredient.name

/** Filtro completo de la lista de ingredientes: texto (name|brand) + tipos de alérgeno + marcas. */
fun filterIngredientsList(
    ingredients: List<Ingredient>,
    query: String,
    filterAllergens: Set<AllergenType>,
    filterBrands: Set<String>,
): List<Ingredient> =
    ingredients
        .filter { ingredientMatchesQuery(it, query) }
        .filter { filterAllergens.isEmpty() || it.allergenTypes.any { a -> a in filterAllergens } }
        .filter { filterBrands.isEmpty() || it.brand in filterBrands }
