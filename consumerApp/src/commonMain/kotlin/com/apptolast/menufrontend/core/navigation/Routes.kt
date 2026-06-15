package com.apptolast.menufrontend.core.navigation

import kotlinx.serialization.Serializable

interface Destination

@Serializable
data object LoginRoute : Destination

@Serializable
data object HomeRoute : Destination

@Serializable
data object FavoritesRoute : Destination

@Serializable
data object ProfileRoute : Destination

@Serializable
data class MenuRoute(val restaurantId: String) : Destination

@Serializable
data class DishDetailRoute(val dishId: String, val restaurantId: String) : Destination

@Serializable
data object ScannerRoute : Destination
