package org.apptolast.menuadmin.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Section(
    val id: String,
    val name: String,
    val displayOrder: Int = 0,
    val dishes: List<Dish> = emptyList(),
)
