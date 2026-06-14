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
import org.apptolast.menuadmin.domain.model.AllergenType
import org.apptolast.menuadmin.domain.model.Ingredient
import org.apptolast.menuadmin.domain.model.IngredientAllergen
import org.apptolast.menuadmin.presentation.components.AllergenBadge
import org.apptolast.menuadmin.presentation.components.SearchBar
import org.apptolast.menuadmin.presentation.screens.ingredients.components.AllergenSelector
import org.apptolast.menuadmin.presentation.theme.Blue500
import org.apptolast.menuadmin.presentation.theme.Blue600
import org.apptolast.menuadmin.presentation.theme.MenuAdminTheme
import org.apptolast.menuadmin.presentation.theme.Red500
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
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
                Column {
                    Text(
                        text = "Maestro de Ingredientes",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = if (uiState.isEditing) {
                            "Analiza etiquetas y registra materias primas"
                        } else {
                            "Gestiona los ingredientes y sus alergenos"
                        },
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (!uiState.isEditing) {
                Button(
                    onClick = onNewIngredient,
                    colors = ButtonDefaults.buttonColors(containerColor = Blue500),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Nuevo",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Nuevo Ingrediente",
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
                            text = "Escanear Etiqueta (OCR) o Pegar Texto",
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
                                    Text("Sube una foto o pega aqui el texto de la etiqueta..")
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
                                        contentDescription = "Subir Foto",
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Subir Foto")
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
                                        contentDescription = "Analizar",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Analizar", color = Color.White)
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
                        label = { Text("Nombre del Producto") },
                        placeholder = { Text("Ej. Salsa de Soja Kikkoman") },
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
                        label = { Text("Marca") },
                        placeholder = { Text("Ej. Kikkoman") },
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
                    label = { Text("Descripcion") },
                    placeholder = { Text("Descripcion del ingrediente...") },
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
                        text = "Alergenos (Click: Contiene / Puede contener / Desactivar)",
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
                                text = "Eliminar",
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
                            text = "Guardar Ingrediente",
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
                placeholder = "Buscar ingredientes...",
            )

            // Allergen filter chips
            AllergenFilterBar(
                selectedFilters = uiState.filterAllergens,
                onToggleFilter = onToggleAllergenFilter,
                onClearFilters = onClearAllergenFilters,
            )

            // Grid of ingredient cards
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 220.dp),
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
                        onClick = { onEditIngredient(ingredient) },
                    )
                }
            }
        }

        // Error display
        uiState.error?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
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
                        text = "Limpiar filtros",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Limpiar",
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
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier = modifier
            .clip(shape)
            .border(width = 1.dp, color = MaterialTheme.colorScheme.outlineVariant, shape = shape)
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .defaultMinSize(minHeight = 120.dp)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = ingredient.name,
            fontSize = 15.sp,
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
        if (ingredient.allergenTypes.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                ingredient.allergenTypes.forEach { allergen ->
                    AllergenBadge(
                        allergenType = allergen,
                        isActive = true,
                    )
                }
            }
        } else {
            Text(
                text = "Sin alergenos",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
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
                                allergenCode = "DAIRY",
                                allergenName = "Lacteos",
                            ),
                        ),
                    ),
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
