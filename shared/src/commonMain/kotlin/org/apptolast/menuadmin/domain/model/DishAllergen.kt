package org.apptolast.menuadmin.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class DishAllergen(
    val allergenId: Int,
    val code: String,
    val allergenType: AllergenType? = null,
    val containmentLevel: ContainmentLevel = ContainmentLevel.CONTAINS,
    val notes: String = "",
)
