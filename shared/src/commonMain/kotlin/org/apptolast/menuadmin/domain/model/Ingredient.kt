package org.apptolast.menuadmin.domain.model

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class Ingredient(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val brand: String = "",
    val labelInfo: String = "",
    val allergens: List<IngredientAllergen> = emptyList(),
    val createdAt: Instant = Instant.DISTANT_PAST,
    val updatedAt: Instant = Instant.DISTANT_PAST,
) {
    val allergenTypes: Set<AllergenType>
        get() = allergens.mapNotNull { AllergenType.fromApiCode(it.allergenCode) }.toSet()
}

@Serializable
data class IngredientAllergen(
    val allergenId: Int = 0,
    val allergenCode: String = "",
    val allergenName: String = "",
    val containmentLevel: ContainmentLevel = ContainmentLevel.CONTAINS,
)
