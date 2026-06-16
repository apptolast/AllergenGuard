package com.apptolast.menufrontend.features.menu.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apptolast.menufrontend.core.theme.AllergenGuardTheme
import com.apptolast.menufrontend.core.theme.extendedColors
import com.apptolast.menufrontend.domain.model.Allergen
import com.apptolast.menufrontend.domain.model.Dish
import com.apptolast.menufrontend.features.components.allergenLabels
import com.apptolast.menufrontend.features.menu.components.AllergenFilterSection
import com.apptolast.menufrontend.features.menu.components.DishCard
import com.apptolast.menufrontend.features.menu.data.MenuAction
import com.apptolast.menufrontend.features.menu.data.MenuState
import com.apptolast.menufrontend.resources.Res
import com.apptolast.menufrontend.resources.back
import com.apptolast.menufrontend.resources.menu_interactive_menu
import com.apptolast.menufrontend.resources.menu_safe_dishes
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MenuScreenRoot(
    restaurantId: String,
    onDishClick: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: MenuViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(restaurantId) {
        viewModel.loadMenu(restaurantId)
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is MenuEffect.NavigateToDishDetail -> onDishClick(effect.dishId)
                MenuEffect.NavigateBack -> onNavigateBack()
            }
        }
    }

    MenuScreen(
        state = state,
        onAction = viewModel::onAction,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    state: MenuState,
    onAction: (MenuAction) -> Unit,
) {
    val allergenLabels = allergenLabels()
    val colors = MaterialTheme.extendedColors
    var filtersExpanded by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = state.restaurantName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = stringResource(Res.string.menu_interactive_menu),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { onAction(MenuAction.NavigateBack) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.back),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { padding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Allergen filter section (collapsible)
                item {
                    AllergenFilterSection(
                        allergenLabels = allergenLabels,
                        activeFilters = state.activeFilters,
                        userAllergens = state.userAllergens,
                        expanded = filtersExpanded,
                        onToggleAllergen = { onAction(MenuAction.ToggleAllergenFilter(it)) },
                        onToggleExpanded = { filtersExpanded = !filtersExpanded },
                        onRestoreFilters = { onAction(MenuAction.RestoreUserFilters) },
                        modifier = Modifier.padding(horizontal = 20.dp),
                    )
                }

                // Safe dishes header
                item {
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(Res.string.menu_safe_dishes),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.safe,
                        )
                        Text(
                            text = "${state.filteredDishes.size} platos",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.safe,
                        )
                    }
                }

                // Dish list
                items(
                    items = state.filteredDishes,
                    key = { it.id },
                ) { dish ->
                    DishCard(
                        dish = dish,
                        userAllergens = state.userAllergens,
                        allergenLabels = allergenLabels,
                        onClick = { onAction(MenuAction.DishClicked(dish.id)) },
                        modifier = Modifier.padding(horizontal = 20.dp),
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun PreviewMenuScreen() {
    val dishes = listOf(
        Dish(
            id = "1",
            restaurantId = "r1",
            name = "Ensalada de quinoa",
            description = "Quinoa, aguacate y vinagreta cítrica",
            price = 9.5,
            ingredients = listOf("Quinoa", "Aguacate"),
            allergens = setOf(Allergen.SULFITES),
        ),
        Dish(
            id = "2",
            restaurantId = "r1",
            name = "Pulpo a la brasa",
            description = "Pulpo con puré de patata ahumado",
            price = 18.0,
            ingredients = listOf("Pulpo", "Patata"),
            allergens = setOf(Allergen.MOLLUSKS),
        ),
    )
    AllergenGuardTheme {
        MenuScreen(
            state = MenuState(
                restaurantName = "Hotel Valsequillo",
                allDishes = dishes,
                filteredDishes = dishes,
                activeFilters = setOf(Allergen.GLUTEN, Allergen.FISH, Allergen.SOY),
                userAllergens = setOf(Allergen.GLUTEN, Allergen.FISH, Allergen.SOY),
            ),
            onAction = {},
        )
    }
}

