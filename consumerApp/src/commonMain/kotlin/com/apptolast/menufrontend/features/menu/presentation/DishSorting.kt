package com.apptolast.menufrontend.features.menu.presentation

import com.apptolast.menufrontend.domain.model.Dish
import com.apptolast.menufrontend.features.menu.data.DishSortMode

/** Flat ordering for NAME and PRICE modes (CATEGORY uses [groupedByCategory]). */
fun List<Dish>.sortedForMenu(
    mode: DishSortMode,
    ascending: Boolean,
): List<Dish> {
    val base = when (mode) {
        DishSortMode.PRICE -> sortedBy { it.price }
        else -> sortedBy { it.name.lowercase() }
    }
    return if (ascending) base else base.reversed()
}

/**
 * Groups dishes by category for the CATEGORY sort mode. Returns `category -> dishes` pairs with the
 * category groups ordered by name ([ascending]); the uncategorized group ([otherLabel]) is always
 * last, and dishes within each group are ordered by name ascending.
 */
fun List<Dish>.groupedByCategory(
    ascending: Boolean,
    otherLabel: String,
): List<Pair<String, List<Dish>>> {
    val groups = groupBy { it.category.ifBlank { otherLabel } }
        .mapValues { (_, dishes) -> dishes.sortedBy { it.name.lowercase() } }
    val named = groups.keys.filter { it != otherLabel }.sortedBy { it.lowercase() }
    val ordered = if (ascending) named else named.reversed()
    return buildList {
        ordered.forEach { add(it to groups.getValue(it)) }
        groups[otherLabel]?.let { add(otherLabel to it) }
    }
}
