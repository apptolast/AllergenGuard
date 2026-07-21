package org.apptolast.menuadmin.presentation.screens.recipes.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import menuadmin.adminapp.generated.resources.Res
import menuadmin.adminapp.generated.resources.recipecard_allergens_count
import menuadmin.adminapp.generated.resources.recipecard_ingredients_count
import menuadmin.adminapp.generated.resources.recipecard_subrecipe_label
import org.apptolast.menuadmin.domain.model.AllergenType
import org.apptolast.menuadmin.domain.model.Ingredient
import org.apptolast.menuadmin.domain.model.IngredientAllergen
import org.apptolast.menuadmin.domain.model.Recipe
import org.apptolast.menuadmin.domain.model.RecipeComponentType
import org.apptolast.menuadmin.domain.model.RecipeIngredient
import org.apptolast.menuadmin.presentation.components.AllergenBadge
import org.apptolast.menuadmin.presentation.components.iconResource
import org.apptolast.menuadmin.presentation.theme.Blue500
import org.apptolast.menuadmin.presentation.theme.MenuAdminTheme
import org.apptolast.menuadmin.presentation.theme.color
import org.jetbrains.compose.resources.imageResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecipeCard(
    recipe: Recipe,
    ingredientLookup: Map<String, Ingredient>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    // Recipes by id, so SUB_RECIPE components can be expanded to show their own ingredients (Spec 003).
    recipeLookup: Map<String, Recipe> = emptyMap(),
) {
    val shape = RoundedCornerShape(12.dp)

    Column(
        modifier = modifier
            .clip(shape)
            .border(width = 1.dp, color = MaterialTheme.colorScheme.outlineVariant, shape = shape)
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick),
    ) {
        // === Top section: Name + Category + Allergen badges ===
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Name + Category badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = recipe.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                    lineHeight = 20.sp,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (recipe.price > 0) {
                        val priceInt = recipe.price.toInt()
                        val priceDec = ((recipe.price * 100).toInt() % 100)
                            .toString().padStart(2, '0')
                        Text(
                            text = "$priceInt.${priceDec}\u20AC",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(start = 12.dp),
                        )
                    }
                    if (recipe.category.isNotBlank()) {
                        Text(
                            text = recipe.category,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Blue500,
                        )
                    }
                }
            }

            // Allergen badges
            if (recipe.computedAllergens.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    recipe.computedAllergens.forEach { allergen ->
                        AllergenBadge(
                            allergenType = allergen,
                            isActive = true,
                        )
                    }
                }
            } else if (recipe.allergenCount > 0) {
                Text(
                    text = stringResource(Res.string.recipecard_allergens_count, recipe.allergenCount),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Blue500,
                )
            }
        }

        // === Bottom section: Ingredient breakdown ===
        if (recipe.ingredients.isNotEmpty()) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Section header
                Text(
                    text = stringResource(Res.string.recipecard_ingredients_count, recipe.ingredients.size),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                // Ingredient rows — sub-recipes render as expandable rows showing their own components.
                recipe.ingredients.forEach { recipeIngredient ->
                    if (recipeIngredient.type == RecipeComponentType.SUB_RECIPE) {
                        val sub = recipeLookup[recipeIngredient.ingredientId]
                        SubRecipeRow(
                            name = recipeIngredient.ingredientName,
                            allergenTypes = sub?.computedAllergens ?: emptySet(),
                            components = sub?.ingredients ?: emptyList(),
                            ingredientLookup = ingredientLookup,
                        )
                    } else {
                        val ingredient = ingredientLookup[recipeIngredient.ingredientId]
                        IngredientRow(
                            name = recipeIngredient.ingredientName,
                            allergenTypes = ingredient?.allergenTypes ?: emptySet(),
                        )
                    }
                }
            }
        } else if (recipe.ingredientCount > 0) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                Text(
                    text = stringResource(Res.string.recipecard_ingredients_count, recipe.ingredientCount),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SubRecipeRow(
    name: String,
    allergenTypes: Set<AllergenType>,
    components: List<RecipeIngredient>,
    ingredientLookup: Map<String, Ingredient>,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = Blue500,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 4.dp),
                )
                Text(
                    text = stringResource(Res.string.recipecard_subrecipe_label),
                    fontSize = 10.sp,
                    color = Blue500,
                    modifier = Modifier.padding(start = 6.dp),
                )
            }
            if (allergenTypes.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(start = 12.dp),
                ) {
                    allergenTypes.forEach { allergen ->
                        Image(
                            bitmap = imageResource(allergen.iconResource()),
                            contentDescription = null,
                            modifier = Modifier.size(30.dp),
                            filterQuality = FilterQuality.High,
                        )
                    }
                }
            }
        }
        if (expanded && components.isNotEmpty()) {
            Column(modifier = Modifier.padding(start = 20.dp)) {
                components.forEach { component ->
                    val ing = ingredientLookup[component.ingredientId]
                    IngredientRow(
                        name = component.ingredientName,
                        allergenTypes = ing?.allergenTypes ?: emptySet(),
                    )
                }
            }
        }
    }
}

