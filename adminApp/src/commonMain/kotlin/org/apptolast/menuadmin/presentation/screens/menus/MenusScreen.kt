package org.apptolast.menuadmin.presentation.screens.menus

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import org.apptolast.menuadmin.domain.model.Dish
import org.apptolast.menuadmin.domain.model.Menu
import org.apptolast.menuadmin.domain.model.Recipe
import org.apptolast.menuadmin.presentation.components.ConfirmDialog
import org.apptolast.menuadmin.presentation.screens.menus.components.AllergenMatrixTable
import org.apptolast.menuadmin.presentation.screens.menus.components.AllergenTableLegend
import org.apptolast.menuadmin.presentation.screens.menus.components.CategoryFilterRow
import org.apptolast.menuadmin.presentation.theme.Blue500
import org.apptolast.menuadmin.presentation.theme.Green500
import org.apptolast.menuadmin.presentation.theme.MenuAdminTheme
import org.apptolast.menuadmin.presentation.theme.Red500
import kotlin.time.Clock

@Composable
fun MenusScreen(viewModel: MenusViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    MenusContent(
        uiState = uiState,
        onSelectMenu = viewModel::selectMenu,
        onBack = viewModel::clearMenuSelection,
        onFilterCategory = viewModel::filterByCategory,
        onExportPdf = viewModel::exportPdf,
        onNewMenu = viewModel::onNewMenu,
        onEditMenu = viewModel::onEditMenu,
        onRequestDeleteMenu = viewModel::onRequestDeleteMenu,
        onConfirmDeleteMenu = viewModel::onConfirmDeleteMenu,
        onDismissDeleteDialog = viewModel::onDismissDeleteDialog,
        onFormNameChange = viewModel::onFormNameChange,
        onFormDescriptionChange = viewModel::onFormDescriptionChange,
        onFormRestaurantLogoUrlChange = viewModel::onFormRestaurantLogoUrlChange,
        onFormCompanyLogoUrlChange = viewModel::onFormCompanyLogoUrlChange,
        onToggleRecipeSelection = viewModel::onToggleRecipeSelection,
        onDismissForm = viewModel::onDismissForm,
        onSaveMenu = viewModel::onSaveMenu,
    )
}

