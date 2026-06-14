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
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apptolast.menufrontend.core.theme.SafeGreen
import com.apptolast.menufrontend.domain.model.Allergen
import com.apptolast.menufrontend.features.menu.components.AllergenFilterChip
import com.apptolast.menufrontend.features.menu.components.DishCard
import com.apptolast.menufrontend.features.menu.data.MenuAction
import com.apptolast.menufrontend.features.menu.data.MenuState
import com.apptolast.menufrontend.resources.Res
import com.apptolast.menufrontend.resources.allergen_celery
import com.apptolast.menufrontend.resources.allergen_crustaceans
import com.apptolast.menufrontend.resources.allergen_dairy
import com.apptolast.menufrontend.resources.allergen_eggs
import com.apptolast.menufrontend.resources.allergen_fish
import com.apptolast.menufrontend.resources.allergen_gluten
import com.apptolast.menufrontend.resources.allergen_lupin
import com.apptolast.menufrontend.resources.allergen_mollusks
import com.apptolast.menufrontend.resources.allergen_mustard
import com.apptolast.menufrontend.resources.allergen_peanuts
import com.apptolast.menufrontend.resources.allergen_sesame
import com.apptolast.menufrontend.resources.allergen_soy
import com.apptolast.menufrontend.resources.allergen_sulfites
import com.apptolast.menufrontend.resources.allergen_tree_nuts
import com.apptolast.menufrontend.resources.back
import com.apptolast.menufrontend.resources.menu_interactive_menu
import com.apptolast.menufrontend.resources.menu_safe_dishes
import com.apptolast.menufrontend.resources.menu_select_allergies
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
    val allergenLabels = allergenLabelMap()

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
                // Allergen filter section
                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = stringResource(Res.string.menu_select_allergies),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground,
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(Allergen.entries.take(8)) { allergen ->
                                AllergenFilterChip(
                                    allergen = allergen,
                                    label = allergenLabels[allergen] ?: allergen.name,
                                    isSelected = allergen in state.activeFilters,
                                    onToggle = { onAction(MenuAction.ToggleAllergenFilter(allergen)) },
                                )
                            }
                        }
                    }
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
                            color = SafeGreen,
                        )
                        Text(
                            text = "${state.filteredDishes.size} platos",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SafeGreen,
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

@Composable
private fun allergenLabelMap(): Map<Allergen, String> = mapOf(
    Allergen.GLUTEN to stringResource(Res.string.allergen_gluten),
    Allergen.FISH to stringResource(Res.string.allergen_fish),
    Allergen.PEANUTS to stringResource(Res.string.allergen_peanuts),
    Allergen.DAIRY to stringResource(Res.string.allergen_dairy),
    Allergen.EGGS to stringResource(Res.string.allergen_eggs),
    Allergen.SOY to stringResource(Res.string.allergen_soy),
    Allergen.SULFITES to stringResource(Res.string.allergen_sulfites),
    Allergen.MOLLUSKS to stringResource(Res.string.allergen_mollusks),
    Allergen.CRUSTACEANS to stringResource(Res.string.allergen_crustaceans),
    Allergen.TREE_NUTS to stringResource(Res.string.allergen_tree_nuts),
    Allergen.CELERY to stringResource(Res.string.allergen_celery),
    Allergen.MUSTARD to stringResource(Res.string.allergen_mustard),
    Allergen.SESAME to stringResource(Res.string.allergen_sesame),
    Allergen.LUPIN to stringResource(Res.string.allergen_lupin),
)
