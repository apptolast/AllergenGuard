package org.apptolast.menuadmin.domain.model

import kotlin.time.Instant

data class DashboardStats(
    val totalIngredients: Int = 0,
    val activeRecipes: Int = 0,
    val totalMenus: Int = 0,
    val totalRestaurants: Int = 0,
    val recentActivity: List<ActivityEntry> = emptyList(),
    val allergenFrequency: Map<AllergenType, Int> = emptyMap(),
)

data class ActivityEntry(
    val description: String,
    val timestamp: Instant,
    val type: ActivityType,
)

enum class ActivityType { CREATED, UPDATED, DELETED }
