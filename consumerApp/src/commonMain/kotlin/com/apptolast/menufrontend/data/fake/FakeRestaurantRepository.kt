package com.apptolast.menufrontend.data.fake

import com.apptolast.menufrontend.data.repository.RestaurantRepository
import com.apptolast.menufrontend.domain.model.Allergen
import com.apptolast.menufrontend.domain.model.Dish
import com.apptolast.menufrontend.domain.model.Restaurant

class FakeRestaurantRepository : RestaurantRepository {

    private val restaurants = listOf(
        Restaurant(
            id = "rest-1",
            name = "Hotel Palace Barcelona",
            cuisineType = "Cocina mediterránea",
            rating = 4.8f,
            reviewCount = 124,
            dishCount = 42,
        ),
        Restaurant(
            id = "rest-2",
            name = "La Brava Piconera",
            cuisineType = "Tapas y raciones",
            rating = 4.5f,
            reviewCount = 87,
            dishCount = 26,
        ),
        Restaurant(
            id = "rest-3",
            name = "Taberna El Serranito",
            cuisineType = "Cocina andaluza",
            rating = 4.6f,
            reviewCount = 203,
            dishCount = 35,
        ),
        Restaurant(
            id = "rest-4",
            name = "Sakura Sushi",
            cuisineType = "Cocina japonesa",
            rating = 4.7f,
            reviewCount = 156,
            dishCount = 48,
        ),
        Restaurant(
            id = "rest-5",
            name = "Trattoria Bella Napoli",
            cuisineType = "Cocina italiana",
            rating = 4.4f,
            reviewCount = 92,
            dishCount = 31,
        ),
    )

    private val dishes = listOf(
        // Hotel Palace Barcelona
        Dish(
            id = "dish-1",
            restaurantId = "rest-1",
            name = "Butifarra Cordobesa a la Brasa",
            description = "Patata, Butifarra, Alioli de huevos fritos. Cebollino",
            price = 14.50,
            ingredients = listOf("Patata", "Butifarra", "Alioli"),
            allergens = setOf(Allergen.EGGS),
        ),
        Dish(
            id = "dish-2",
            restaurantId = "rest-1",
            name = "Salmorejo de Tomates a la Brasa",
            description = "Salmorejo asado. Huevos de codorniz. Pizcos de jamón",
            price = 11.00,
            ingredients = listOf("Tomate", "Pan", "Aceite de oliva", "Jamón", "Huevo de codorniz"),
            allergens = setOf(Allergen.GLUTEN, Allergen.EGGS),
        ),
        Dish(
            id = "dish-3",
            restaurantId = "rest-1",
            name = "Ensalada Mediterránea",
            description = "Tomate, pepino, aceitunas, queso feta, vinagreta de limón",
            price = 12.00,
            ingredients = listOf("Tomate", "Pepino", "Aceitunas", "Queso feta", "Limón"),
            allergens = setOf(Allergen.DAIRY),
        ),
        Dish(
            id = "dish-4",
            restaurantId = "rest-1",
            name = "Lubina a la Plancha",
            description = "Lubina fresca con verduras de temporada y salsa de mantequilla",
            price = 22.00,
            ingredients = listOf("Lubina", "Verduras", "Mantequilla", "Limón"),
            allergens = setOf(Allergen.FISH, Allergen.DAIRY),
        ),
        Dish(
            id = "dish-5",
            restaurantId = "rest-1",
            name = "Paella de Verduras",
            description = "Arroz bomba, judías verdes, alcachofas, pimiento rojo, azafrán",
            price = 16.00,
            ingredients = listOf("Arroz", "Judías verdes", "Alcachofas", "Pimiento", "Azafrán"),
            allergens = emptySet(),
        ),
        Dish(
            id = "dish-6",
            restaurantId = "rest-1",
            name = "Croquetas de Jamón Ibérico",
            description = "Croquetas caseras de jamón ibérico con bechamel cremosa",
            price = 9.50,
            ingredients = listOf("Jamón ibérico", "Bechamel", "Pan rallado"),
            allergens = setOf(Allergen.GLUTEN, Allergen.DAIRY, Allergen.EGGS),
        ),
        Dish(
            id = "dish-7",
            restaurantId = "rest-1",
            name = "Tiramisú Clásico",
            description = "Bizcocho de café, mascarpone, cacao en polvo",
            price = 8.50,
            ingredients = listOf("Mascarpone", "Café", "Bizcocho", "Cacao"),
            allergens = setOf(Allergen.GLUTEN, Allergen.DAIRY, Allergen.EGGS),
        ),
        // La Brava Piconera
        Dish(
            id = "dish-8",
            restaurantId = "rest-2",
            name = "Patatas Bravas",
            description = "Patatas fritas con salsa brava picante y alioli",
            price = 7.50,
            ingredients = listOf("Patata", "Salsa brava", "Alioli"),
            allergens = setOf(Allergen.EGGS),
        ),
        Dish(
            id = "dish-9",
            restaurantId = "rest-2",
            name = "Gambas al Ajillo",
            description = "Gambas salteadas con ajo, guindilla y aceite de oliva",
            price = 13.00,
            ingredients = listOf("Gambas", "Ajo", "Guindilla", "Aceite de oliva"),
            allergens = setOf(Allergen.CRUSTACEANS),
        ),
        Dish(
            id = "dish-10",
            restaurantId = "rest-2",
            name = "Tortilla Española",
            description = "Tortilla de patatas con cebolla caramelizada",
            price = 9.00,
            ingredients = listOf("Huevos", "Patata", "Cebolla"),
            allergens = setOf(Allergen.EGGS),
        ),
    )

    override suspend fun getNearbyRestaurants(): Result<List<Restaurant>> {
        return Result.success(restaurants)
    }

    override suspend fun searchRestaurants(query: String): Result<List<Restaurant>> {
        if (query.isBlank()) return Result.success(restaurants)
        val filtered = restaurants.filter {
            it.name.contains(query, ignoreCase = true) ||
                it.cuisineType.contains(query, ignoreCase = true)
        }
        return Result.success(filtered)
    }

    override suspend fun getRestaurantMenu(restaurantId: String): Result<List<Dish>> {
        val menu = dishes.filter { it.restaurantId == restaurantId }
        return Result.success(menu)
    }

    override suspend fun getDishDetail(dishId: String): Result<Dish> {
        val dish = dishes.find { it.id == dishId }
        return if (dish != null) {
            Result.success(dish)
        } else {
            Result.failure(NoSuchElementException("Dish not found: $dishId"))
        }
    }

    override suspend fun getRestaurantName(restaurantId: String): Result<String> {
        val restaurant = restaurants.find { it.id == restaurantId }
        return if (restaurant != null) {
            Result.success(restaurant.name)
        } else {
            Result.failure(NoSuchElementException("Restaurant not found: $restaurantId"))
        }
    }
}
