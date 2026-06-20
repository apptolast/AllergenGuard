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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apptolast.menufrontend.core.share.APP_SHARE_URL
import com.apptolast.menufrontend.core.share.rememberShareLauncher
import com.apptolast.menufrontend.core.theme.AllergenGuardTheme
import com.apptolast.menufrontend.core.theme.extendedColors
import com.apptolast.menufrontend.domain.model.Allergen
import com.apptolast.menufrontend.domain.model.Dish
import com.apptolast.menufrontend.features.components.allergenLabels
import com.apptolast.menufrontend.features.menu.components.AllergenFilterSection
import com.apptolast.menufrontend.features.menu.components.DishCard
import com.apptolast.menufrontend.features.menu.data.DishSortMode
import com.apptolast.menufrontend.features.menu.data.MenuAction
import com.apptolast.menufrontend.features.menu.data.MenuState
import com.apptolast.menufrontend.resources.Res
import com.apptolast.menufrontend.resources.back
import com.apptolast.menufrontend.resources.menu_category_other
import com.apptolast.menufrontend.resources.menu_favorite
import com.apptolast.menufrontend.resources.menu_interactive_menu
import com.apptolast.menufrontend.resources.menu_no_menu
import com.apptolast.menufrontend.resources.menu_safe_dishes
import com.apptolast.menufrontend.resources.menu_share
import com.apptolast.menufrontend.resources.menu_sort_category
import com.apptolast.menufrontend.resources.menu_sort_name
import com.apptolast.menufrontend.resources.menu_sort_price
import com.apptolast.menufrontend.resources.menu_toggle_unsafe
import com.apptolast.menufrontend.resources.share_restaurant_message
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
    val share = rememberShareLauncher()
    val shareMessage = stringResource(
        Res.string.share_restaurant_message,
        state.restaurantName,
        APP_SHARE_URL,
    )

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
        onShare = { share(shareMessage) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    state: MenuState,
    onAction: (MenuAction) -> Unit,
    onShare: () -> Unit,
) {
    val allergenLabels = allergenLabels()
    var filtersExpanded by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = state.restaurantName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = state.restaurantDescription.ifBlank {
                                stringResource(Res.string.menu_interactive_menu)
                            },
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
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
                actions = {
                    // Share this restaurant (opens the native share sheet).
                    IconButton(onClick = onShare) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = stringResource(Res.string.menu_share),
                        )
                    }
                    // Show/hide dishes the user can't eat (eye toggle, like a password field).
                    IconButton(onClick = { onAction(MenuAction.ToggleShowUnsafe) }) {
                        Icon(
                            imageVector = if (state.showUnsafe) {
                                Icons.Filled.Visibility
                            } else {
                                Icons.Filled.VisibilityOff
                            },
                            contentDescription = stringResource(Res.string.menu_toggle_unsafe),
                        )
                    }
                    // Mark this restaurant as a favorite.
                    IconButton(onClick = { onAction(MenuAction.ToggleFavorite) }) {
                        Icon(
                            imageVector = if (state.isFavorite) {
                                Icons.Filled.Favorite
                            } else {
                                Icons.Filled.FavoriteBorder
                            },
                            contentDescription = stringResource(Res.string.menu_favorite),
                            tint = if (state.isFavorite) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
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
        } else if (state.allDishes.isEmpty()) {
            // No active (published) menu for this restaurant → nothing to show.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(Res.string.menu_no_menu),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            val otherLabel = stringResource(Res.string.menu_category_other)
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

                // Safe dishes header + sort control
                item {
                    Spacer(Modifier.height(4.dp))
                    SafeDishesHeader(
                        count = state.filteredDishes.size,
                        sortMode = state.sortMode,
                        sortAscending = state.sortAscending,
                        onAction = onAction,
                        modifier = Modifier.padding(horizontal = 20.dp),
                    )
                }

                if (state.sortMode == DishSortMode.CATEGORY) {
                    state.filteredDishes.groupedByCategory(state.sortAscending, otherLabel)
                        .forEach { (category, dishes) ->
                            item(key = "cat-$category") {
                                Text(
                                    text = category,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                                )
                            }
                            items(items = dishes, key = { it.id }) { dish ->
                                DishCard(
                                    dish = dish,
                                    userAllergens = state.userAllergens,
                                    allergenLabels = allergenLabels,
                                    onClick = { onAction(MenuAction.DishClicked(dish.id)) },
                                    modifier = Modifier.padding(horizontal = 20.dp),
                                )
                            }
                        }
                } else {
                    items(
                        items = state.filteredDishes.sortedForMenu(state.sortMode, state.sortAscending),
                        key = { it.id },
                    ) { dish ->
                        DishCard(
                            dish = dish,
                            userAllergens = state.activeFilters,
                            allergenLabels = allergenLabels,
                            onClick = { onAction(MenuAction.DishClicked(dish.id)) },
                            modifier = Modifier.padding(horizontal = 20.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SafeDishesHeader(
    count: Int,
    sortMode: DishSortMode,
    sortAscending: Boolean,
    onAction: (MenuAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.extendedColors
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = stringResource(Res.string.menu_safe_dishes),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.safe,
            )
            Text(
                text = "$count platos",
                style = MaterialTheme.typography.bodySmall,
                color = colors.safe,
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            var expanded by remember { mutableStateOf(false) }
            Box {
                TextButton(onClick = { expanded = true }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Sort,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = sortModeLabel(sortMode),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DishSortMode.entries.forEach { mode ->
                        DropdownMenuItem(
                            text = { Text(sortModeLabel(mode)) },
                            onClick = {
                                onAction(MenuAction.SetSortMode(mode))
                                expanded = false
                            },
                        )
                    }
                }
            }
            IconButton(onClick = { onAction(MenuAction.ToggleSortDirection) }) {
                Icon(
                    imageVector = if (sortAscending) {
                        Icons.Filled.KeyboardArrowUp
                    } else {
                        Icons.Filled.KeyboardArrowDown
                    },
                    contentDescription = null,
                )
            }
        }
    }
}

@Composable
private fun sortModeLabel(mode: DishSortMode): String = when (mode) {
    DishSortMode.NAME -> stringResource(Res.string.menu_sort_name)
    DishSortMode.PRICE -> stringResource(Res.string.menu_sort_price)
    DishSortMode.CATEGORY -> stringResource(Res.string.menu_sort_category)
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
            onShare = {},
        )
    }
}

