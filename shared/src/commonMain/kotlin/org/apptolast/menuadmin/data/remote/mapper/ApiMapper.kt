package org.apptolast.menuadmin.data.remote.mapper

import org.apptolast.menuadmin.data.remote.dish.DishAllergenResponseDto
import org.apptolast.menuadmin.data.remote.dish.DishResponseDto
import org.apptolast.menuadmin.data.remote.ingredient.IngredientAllergenResponseDto
import org.apptolast.menuadmin.data.remote.ingredient.IngredientResponseDto
import org.apptolast.menuadmin.data.remote.menu.MenuRecipeResponseDto
import org.apptolast.menuadmin.data.remote.menu.MenuResponseDto
import org.apptolast.menuadmin.data.remote.menu.SectionResponseDto
import org.apptolast.menuadmin.data.remote.menudigitalcard.MenuDigitalCardResponseDto
import org.apptolast.menuadmin.data.remote.recipe.RecipeIngredientResponseDto
import org.apptolast.menuadmin.data.remote.recipe.RecipeResponseDto
import org.apptolast.menuadmin.data.remote.recipe.RecipeSummaryResponseDto
import org.apptolast.menuadmin.data.remote.restaurant.RestaurantResponseDto
import org.apptolast.menuadmin.domain.model.AllergenType
import org.apptolast.menuadmin.domain.model.ContainmentLevel
import org.apptolast.menuadmin.domain.model.Dish
import org.apptolast.menuadmin.domain.model.DishAllergen
import org.apptolast.menuadmin.domain.model.Ingredient
import org.apptolast.menuadmin.domain.model.IngredientAllergen
import org.apptolast.menuadmin.domain.model.Menu
import org.apptolast.menuadmin.domain.model.MenuDigitalCard
import org.apptolast.menuadmin.domain.model.MenuRecipeSummary
import org.apptolast.menuadmin.domain.model.Recipe
import org.apptolast.menuadmin.domain.model.RecipeIngredient
import org.apptolast.menuadmin.domain.model.Restaurant
import org.apptolast.menuadmin.domain.model.SafetyLevel
import org.apptolast.menuadmin.domain.model.Section
import kotlin.time.Clock
import kotlin.time.Instant

fun RestaurantResponseDto.toDomain(): Restaurant =
    Restaurant(
        id = id,
        name = name,
        slug = slug,
        description = description,
        address = address,
        phone = phone,
        logoUrl = logoUrl,
        active = active,
    )

fun MenuResponseDto.toDomain(): Menu {
    val domainSections = sections.map { it.toDomain() }
    val allDishes = domainSections.flatMap { it.dishes }
    return Menu(
        id = id,
        name = name,
        description = description,
        displayOrder = displayOrder,
        sections = domainSections,
        published = published,
        archived = archived,
        restaurantLogoUrl = restaurantLogoUrl,
        companyLogoUrl = companyLogoUrl,
        recipes = recipes.map { it.toDomain() },
        dishes = allDishes,
    )
}

fun MenuRecipeResponseDto.toDomain(): MenuRecipeSummary = MenuRecipeSummary(id = id, name = name)

fun SectionResponseDto.toDomain(): Section =
    Section(
        id = id,
        name = name,
        displayOrder = displayOrder,
    )

fun DishResponseDto.toDomain(): Dish {
    val domainAllergens = allergens.map { it.toDomain() }
    return Dish(
        id = id,
        name = name,
        description = description,
        price = price,
        sectionId = sectionId ?: "",
        imageUrl = imageUrl,
        isAvailable = available,
        dishAllergens = domainAllergens,
        allergens = domainAllergens.mapNotNull { it.allergenType }.toSet(),
        safetyLevel = safetyLevel?.let { SafetyLevel.fromApi(it) },
        matchedAllergens = matchedAllergens,
    )
}

fun DishAllergenResponseDto.toDomain(): DishAllergen =
    DishAllergen(
        allergenId = allergenId,
        code = code,
        allergenType = AllergenType.fromApiCode(code),
        containmentLevel = ContainmentLevel.fromApi(containmentLevel),
        notes = notes,
    )

@OptIn(kotlin.time.ExperimentalTime::class)
fun IngredientResponseDto.toDomain(): Ingredient =
    Ingredient(
        id = id,
        name = name,
        description = description ?: "",
        brand = brand ?: "",
        labelInfo = labelInfo ?: "",
        allergens = allergens.map { it.toDomain() },
        createdAt = createdAt?.let { runCatching { Instant.parse(it) }.getOrNull() }
            ?: Clock.System.now(),
        updatedAt = updatedAt?.let { runCatching { Instant.parse(it) }.getOrNull() }
            ?: Clock.System.now(),
    )

fun IngredientAllergenResponseDto.toDomain(): IngredientAllergen =
    IngredientAllergen(
        allergenId = allergenId,
        allergenCode = allergenCode,
        allergenName = allergenName,
        containmentLevel = ContainmentLevel.fromApi(containmentLevel),
    )

@OptIn(kotlin.time.ExperimentalTime::class)
fun RecipeResponseDto.toDomain(): Recipe {
    val allergenTypes = computedAllergens.mapNotNull { AllergenType.fromApiCode(it.allergenCode) }.toSet()
    return Recipe(
        id = id,
        restaurantId = restaurantId,
        name = name,
        description = description ?: "",
        category = category ?: "",
        price = price ?: 0.0,
        isActive = active,
        ingredients = ingredients.map { it.toDomain() },
        computedAllergens = allergenTypes,
        ingredientCount = ingredients.size,
        allergenCount = allergenTypes.size,
        createdAt = createdAt?.let { runCatching { Instant.parse(it) }.getOrNull() }
            ?: Clock.System.now(),
        updatedAt = updatedAt?.let { runCatching { Instant.parse(it) }.getOrNull() }
            ?: Clock.System.now(),
    )
}

fun RecipeSummaryResponseDto.toDomain(): Recipe =
    Recipe(
        id = id,
        name = name,
        category = category ?: "",
        price = price ?: 0.0,
        isActive = active,
        ingredientCount = ingredientCount,
        allergenCount = allergenCount,
    )

fun RecipeIngredientResponseDto.toDomain(): RecipeIngredient =
    RecipeIngredient(
        ingredientId = ingredientId,
        ingredientName = ingredientName,
        quantity = quantity ?: 0.0,
        unit = unit ?: "",
    )

@OptIn(kotlin.time.ExperimentalTime::class)
fun MenuDigitalCardResponseDto.toDomain(): MenuDigitalCard =
    MenuDigitalCard(
        id = id,
        menuId = menuId,
        dishId = dishId,
        dishName = dishName,
        createdAt = runCatching { Instant.parse(createdAt) }.getOrNull()
            ?: Clock.System.now(),
        updatedAt = runCatching { Instant.parse(updatedAt) }.getOrNull()
            ?: Clock.System.now(),
    )
