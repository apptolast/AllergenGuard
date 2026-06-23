package org.apptolast.menuadmin.presentation.screens.ingredients

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import menuadmin.adminapp.generated.resources.Res
import menuadmin.adminapp.generated.resources.action_back
import menuadmin.adminapp.generated.resources.action_clear
import menuadmin.adminapp.generated.resources.field_brand
import menuadmin.adminapp.generated.resources.field_description
import menuadmin.adminapp.generated.resources.ingredients_allergens_label
import menuadmin.adminapp.generated.resources.ingredients_analyze
import menuadmin.adminapp.generated.resources.ingredients_brand_placeholder
import menuadmin.adminapp.generated.resources.ingredients_clear_filters
import menuadmin.adminapp.generated.resources.ingredients_delete
import menuadmin.adminapp.generated.resources.ingredients_description_placeholder
import menuadmin.adminapp.generated.resources.ingredients_editor_subtitle
import menuadmin.adminapp.generated.resources.ingredients_name_placeholder
import menuadmin.adminapp.generated.resources.ingredients_new
import menuadmin.adminapp.generated.resources.ingredients_no_allergens
import menuadmin.adminapp.generated.resources.ingredients_paste_hint
import menuadmin.adminapp.generated.resources.ingredients_product_name
import menuadmin.adminapp.generated.resources.ingredients_save
import menuadmin.adminapp.generated.resources.ingredients_scan_label
import menuadmin.adminapp.generated.resources.ingredients_search
import menuadmin.adminapp.generated.resources.ingredients_subtitle
import menuadmin.adminapp.generated.resources.ingredients_title
import menuadmin.adminapp.generated.resources.ingredients_upload_photo
import org.apptolast.menuadmin.domain.model.AllergenType
import org.apptolast.menuadmin.domain.model.ContainmentLevel
import org.apptolast.menuadmin.domain.model.Ingredient
import org.apptolast.menuadmin.domain.model.IngredientAllergen
import org.apptolast.menuadmin.presentation.components.AllergenBadge
import org.apptolast.menuadmin.presentation.components.ErrorSnackbarEffect
import org.apptolast.menuadmin.presentation.components.SearchBar
import org.apptolast.menuadmin.presentation.screens.ingredients.components.AllergenSelector
import org.apptolast.menuadmin.presentation.theme.Blue500
import org.apptolast.menuadmin.presentation.theme.Blue600
import org.apptolast.menuadmin.presentation.theme.MenuAdminTheme
import org.apptolast.menuadmin.presentation.theme.Red500
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun IngredientsScreen(viewModel: IngredientsViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    IngredientsContent(
        uiState = uiState,
        onNewIngredient = viewModel::onNewIngredient,
        onDismissEditor = viewModel::onDismissEditor,
        onFormLabelInfoChange = viewModel::onFormLabelInfoChange,
        onFormNameChange = viewModel::onFormNameChange,
        onFormBrandChange = viewModel::onFormBrandChange,
        onFormDescriptionChange = viewModel::onFormDescriptionChange,
        onToggleAllergen = viewModel::onToggleAllergen,
        onSaveIngredient = viewModel::onSaveIngredient,
        onDeleteIngredient = viewModel::onDeleteIngredient,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onEditIngredient = viewModel::onEditIngredient,
        onToggleAllergenFilter = viewModel::onToggleAllergenFilter,
        onClearAllergenFilters = viewModel::onClearAllergenFilters,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IngredientsContent(
    uiState: IngredientsUiState,
    onNewIngredient: () -> Unit,
    onDismissEditor: () -> Unit,
    onFormLabelInfoChange: (String) -> Unit,
    onFormNameChange: (String) -> Unit,
    onFormBrandChange: (String) -> Unit,
    onFormDescriptionChange: (String) -> Unit,
    onToggleAllergen: (AllergenType) -> Unit,
    onSaveIngredient: () -> Unit,
    onDeleteIngredient: (String) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onEditIngredient: (Ingredient) -> Unit,
    onToggleAllergenFilter: (AllergenType) -> Unit,
    onClearAllergenFilters: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (uiState.isLoading) {
        CircularProgressIndicator()
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (uiState.isEditing) {
                    IconButton(onClick = onDismissEditor) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.action_back),
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
                Column {
                    Text(
                        text = stringResource(Res.string.ingredients_title),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = if (uiState.isEditing) {
                            stringResource(Res.string.ingredients_editor_subtitle)
                        } else {
                            stringResource(Res.string.ingredients_subtitle)
                        },
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            // Creating ingredients is an ACCOUNT_ADMIN-only action; managers get a read-only catalog.
            if (!uiState.isEditing && uiState.isAccountAdmin) {
                Button(
                    onClick = onNewIngredient,
                    colors = ButtonDefaults.buttonColors(containerColor = Blue500),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(Res.string.ingredients_new),
                        tint = Color.White,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(Res.string.ingredients_new),
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }

        if (uiState.isEditing) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Label Scanner Section (OCR)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(8.dp),
                        )
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp),
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = stringResource(Res.string.ingredients_scan_label),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Blue500,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.Top,
                        ) {
                            OutlinedTextField(
                                value = uiState.formLabelInfo,
                                onValueChange = onFormLabelInfoChange,
                                placeholder = {
                                    Text(stringResource(Res.string.ingredients_paste_hint))
                                },
                                modifier = Modifier.weight(1f).height(100.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Blue500,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                ),
                            )
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                OutlinedButton(
                                    onClick = { /* Upload photo */ },
                                    shape = RoundedCornerShape(8.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.CameraAlt,
                                        contentDescription = stringResource(Res.string.ingredients_upload_photo),
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(stringResource(Res.string.ingredients_upload_photo))
                                }
                                Button(
                                    onClick = { /* Analyze */ },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Blue600,
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Search,
                                        contentDescription = stringResource(Res.string.ingredients_analyze),
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(stringResource(Res.string.ingredients_analyze), color = Color.White)
                                }
                            }
                        }
                    }
                }

                // Form Fields - Name and Brand
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    OutlinedTextField(
                        value = uiState.formName,
                        onValueChange = onFormNameChange,
                        label = { Text(stringResource(Res.string.ingredients_product_name)) },
                        placeholder = { Text(stringResource(Res.string.ingredients_name_placeholder)) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Blue500,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        ),
                    )
                    OutlinedTextField(
                        value = uiState.formBrand,
                        onValueChange = onFormBrandChange,
                        label = { Text(stringResource(Res.string.field_brand)) },
                        placeholder = { Text(stringResource(Res.string.ingredients_brand_placeholder)) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Blue500,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        ),
                    )
                }

                // Description
                OutlinedTextField(
                    value = uiState.formDescription,
                    onValueChange = onFormDescriptionChange,
                    label = { Text(stringResource(Res.string.field_description)) },
                    placeholder = { Text(stringResource(Res.string.ingredients_description_placeholder)) },
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Blue500,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    ),
                )

                // Allergen Selector
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = stringResource(Res.string.ingredients_allergens_label),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    AllergenSelector(
                        allergens = uiState.formAllergens,
                        onToggle = onToggleAllergen,
                    )
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (uiState.editingIngredient != null) {
                        OutlinedButton(
                            onClick = {
                                onDeleteIngredient(uiState.editingIngredient.id)
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Red500,
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                Red500,
                            ),
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(Res.string.ingredients_delete),
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }
                    Button(
                        onClick = onSaveIngredient,
                        enabled = !uiState.isSaving && uiState.formName.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = Blue500),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = Color.White,
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Save,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(Res.string.ingredients_save),
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        } else {
            // Search bar
            SearchBar(
                query = uiState.searchQuery,
                onQueryChange = onSearchQueryChange,
                placeholder = stringResource(Res.string.ingredients_search),
            )

            // Allergen filter chips
            AllergenFilterBar(
                selectedFilters = uiState.filterAllergens,
                onToggleFilter = onToggleAllergenFilter,
                onClearFilters = onClearAllergenFilters,
            )

            // Grid of ingredient cards
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 280.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(
                    items = uiState.ingredients,
                    key = { it.id },
                ) { ingredient ->
                    IngredientCard(
                        ingredient = ingredient,
                        // Managers can browse but not open the editor (no write actions for them).
                        onClick = if (uiState.isAccountAdmin) {
                            { onEditIngredient(ingredient) }
                        } else {
                            null
                        },
                    )
                }
            }
        }

        // Errors surface through the app-wide snackbar instead of inline red text.
        ErrorSnackbarEffect(uiState.error)
    }
}

