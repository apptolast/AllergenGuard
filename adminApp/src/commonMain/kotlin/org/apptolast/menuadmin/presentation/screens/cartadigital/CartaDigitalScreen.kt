package org.apptolast.menuadmin.presentation.screens.cartadigital

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import menuadmin.adminapp.generated.resources.Res
import menuadmin.adminapp.generated.resources.carta_all_dishes
import menuadmin.adminapp.generated.resources.carta_clear
import menuadmin.adminapp.generated.resources.carta_empty_no_recipes
import menuadmin.adminapp.generated.resources.carta_question
import menuadmin.adminapp.generated.resources.carta_safe_count
import menuadmin.adminapp.generated.resources.carta_safe_dishes
import menuadmin.adminapp.generated.resources.carta_section_count
import menuadmin.adminapp.generated.resources.carta_subtitle
import menuadmin.adminapp.generated.resources.carta_title
import menuadmin.adminapp.generated.resources.carta_unsafe_dishes
import org.apptolast.menuadmin.domain.model.AllergenType
import org.apptolast.menuadmin.domain.model.Recipe
import org.apptolast.menuadmin.presentation.components.AllergenBadge
import org.apptolast.menuadmin.presentation.components.ErrorSnackbarEffect
import org.apptolast.menuadmin.presentation.theme.Blue500
import org.apptolast.menuadmin.presentation.theme.Green100
import org.apptolast.menuadmin.presentation.theme.Green500
import org.apptolast.menuadmin.presentation.theme.MenuAdminTheme
import org.apptolast.menuadmin.presentation.theme.Red100
import org.apptolast.menuadmin.presentation.theme.Red500
import org.jetbrains.compose.resources.stringResource

