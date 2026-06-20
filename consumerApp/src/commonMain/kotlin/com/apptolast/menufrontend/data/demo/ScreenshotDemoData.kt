package com.apptolast.menufrontend.data.demo

import com.apptolast.menufrontend.domain.model.Allergen
import com.apptolast.menufrontend.domain.model.Dish
import com.apptolast.menufrontend.domain.model.Restaurant
import com.apptolast.menufrontend.domain.model.User

/**
 * Fictional, network-free data shown only in [com.apptolast.menufrontend.core.screenshot.ScreenshotMode]
 * (App Store screenshots). No real personal data; food photos are public thumbnails from TheMealDB.
 *
 * The demo user is allergic to gluten and peanuts, so the menu has a mix of safe dishes (shown by
 * default, in green) and unsafe ones (revealed by the toggle, in red) to showcase the filtering.
 */
object ScreenshotDemoData {
    const val RESTAURANT_ID = "demo-rest-1"
    const val DISH_ID = "demo-dish-salmon"

    val user = User(
        id = "demo-user",
        name = "Lucía Martín",
        email = "lucia.martin@example.com",
        allergens = setOf(Allergen.GLUTEN, Allergen.PEANUTS),
        favoriteRestaurantIds = setOf(RESTAURANT_ID, "demo-rest-3"),
    )

    val allergens: Set<Allergen> = user.allergens

    val restaurants = listOf(
        Restaurant(RESTAURANT_ID, "El Jardín Mediterráneo", "Mediterránea", 4.7f, 312, 24, IMG_CHICKEN, "350 m"),
        Restaurant("demo-rest-2", "Sakura Sushi", "Japonesa", 4.5f, 198, 18, IMG_TUNA, "1,2 km"),
        Restaurant("demo-rest-3", "La Huerta Verde", "Vegetariana", 4.8f, 142, 16, IMG_QUINOA, "600 m"),
        Restaurant("demo-rest-4", "Bistró Atlántico", "Pescados y mariscos", 4.6f, 220, 20, IMG_SALMON, "900 m"),
    )

    val favoriteRestaurants = restaurants.filter { it.id in user.favoriteRestaurantIds }

    val menu = listOf(
        Dish(
            id = DISH_ID,
            restaurantId = RESTAURANT_ID,
            name = "Salmón al horno con hinojo",
            description = "Lomo de salmón al horno con hinojo asado y tomate cherry.",
            category = "Pescados",
            price = 16.50,
            imageUrl = IMG_SALMON,
            ingredients = listOf("Salmón", "Hinojo", "Tomate cherry", "Aceite de oliva", "Limón"),
            allergens = setOf(Allergen.FISH),
        ),
        Dish(
            id = "demo-dish-chicken",
            restaurantId = RESTAURANT_ID,
            name = "Pollo a la plancha con verduras",
            description = "Pechuga de pollo a la plancha con verduras de temporada salteadas.",
            category = "Carnes",
            price = 13.90,
            imageUrl = IMG_CHICKEN,
            ingredients = listOf("Pollo", "Calabacín", "Pimiento", "Cebolla", "Aceite de oliva"),
            allergens = emptySet(),
        ),
        Dish(
            id = "demo-dish-quinoa",
            restaurantId = RESTAURANT_ID,
            name = "Ensalada de quinoa y aguacate",
            description = "Quinoa, aguacate, tomate y vinagreta cítrica.",
            category = "Ensaladas",
            price = 10.50,
            imageUrl = IMG_QUINOA,
            ingredients = listOf("Quinoa", "Aguacate", "Tomate", "Limón", "Aceite de oliva"),
            allergens = setOf(Allergen.SULFITES),
        ),
        Dish(
            id = "demo-dish-tuna",
            restaurantId = RESTAURANT_ID,
            name = "Tartar de atún",
            description = "Atún rojo, aguacate y sésamo con toque de soja.",
            category = "Entrantes",
            price = 14.00,
            imageUrl = IMG_TUNA,
            ingredients = listOf("Atún rojo", "Aguacate", "Sésamo", "Salsa de soja"),
            allergens = setOf(Allergen.FISH, Allergen.SOY, Allergen.SESAME),
        ),
        Dish(
            id = "demo-dish-pumpkin",
            restaurantId = RESTAURANT_ID,
            name = "Crema de calabaza",
            description = "Crema suave de calabaza asada con un hilo de aceite de oliva.",
            category = "Entrantes",
            price = 7.50,
            imageUrl = IMG_PUMPKIN,
            ingredients = listOf("Calabaza", "Cebolla", "Caldo de verduras", "Aceite de oliva"),
            allergens = emptySet(),
        ),
        Dish(
            id = "demo-dish-lasagna",
            restaurantId = RESTAURANT_ID,
            name = "Lasaña boloñesa",
            description = "Pasta al horno con boloñesa de ternera y bechamel.",
            category = "Pastas",
            price = 12.00,
            imageUrl = null,
            ingredients = listOf("Pasta", "Ternera", "Tomate", "Leche", "Harina de trigo"),
            allergens = setOf(Allergen.GLUTEN, Allergen.DAIRY),
        ),
        Dish(
            id = "demo-dish-padthai",
            restaurantId = RESTAURANT_ID,
            name = "Pad Thai con cacahuete",
            description = "Fideos de arroz salteados con gambas, huevo y cacahuete.",
            category = "Pastas",
            price = 11.50,
            imageUrl = null,
            ingredients = listOf("Fideos de arroz", "Gambas", "Huevo", "Cacahuete", "Salsa de soja"),
            allergens = setOf(Allergen.PEANUTS, Allergen.CRUSTACEANS, Allergen.EGGS, Allergen.SOY),
        ),
    )

    const val RESTAURANT_DESCRIPTION =
        "Cocina mediterránea de mercado, con cada plato marcado según sus alérgenos."

    // Public food thumbnails (TheMealDB), loaded by Coil over the network during screenshot capture.
    private const val IMG_SALMON = "https://www.themealdb.com/images/media/meals/1548772327.jpg"
    private const val IMG_CHICKEN = "https://www.themealdb.com/images/media/meals/1520084413.jpg"
    private const val IMG_QUINOA = "https://www.themealdb.com/images/media/meals/yvpuuy1511797244.jpg"
    private const val IMG_TUNA = "https://www.themealdb.com/images/media/meals/wuxrtu1483564410.jpg"
    private const val IMG_PUMPKIN = "https://www.themealdb.com/images/media/meals/1529446352.jpg"
}
