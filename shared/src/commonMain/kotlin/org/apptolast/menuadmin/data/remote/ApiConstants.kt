package org.apptolast.menuadmin.data.remote

import org.apptolast.menuadmin.BuildKonfig

object ApiConstants {
    val BASE_URL: String = BuildKonfig.API_BASE_URL

    // Auth (no bearer token required)
    const val AUTH_LOGIN = "/api/v1/auth/login"
    const val AUTH_REGISTER_ADMIN = "/api/v1/auth/register-admin"
    const val AUTH_REFRESH = "/api/v1/auth/refresh"

    // Admin - Restaurants
    const val ADMIN_RESTAURANTS = "/api/v1/admin/restaurants"

    // Admin - Ingredients
    const val ADMIN_INGREDIENTS = "/api/v1/admin/ingredients"

    // Admin - Recipes (scoped to restaurant)
    const val ADMIN_RECIPES = "/api/v1/admin/recipes"

    // Admin - Menus (scoped to restaurant)
    const val ADMIN_MENUS = "/api/v1/admin/menus"

    // Admin - Dishes (scoped to restaurant)
    const val ADMIN_DISHES = "/api/v1/admin/dishes"

    // Admin - Menu Digital Cards
    const val ADMIN_MENU_DIGITAL_CARDS = "/api/v1/admin/menu-digital-cards"

    // Public
    const val ALLERGENS = "/api/v1/allergens"
    const val RESTAURANTS = "/api/v1/restaurants"
}