@Composable
private fun IngredientRow(
    name: String,
    allergenTypes: Set<AllergenType>,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = name,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (allergenTypes.isNotEmpty()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(start = 12.dp),
            ) {
                allergenTypes.forEach { allergen ->
                    Image(
                        bitmap = imageResource(allergen.iconResource()),
                        contentDescription = null,
                        modifier = Modifier.size(30.dp),
                        filterQuality = FilterQuality.High,
                    )
                }
            }
        } else {
            Text(
                text = "–",
                fontSize = 14.sp,
                color = MenuAdminTheme.colors.textMuted,
                modifier = Modifier.padding(start = 12.dp),
            )
        }
    }
}

@Preview
@Composable
private fun PreviewRecipeCard() {
    MenuAdminTheme {
        RecipeCard(
            recipe = Recipe(
                id = "rec-1",
                name = "Croquetas Ibericas del Puchero",
                category = "Principales",
                price = 12.50,
                isActive = true,
                ingredientCount = 2,
                allergenCount = 2,
                computedAllergens = setOf(AllergenType.DAIRY, AllergenType.GLUTEN),
                ingredients = listOf(
                    RecipeIngredient("ing-1", "Leche entera", 500.0, "ml"),
                    RecipeIngredient("ing-2", "Pan rallado", 200.0, "g"),
                ),
            ),
            ingredientLookup = mapOf(
                "ing-1" to Ingredient(
                    id = "ing-1",
                    name = "Leche entera",
                    allergens = listOf(
                        IngredientAllergen(allergenCode = "MILK", allergenName = "Lacteos"),
                    ),
                ),
                "ing-2" to Ingredient(
                    id = "ing-2",
                    name = "Pan rallado",
                    allergens = listOf(
                        IngredientAllergen(allergenCode = "GLUTEN", allergenName = "Gluten"),
                    ),
                ),
            ),
            onClick = {},
        )
    }
}

@Preview
@Composable
private fun PreviewRecipeCardWithSubRecipe() {
    MenuAdminTheme {
        RecipeCard(
            recipe = Recipe(
                id = "rec-sub-parent",
                name = "Patatas Bravas Piconera",
                category = "Tapas",
                isActive = true,
                computedAllergens = setOf(AllergenType.EGGS),
                ingredients = listOf(
                    RecipeIngredient("ing-p", "Patata"),
                    RecipeIngredient(
                        ingredientId = "sub-may",
                        ingredientName = "Mayonesa de ajo frito y oloroso",
                        type = RecipeComponentType.SUB_RECIPE,
                    ),
                ),
            ),
            ingredientLookup = mapOf(
                "ing-p" to Ingredient(id = "ing-p", name = "Patata"),
                "ing-huevo" to Ingredient(
                    id = "ing-huevo",
                    name = "Huevo",
                    allergens = listOf(IngredientAllergen(allergenCode = "EGGS", allergenName = "Huevos")),
                ),
            ),
            onClick = {},
            recipeLookup = mapOf(
                "sub-may" to Recipe(
                    id = "sub-may",
                    name = "Mayonesa de ajo frito y oloroso",
                    isSubRecipe = true,
                    computedAllergens = setOf(AllergenType.EGGS),
                    ingredients = listOf(RecipeIngredient("ing-huevo", "Huevo")),
                ),
            ),
        )
    }
}

@Preview
@Composable
private fun PreviewRecipeCardWithManyAllergens() {
    MenuAdminTheme {
        RecipeCard(
            recipe = Recipe(
                id = "rec-2",
                name = "Butifarra Cordobesa a la Brasa",
                category = "Principales",
                price = 16.00,
                isActive = true,
                ingredientCount = 3,
                allergenCount = 2,
                computedAllergens = setOf(AllergenType.EGGS, AllergenType.MUSTARD),
                ingredients = listOf(
                    RecipeIngredient("ing-3", "Huevos frescos", 3.0, "ud"),
                    RecipeIngredient("ing-4", "Butifarra", 400.0, "g"),
                    RecipeIngredient("ing-5", "Mostaza Dijon", 20.0, "g"),
                ),
            ),
            ingredientLookup = mapOf(
                "ing-3" to Ingredient(
                    id = "ing-3",
                    name = "Huevos frescos",
                    allergens = listOf(
                        IngredientAllergen(allergenCode = "EGGS", allergenName = "Huevos"),
                    ),
                ),
                "ing-4" to Ingredient(id = "ing-4", name = "Butifarra"),
                "ing-5" to Ingredient(
                    id = "ing-5",
                    name = "Mostaza Dijon",
                    allergens = listOf(
                        IngredientAllergen(allergenCode = "MUSTARD", allergenName = "Mostaza"),
                    ),
                ),
            ),
            onClick = {},
        )
    }
}

@Preview
@Composable
private fun PreviewRecipeCardNoIngredients() {
    MenuAdminTheme {
        RecipeCard(
            recipe = Recipe(
                id = "rec-3",
                name = "Ensaladilla Ekaterina",
                category = "Guarnicion",
                isActive = true,
                ingredientCount = 0,
                allergenCount = 0,
            ),
            ingredientLookup = emptyMap(),
            onClick = {},
        )
    }
}

@Preview
@Composable
private fun PreviewRecipeCardSummaryOnly() {
    MenuAdminTheme {
        RecipeCard(
            recipe = Recipe(
                id = "rec-4",
                name = "Arroz Caldoso Marinero",
                category = "Arroces",
                isActive = true,
                ingredientCount = 4,
                allergenCount = 3,
            ),
            ingredientLookup = emptyMap(),
            onClick = {},
        )
    }
}