@Composable
private fun AllergenFilterBar(
    selectedFilters: Set<AllergenType>,
    onToggleFilter: (AllergenType) -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (selectedFilters.isNotEmpty()) {
            ElevatedAssistChip(
                onClick = onClearFilters,
                label = {
                    Text(
                        text = stringResource(Res.string.ingredients_clear_filters),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(Res.string.action_clear),
                        modifier = Modifier.size(16.dp),
                        tint = Red500,
                    )
                },
                colors = AssistChipDefaults.elevatedAssistChipColors(
                    labelColor = Red500,
                ),
            )
        }
        AllergenType.entries.forEach { allergen ->
            AllergenBadge(
                allergenType = allergen,
                isActive = allergen in selectedFilters,
                onClick = { onToggleFilter(allergen) },
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun IngredientCard(
    ingredient: Ingredient,
    // Null = read-only (no click): the card is not interactive (managers browse the catalog).
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)
    // Keep the containment level per allergen so the card distinguishes "contains" from "may contain".
    // Definite allergens (CONTAINS) are listed first. FREE_OF is not an allergen to display.
    val allergens = ingredient.allergens
        .filter { it.containmentLevel != ContainmentLevel.FREE_OF }
        .mapNotNull { ia -> AllergenType.fromApiCode(ia.allergenCode)?.let { it to ia.containmentLevel } }
        .sortedBy { it.second.ordinal }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 140.dp)
            .clip(shape)
            .border(width = 1.dp, color = MaterialTheme.colorScheme.outlineVariant, shape = shape)
            .background(MaterialTheme.colorScheme.surface)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = ingredient.name,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (ingredient.brand.isNotBlank()) {
            Text(
                text = ingredient.brand,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        if (allergens.isEmpty()) {
            Text(
                text = stringResource(Res.string.ingredients_no_allergens),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                allergens.forEach { (allergen, level) ->
                    AllergenBadge(
                        allergenType = allergen,
                        isActive = true,
                        compact = true,
                        containmentLevel = level,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun IngredientsContentPreview() {
    MenuAdminTheme {
        IngredientsContent(
            uiState = IngredientsUiState(
                isLoading = false,
                isAccountAdmin = true,
                ingredients = listOf(
                    Ingredient(
                        id = "1",
                        name = "Harina de trigo",
                        brand = "Harinera La Meta",
                        allergens = listOf(
                            IngredientAllergen(
                                allergenCode = "GLUTEN",
                                allergenName = "Gluten",
                            ),
                        ),
                    ),
                    Ingredient(
                        id = "2",
                        name = "Leche entera",
                        brand = "Central Lechera",
                        allergens = listOf(
                            IngredientAllergen(
                                allergenCode = "MILK",
                                allergenName = "Lacteos",
                            ),
                        ),
                    ),
                    Ingredient(
                        id = "3",
                        name = "Galletas de Canela",
                        allergens = listOf(
                            IngredientAllergen(allergenCode = "GLUTEN", allergenName = "Gluten"),
                            IngredientAllergen(allergenCode = "MILK", allergenName = "Lacteos"),
                            IngredientAllergen(
                                allergenCode = "SOYA",
                                allergenName = "Soja",
                                containmentLevel = ContainmentLevel.MAY_CONTAIN,
                            ),
                            IngredientAllergen(
                                allergenCode = "TREE_NUTS",
                                allergenName = "Frutos Secos",
                                containmentLevel = ContainmentLevel.MAY_CONTAIN,
                            ),
                        ),
                    ),
                    Ingredient(id = "4", name = "Sal"),
                ),
            ),
            onNewIngredient = {},
            onDismissEditor = {},
            onFormLabelInfoChange = {},
            onFormNameChange = {},
            onFormBrandChange = {},
            onFormDescriptionChange = {},
            onToggleAllergen = {},
            onSaveIngredient = {},
            onDeleteIngredient = {},
            onSearchQueryChange = {},
            onEditIngredient = {},
            onToggleAllergenFilter = {},
            onClearAllergenFilters = {},
        )
    }
}