@Composable
fun CartaDigitalScreen(viewModel: CartaDigitalViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    CartaDigitalContent(
        uiState = uiState,
        onSelectMenu = viewModel::onSelectMenu,
        onToggleAllergen = viewModel::onToggleAllergen,
        onClearAllergens = viewModel::onClearAllergens,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CartaDigitalContent(
    uiState: CartaDigitalUiState,
    onSelectMenu: (String) -> Unit,
    onToggleAllergen: (AllergenType) -> Unit,
    onClearAllergens: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (uiState.isLoading) {
        CircularProgressIndicator()
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // Header
        Column {
            Text(
                text = stringResource(Res.string.carta_title),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(Res.string.carta_subtitle),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Menu Selector
        if (uiState.availableMenus.size > 1) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                uiState.availableMenus.forEach { menu ->
                    FilterChip(
                        selected = menu.id == uiState.selectedMenuId,
                        onClick = { onSelectMenu(menu.id) },
                        label = {
                            Text(
                                text = menu.name,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Blue500,
                            selectedLabelColor = Color.White,
                            labelColor = MaterialTheme.colorScheme.onSurface,
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = MaterialTheme.colorScheme.outlineVariant,
                            selectedBorderColor = Blue500,
                            enabled = true,
                            selected = menu.id == uiState.selectedMenuId,
                        ),
                    )
                }
            }
        }

        // Allergen Selector
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.carta_question),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (uiState.selectedAllergens.isNotEmpty()) {
                    TextButton(onClick = { onClearAllergens() }) {
                        Text(
                            text = stringResource(Res.string.carta_clear),
                            fontSize = 13.sp,
                            color = Blue500,
                        )
                    }
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AllergenType.entries.forEach { allergen ->
                    AllergenBadge(
                        allergenType = allergen,
                        isActive = allergen in uiState.selectedAllergens,
                        onClick = { onToggleAllergen(allergen) },
                    )
                }
            }
        }

        // Results counter
        if (uiState.selectedAllergens.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MenuAdminTheme.colors.successContainer)
                    .padding(16.dp),
            ) {
                Text(
                    text = stringResource(
                        Res.string.carta_safe_count,
                        uiState.safeRecipes.size,
                        uiState.allRecipes.size,
                    ),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MenuAdminTheme.colors.success,
                )
            }
        }

        // Safe Recipes
        if (uiState.selectedAllergens.isNotEmpty() && uiState.safeRecipes.isNotEmpty()) {
            RecipeSection(
                title = stringResource(Res.string.carta_safe_dishes),
                titleColor = Green500,
                borderColor = Green500,
                bgColor = Green100,
                recipes = uiState.safeRecipes,
                userAllergens = uiState.selectedAllergens,
            )
        }

        // Unsafe Recipes
        if (uiState.selectedAllergens.isNotEmpty() && uiState.unsafeRecipes.isNotEmpty()) {
            RecipeSection(
                title = stringResource(Res.string.carta_unsafe_dishes),
                titleColor = Red500,
                borderColor = Red500,
                bgColor = Red100,
                recipes = uiState.unsafeRecipes,
                userAllergens = uiState.selectedAllergens,
            )
        }

        // Show all recipes when no allergens selected
        if (uiState.selectedAllergens.isEmpty() && uiState.allRecipes.isNotEmpty()) {
            Text(
                text = stringResource(Res.string.carta_all_dishes, uiState.allRecipes.size),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            uiState.allRecipes.forEach { recipe ->
                RecipeCard(
                    recipe = recipe,
                    userAllergens = emptySet(),
                    accentColor = Blue500,
                )
            }
        }

        // Empty state
        if (uiState.allRecipes.isEmpty() && !uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Blue500.copy(alpha = 0.05f))
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(Res.string.carta_empty_no_recipes),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // Errors surface through the app-wide snackbar instead of inline red text.
        ErrorSnackbarEffect(uiState.error)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RecipeSection(
    title: String,
    titleColor: Color,
    borderColor: Color,
    bgColor: Color,
    recipes: List<Recipe>,
    userAllergens: Set<AllergenType>,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(Res.string.carta_section_count, title, recipes.size),
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = titleColor,
        )
        recipes.forEach { recipe ->
            RecipeCard(
                recipe = recipe,
                userAllergens = userAllergens,
                accentColor = borderColor,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RecipeCard(
    recipe: Recipe,
    userAllergens: Set<AllergenType>,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(width = 1.dp, color = MaterialTheme.colorScheme.outlineVariant, shape = RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface),
    ) {
        // Left accent bar
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(120.dp)
                .background(accentColor),
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = recipe.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (recipe.category.isNotBlank()) {
                        Text(
                            text = recipe.category,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                if (recipe.price > 0) {
                    val priceInt = recipe.price.toInt()
                    val priceDec = ((recipe.price * 100).toInt() % 100)
                        .toString().padStart(2, '0')
                    Text(
                        text = "$priceInt.${priceDec}\u20AC",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            if (recipe.description.isNotBlank()) {
                Text(
                    text = recipe.description,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                )
            }

            if (recipe.computedAllergens.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    recipe.computedAllergens.forEach { allergen ->
                        val isConflict = allergen in userAllergens
                        AllergenBadge(
                            allergenType = allergen,
                            isActive = isConflict,
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun CartaDigitalContentPreview() {
    MenuAdminTheme {
        CartaDigitalContent(
            uiState = CartaDigitalUiState(
                isLoading = false,
                selectedAllergens = setOf(AllergenType.GLUTEN),
                allRecipes = listOf(
                    Recipe(
                        id = "r1",
                        name = "Butifarra a la Brasa",
                        description = "Butifarra artesana",
                        price = 14.50,
                        category = "Combinados",
                        computedAllergens = emptySet(),
                    ),
                    Recipe(
                        id = "r2",
                        name = "Croquetas Ibericas",
                        description = "Croquetas cremosas",
                        price = 12.50,
                        category = "Entrantes",
                        computedAllergens = setOf(AllergenType.GLUTEN, AllergenType.DAIRY),
                    ),
                ),
                safeRecipes = listOf(
                    Recipe(
                        id = "r1",
                        name = "Butifarra a la Brasa",
                        description = "Butifarra artesana",
                        price = 14.50,
                        category = "Combinados",
                        computedAllergens = emptySet(),
                    ),
                ),
                unsafeRecipes = listOf(
                    Recipe(
                        id = "r2",
                        name = "Croquetas Ibericas",
                        description = "Croquetas cremosas",
                        price = 12.50,
                        category = "Entrantes",
                        computedAllergens = setOf(AllergenType.GLUTEN, AllergenType.DAIRY),
                    ),
                ),
            ),
            onSelectMenu = {},
            onToggleAllergen = {},
            onClearAllergens = {},
        )
    }
}
