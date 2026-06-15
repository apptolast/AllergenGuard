package org.apptolast.menuadmin.navigation

import kotlinx.serialization.Serializable

@Serializable
object LoginRoute

@Serializable
object DashboardRoute

@Serializable
object IngredientsRoute

@Serializable
object RestaurantsRoute

@Serializable
data class RestaurantDetailRoute(
    val restaurantId: String,
)

@Serializable
object SettingsRoute

@Serializable
object BackupRestoreRoute

@Serializable
object ProfileRoute
