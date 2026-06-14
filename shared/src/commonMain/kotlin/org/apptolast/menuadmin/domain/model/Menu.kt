package org.apptolast.menuadmin.domain.model

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class Menu(
    val id: String,
    val restaurantId: String = "",
    val name: String,
    val description: String = "",
    val displayOrder: Int = 0,
    val sections: List<Section> = emptyList(),
    val published: Boolean = false,
    val archived: Boolean = false,
    val restaurantLogoUrl: String? = null,
    val companyLogoUrl: String? = null,
    val recipes: List<MenuRecipeSummary> = emptyList(),
    // Flattened dishes from all sections (for backward compatibility)
    val dishes: List<Dish> = emptyList(),
    // Legacy fields (used by mock data)
    val companyName: String = "",
    val isActive: Boolean = true,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
)

@Serializable
data class MenuRecipeSummary(
    val id: String,
    val name: String,
)

@Serializable
data class Dish(
    val id: String,
    val name: String,
    val description: String = "",
    val price: Double = 0.0,
    val sectionId: String = "",
    val allergens: Set<AllergenType> = emptySet(),
    val dishAllergens: List<DishAllergen> = emptyList(),
    val safetyLevel: SafetyLevel? = null,
    val matchedAllergens: List<String> = emptyList(),
    val imageUrl: String? = null,
    val isAvailable: Boolean = true,
    val category: String = "",
    val ingredients: List<String> = emptyList(),
)