@Composable
fun MenusContent(
    uiState: MenusUiState,
    onSelectMenu: (Menu) -> Unit,
    onBack: () -> Unit,
    onFilterCategory: (String?) -> Unit,
    onExportPdf: () -> Unit,
    onNewMenu: () -> Unit,
    onEditMenu: (Menu) -> Unit,
    onRequestDeleteMenu: (Menu) -> Unit,
    onConfirmDeleteMenu: () -> Unit,
    onDismissDeleteDialog: () -> Unit,
    onFormNameChange: (String) -> Unit,
    onFormDescriptionChange: (String) -> Unit,
    onFormRestaurantLogoUrlChange: (String) -> Unit,
    onFormCompanyLogoUrlChange: (String) -> Unit,
    onToggleRecipeSelection: (String) -> Unit,
    onDismissForm: () -> Unit,
    onSaveMenu: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (uiState.isLoading) {
        CircularProgressIndicator()
        return
    }

    when {
        uiState.isFormVisible -> MenuEditorForm(
            uiState = uiState,
            onFormNameChange = onFormNameChange,
            onFormDescriptionChange = onFormDescriptionChange,
            onFormRestaurantLogoUrlChange = onFormRestaurantLogoUrlChange,
            onFormCompanyLogoUrlChange = onFormCompanyLogoUrlChange,
            onToggleRecipeSelection = onToggleRecipeSelection,
            onDismiss = onDismissForm,
            onSave = onSaveMenu,
            modifier = modifier,
        )

        uiState.selectedMenu != null -> AllergenMatrixView(
            uiState = uiState,
            onBack = onBack,
            onFilterCategory = onFilterCategory,
            onExportPdf = onExportPdf,
            onEditMenu = onEditMenu,
            onRequestDeleteMenu = onRequestDeleteMenu,
        )

        else -> MenuListView(
            uiState = uiState,
            onSelectMenu = onSelectMenu,
            onNewMenu = onNewMenu,
            onEditMenu = onEditMenu,
            onRequestDeleteMenu = onRequestDeleteMenu,
        )
    }

    // Delete confirmation dialog
    uiState.menuToDelete?.let { menu ->
        ConfirmDialog(
            title = "Eliminar menu",
            message = "Se archivara el menu \"${menu.name}\". Esta accion se puede deshacer.",
            confirmText = "Eliminar",
            onConfirm = onConfirmDeleteMenu,
            onDismiss = onDismissDeleteDialog,
            isDanger = true,
        )
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

@Composable
private fun MenuEditorForm(
    uiState: MenusUiState,
    onFormNameChange: (String) -> Unit,
    onFormDescriptionChange: (String) -> Unit,
    onFormRestaurantLogoUrlChange: (String) -> Unit,
    onFormCompanyLogoUrlChange: (String) -> Unit,
    onToggleRecipeSelection: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Volver",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Column {
                    Text(
                        text = if (uiState.editingMenu != null) "Editar Menu" else "Nuevo Menu",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "Configura el menu y selecciona las recetas",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        // Scrollable content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Name & Description
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                OutlinedTextField(
                    value = uiState.formName,
                    onValueChange = onFormNameChange,
                    label = { Text("Nombre del Menu") },
                    placeholder = { Text("Ej. Menu Primavera 2026") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Blue500,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    ),
                )
                OutlinedTextField(
                    value = uiState.formDescription,
                    onValueChange = onFormDescriptionChange,
                    label = { Text("Descripcion") },
                    placeholder = { Text("Descripcion del menu...") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Blue500,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    ),
                )
            }

            // Logos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                OutlinedTextField(
                    value = uiState.formRestaurantLogoUrl,
                    onValueChange = onFormRestaurantLogoUrlChange,
                    label = { Text("Logo del Restaurante (URL)") },
                    placeholder = { Text("https://...") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Blue500,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    ),
                )
                OutlinedTextField(
                    value = uiState.formCompanyLogoUrl,
                    onValueChange = onFormCompanyLogoUrlChange,
                    label = { Text("Logo de la Empresa (URL)") },
                    placeholder = { Text("https://...") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Blue500,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    ),
                )
            }

            // Recipe Selection
            Text(
                text = "Recetas del Menu",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            if (uiState.availableRecipes.isEmpty()) {
                Text(
                    text = "No hay recetas disponibles. Crea recetas primero.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Text(
                    text = "${uiState.formSelectedRecipeIds.size} de ${uiState.availableRecipes.size} seleccionadas",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface),
                ) {
                    uiState.availableRecipes.forEach { recipe ->
                        val isSelected = recipe.id in uiState.formSelectedRecipeIds
                        RecipeSelectionRow(
                            recipe = recipe,
                            isSelected = isSelected,
                            onToggle = { onToggleRecipeSelection(recipe.id) },
                        )
                    }
                }
            }
        }

        // Save button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            Button(
                onClick = onSave,
                enabled = !uiState.isSaving && uiState.formName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Blue500),
                shape = RoundedCornerShape(8.dp),
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp,
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
                    text = if (uiState.editingMenu != null) "Guardar Cambios" else "Crear Menu",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun RecipeSelectionRow(
    recipe: Recipe,
    isSelected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val categoryLabel = recipe.category

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = if (isSelected) Icons.Filled.CheckBox else Icons.Filled.CheckBoxOutlineBlank,
            contentDescription = if (isSelected) "Seleccionada" else "No seleccionada",
            tint = if (isSelected) Blue500 else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = recipe.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (categoryLabel.isNotBlank()) {
                Text(
                    text = categoryLabel,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Text(
            text = "${recipe.ingredientCount} ing.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun MenuListView(
    uiState: MenusUiState,
    onSelectMenu: (Menu) -> Unit,
    onNewMenu: () -> Unit,
    onEditMenu: (Menu) -> Unit,
    onRequestDeleteMenu: (Menu) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "Menus",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Gestiona los menus y sus alergenos",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Button(
                onClick = onNewMenu,
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
                    text = "Nuevo Menu",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        // Menu Cards
        uiState.menus.forEach { menu ->
            MenuCard(
                menu = menu,
                onClick = { onSelectMenu(menu) },
                onEdit = { onEditMenu(menu) },
                onDelete = { onRequestDeleteMenu(menu) },
            )
        }
    }
}

@Composable
private fun MenuCard(
    menu: Menu,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(width = 1.dp, color = MaterialTheme.colorScheme.outlineVariant, shape = RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = menu.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (menu.description.isNotBlank()) {
                    Text(
                        text = menu.description,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (menu.sections.isNotEmpty()) {
                        Badge(
                            text = "${menu.sections.size} secciones",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (menu.dishes.isNotEmpty()) {
                        Badge(
                            text = "${menu.dishes.size} platos",
                            color = Green500,
                        )
                    }
                    if (menu.recipes.isNotEmpty()) {
                        Badge(
                            text = "${menu.recipes.size} recetas",
                            color = Blue500,
                        )
                    }
                }
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Editar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Eliminar",
                        tint = Red500,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun Badge(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(horizontal = 8.dp, vertical = 2.dp),
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = color,
        )
    }
}

@Composable
private fun AllergenMatrixView(
    uiState: MenusUiState,
    onBack: () -> Unit,
    onFilterCategory: (String?) -> Unit,
    onExportPdf: () -> Unit,
    onEditMenu: (Menu) -> Unit,
    onRequestDeleteMenu: (Menu) -> Unit,
) {
    val menu = uiState.selectedMenu ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Header with back button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Volver",
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = menu.name,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "Menu de Alergenos",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            OutlinedButton(
                onClick = onExportPdf,
                shape = RoundedCornerShape(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.PictureAsPdf,
                    contentDescription = "Exportar PDF",
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Exportar PDF")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { onEditMenu(menu) },
                colors = ButtonDefaults.buttonColors(containerColor = Blue500),
                shape = RoundedCornerShape(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Editar Menu", color = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedButton(
                onClick = { onRequestDeleteMenu(menu) },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Red500),
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Eliminar",
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Eliminar")
            }
        }

        // Description section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Blue500.copy(alpha = 0.05f))
                .border(
                    width = 1.dp,
                    color = Blue500.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp),
                )
                .padding(16.dp),
        ) {
            Text(
                text = "Generar Menu Adaptado (Libre de...): Selecciona una categoria y consulta " +
                    "la tabla de alergenos para cada plato.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Category Filter (dynamic from recipe data)
        val recipeCategories = uiState.menuRecipes
            .map { it.category }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
        CategoryFilterRow(
            categories = recipeCategories,
            selectedCategory = uiState.selectedCategory,
            onCategorySelected = onFilterCategory,
        )

        // Allergen Matrix Table (from menu's recipes with computed allergens)
        if (uiState.isLoadingRecipes) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.menuRecipes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Este menu no tiene recetas asociadas. " +
                        "Edita el menu para seleccionar recetas y ver la tabla de alergenos.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            // Convert recipes to Dish format for the allergen matrix table
            val dishesFromRecipes = uiState.menuRecipes.map { recipe ->
                Dish(
                    id = recipe.id,
                    name = recipe.name,
                    allergens = recipe.computedAllergens,
                    category = recipe.category,
                )
            }
            AllergenMatrixTable(
                dishes = dishesFromRecipes,
                selectedCategory = uiState.selectedCategory,
            )

            // Legend footer
            AllergenTableLegend()
        }
    }
}

@Preview
@Composable
private fun MenusContentPreview() {
    MenuAdminTheme {
        MenusContent(
            uiState = MenusUiState(
                isLoading = false,
                menus = listOf(
                    Menu(
                        id = "menu-1",
                        name = "Menu Primavera 2025",
                        description = "Menu de temporada",
                        dishes = listOf(
                            Dish(
                                id = "d1",
                                name = "Croquetas Ibericas",
                                price = 12.50,
                                category = "Entrantes",
                                allergens = setOf(AllergenType.GLUTEN, AllergenType.DAIRY),
                            ),
                        ),
                        createdAt = Clock.System.now(),
                        updatedAt = Clock.System.now(),
                    ),
                ),
                availableRecipes = listOf(
                    Recipe(
                        id = "rec-1",
                        name = "Croquetas Ibericas",
                        category = "ENTRANTE",
                        ingredientCount = 5,
                    ),
                ),
            ),
            onSelectMenu = {},
            onBack = {},
            onFilterCategory = {},
            onExportPdf = {},
            onNewMenu = {},
            onEditMenu = {},
            onRequestDeleteMenu = {},
            onConfirmDeleteMenu = {},
            onDismissDeleteDialog = {},
            onFormNameChange = {},
            onFormDescriptionChange = {},
            onFormRestaurantLogoUrlChange = {},
            onFormCompanyLogoUrlChange = {},
            onToggleRecipeSelection = {},
            onDismissForm = {},
            onSaveMenu = {},
        )
    }
}
