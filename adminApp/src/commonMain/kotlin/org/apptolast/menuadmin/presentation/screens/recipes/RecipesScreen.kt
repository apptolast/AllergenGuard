package org.apptolast.menuadmin.presentation.screens.recipes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import org.apptolast.menuadmin.domain.model.AllergenType
import org.apptolast.menuadmin.domain.model.ContainmentLevel
import org.apptolast.menuadmin.domain.model.Recipe
import org.apptolast.menuadmin.domain.model.RecipeIngredient
import org.apptolast.menuadmin.presentation.components.AllergenBadge
import org.apptolast.menuadmin.presentation.components.AllergenSummaryCard
import org.apptolast.menuadmin.presentation.components.ErrorSnackbarEffect
import org.apptolast.menuadmin.presentation.components.SearchBar
import org.apptolast.menuadmin.presentation.screens.recipes.components.RecipeCard
import org.apptolast.menuadmin.presentation.theme.Blue500
import org.apptolast.menuadmin.presentation.theme.MenuAdminTheme
import org.apptolast.menuadmin.presentation.theme.Red500

@Composable
fun RecipesScreen(viewModel: RecipesViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    RecipesContent(
        uiState = uiState,
        onNewRecipe = viewModel::onNewRecipe,
        onDismissEditor = viewModel::onDismissEditor,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onEditRecipe = viewModel::onEditRecipe,
        onFormNameChange = viewModel::onFormNameChange,
        onFormDescriptionChange = viewModel::onFormDescriptionChange,
        onFormCategoryChange = viewModel::onFormCategoryChange,
        onFormPriceChange = viewModel::onFormPriceChange,
        onPickImage = viewModel::onPickImage,
        onRemoveImage = viewModel::onRemoveImage,
        onAddIngredientToForm = viewModel::onAddIngredientToForm,
        onRemoveIngredientFromForm = viewModel::onRemoveIngredientFromForm,
        onSaveRecipe = viewModel::onSaveRecipe,
        onDeleteRecipe = viewModel::onDeleteRecipe,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecipesContent(
    uiState: RecipesUiState,
    onNewRecipe: () -> Unit,
    onDismissEditor: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onEditRecipe: (Recipe) -> Unit,
    onFormNameChange: (String) -> Unit,
    onFormDescriptionChange: (String) -> Unit,
    onFormCategoryChange: (String) -> Unit,
    onFormPriceChange: (String) -> Unit,
    onPickImage: () -> Unit,
    onRemoveImage: () -> Unit,
    onAddIngredientToForm: (RecipeIngredient) -> Unit,
    onRemoveIngredientFromForm: (String) -> Unit,
    onSaveRecipe: () -> Unit,
    onDeleteRecipe: (String) -> Unit,
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
                        text = "Recetas y Subelaboraciones",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = if (uiState.isEditing) {
                            if (uiState.editingRecipe != null) "Editando receta" else "Nueva receta"
                        } else {
                            "Gestiona tus recetas y sus ingredientes"
                        },
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (!uiState.isEditing) {
                Button(
                    onClick = onNewRecipe,
                    colors = ButtonDefaults.buttonColors(containerColor = Blue500),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Nueva",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Nueva Receta",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            } else {
                // Editing: keep the primary actions in the top bar so they're reachable without
                // scrolling to the bottom of a long composition list.
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (uiState.editingRecipe != null) {
                        OutlinedButton(
                            onClick = { onDeleteRecipe(uiState.editingRecipe.id) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Red500),
                            border = BorderStroke(1.dp, Red500),
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Eliminar", fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Button(
                        onClick = onSaveRecipe,
                        enabled = !uiState.isSaving,
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
                            text = "Guardar Cambios",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }

        if (uiState.isEditing) {
            // Recipe Editor Form
            RecipeEditorForm(
                uiState = uiState,
                onFormNameChange = onFormNameChange,
                onFormDescriptionChange = onFormDescriptionChange,
                onFormCategoryChange = onFormCategoryChange,
                onFormPriceChange = onFormPriceChange,
                onPickImage = onPickImage,
                onRemoveImage = onRemoveImage,
                onAddIngredientToForm = onAddIngredientToForm,
                onRemoveIngredientFromForm = onRemoveIngredientFromForm,
            )
        } else {
            // Search Bar
            SearchBar(
                query = uiState.searchQuery,
                onQueryChange = onSearchQueryChange,
                placeholder = "Buscar recetas...",
            )

            // Recipe Cards Grid
            val ingredientLookup = remember(uiState.allIngredients) {
                uiState.allIngredients.associateBy { it.id }
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    maxItemsInEachRow = 3,
                ) {
                    uiState.recipes.forEach { recipe ->
                        RecipeCard(
                            recipe = recipe,
                            ingredientLookup = ingredientLookup,
                            onClick = { onEditRecipe(recipe) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    val remainder = uiState.recipes.size % 3
                    if (remainder != 0) {
                        repeat(3 - remainder) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // Errors surface through the app-wide snackbar instead of inline red text.
        ErrorSnackbarEffect(uiState.error)
    }
}

@Composable
private fun DishImagePanel(
    imageUrl: String?,
    isUploading: Boolean,
    onPickImage: () -> Unit,
    onRemoveImage: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Imagen del Plato",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        // Preview area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            when {
                isUploading -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        Text(
                            text = "Comprimiendo y subiendo...",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                imageUrl != null -> {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Imagen del plato",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                    )
                }

                else -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Image,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(40.dp),
                        )
                        Text(
                            text = "Sin imagen",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        // Actions
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = onPickImage,
                enabled = !isUploading,
                shape = RoundedCornerShape(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Image,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(if (imageUrl != null) "Cambiar imagen" else "Subir imagen")
            }
            if (imageUrl != null && !isUploading) {
                TextButton(onClick = onRemoveImage) {
                    Text("Quitar", color = Red500)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RecipeEditorForm(
    uiState: RecipesUiState,
    onFormNameChange: (String) -> Unit,
    onFormDescriptionChange: (String) -> Unit,
    onFormCategoryChange: (String) -> Unit,
    onFormPriceChange: (String) -> Unit,
    onPickImage: () -> Unit,
    onRemoveImage: () -> Unit,
    onAddIngredientToForm: (RecipeIngredient) -> Unit,
    onRemoveIngredientFromForm: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var ingredientSearchQuery by remember { mutableStateOf("") }
    var categoryFieldFocused by remember { mutableStateOf(false) }

    // Aggregate allergens from form ingredients, keeping the strongest containment level per allergen
    // (CONTAINS beats MAY_CONTAIN; FREE_OF ignored) so the summary can mark traces distinctly.
    val aggregateAllergens: Map<AllergenType, ContainmentLevel> =
        remember(uiState.formIngredients, uiState.allIngredients) {
            val ingredientMap = uiState.allIngredients.associateBy { it.id }
            val strongest = mutableMapOf<AllergenType, ContainmentLevel>()
            uiState.formIngredients.forEach { ri ->
                ingredientMap[ri.ingredientId]?.allergens.orEmpty().forEach { ia ->
                    if (ia.containmentLevel == ContainmentLevel.FREE_OF) return@forEach
                    val type = AllergenType.fromApiCode(ia.allergenCode) ?: return@forEach
                    val current = strongest[type]
                    if (current == null || ia.containmentLevel.ordinal < current.ordinal) {
                        strongest[type] = ia.containmentLevel
                    }
                }
            }
            strongest
        }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // Form fields row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Left column - form fields
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Name
                OutlinedTextField(
                    value = uiState.formName,
                    onValueChange = onFormNameChange,
                    label = { Text("Nombre del Plato / Subelaboracion") },
                    placeholder = { Text("Ej. Croquetas de jamon") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Blue500,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    ),
                )

                // Description (customer-facing; shown in the mobile app dish detail)
                OutlinedTextField(
                    value = uiState.formDescription,
                    onValueChange = onFormDescriptionChange,
                    label = { Text("Descripcion") },
                    placeholder = { Text("Ej. Crujientes croquetas caseras de jamon iberico") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Blue500,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    ),
                )

                // Price
                OutlinedTextField(
                    value = uiState.formPrice,
                    onValueChange = { value ->
                        if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                            onFormPriceChange(value)
                        }
                    },
                    label = { Text("Precio") },
                    placeholder = { Text("Ej. 12.50") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Blue500,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    ),
                )

                // Category selector with autocomplete
                Box {
                    OutlinedTextField(
                        value = uiState.formCategory,
                        onValueChange = onFormCategoryChange,
                        label = { Text("Categoria") },
                        placeholder = { Text("Ej. Entrantes, Principales, Postres...") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { categoryFieldFocused = it.isFocused },
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Blue500,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        ),
                    )
                    val suggestions = if (categoryFieldFocused && uiState.formCategory.isNotBlank()) {
                        uiState.availableCategories.filter {
                            it.contains(uiState.formCategory, ignoreCase = true) &&
                                !it.equals(uiState.formCategory, ignoreCase = true)
                        }
                    } else {
                        emptyList()
                    }
                    DropdownMenu(
                        expanded = suggestions.isNotEmpty(),
                        onDismissRequest = { categoryFieldFocused = false },
                    ) {
                        suggestions.forEach { suggestion ->
                            DropdownMenuItem(
                                text = { Text(suggestion) },
                                onClick = {
                                    onFormCategoryChange(suggestion)
                                    categoryFieldFocused = false
                                },
                            )
                        }
                    }
                }

                // Add ingredients section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Anadir Componentes",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    OutlinedTextField(
                        value = ingredientSearchQuery,
                        onValueChange = { ingredientSearchQuery = it },
                        placeholder = { Text("Buscar ingrediente...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Blue500,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        ),
                    )
                    // Show matching ingredients
                    if (ingredientSearchQuery.isNotBlank()) {
                        val matchingIngredients = uiState.allIngredients.filter {
                            it.name.contains(ingredientSearchQuery, ignoreCase = true) &&
                                uiState.formIngredients.none { fi -> fi.ingredientId == it.id }
                        }.take(5)

                        if (matchingIngredients.isNotEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surface),
                            ) {
                                matchingIngredients.forEach { ingredient ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                onAddIngredientToForm(
                                                    RecipeIngredient(
                                                        ingredientId = ingredient.id,
                                                        ingredientName = ingredient.name,
                                                    ),
                                                )
                                                ingredientSearchQuery = ""
                                            }
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            text = ingredient.name,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                        )
                                        Icon(
                                            imageVector = Icons.Filled.Add,
                                            contentDescription = "Anadir",
                                            tint = Blue500,
                                            modifier = Modifier.size(18.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Current ingredients list
                if (uiState.formIngredients.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = "COMPOSICION DETALLADA",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = "${uiState.formIngredients.size} items",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        uiState.formIngredients.forEach { ri ->
                            val ingredientAllergens =
                                uiState.allIngredients
                                    .find { it.id == ri.ingredientId }
                                    ?.allergens.orEmpty()
                                    .filter { it.containmentLevel != ContainmentLevel.FREE_OF }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Text(
                                        text = ri.ingredientName,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    if (ingredientAllergens.isNotEmpty()) {
                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp),
                                        ) {
                                            ingredientAllergens.forEach { ia ->
                                                val type = AllergenType.fromApiCode(ia.allergenCode)
                                                if (type != null) {
                                                    AllergenBadge(
                                                        allergenType = type,
                                                        isActive = true,
                                                        containmentLevel = ia.containmentLevel,
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                IconButton(
                                    onClick = {
                                        onRemoveIngredientFromForm(ri.ingredientId)
                                    },
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Quitar",
                                        tint = Red500,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Right column - Allergen summary
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "Resumen Total de Alergenos",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    AllergenType.entries.forEach { allergen ->
                        AllergenSummaryCard(
                            allergenType = allergen,
                            containmentLevel = aggregateAllergens[allergen],
                        )
                    }
                }

                // Dish image: preview + management (compressed client-side, stored in Firebase Storage)
                DishImagePanel(
                    imageUrl = uiState.formImageUrl,
                    isUploading = uiState.isUploadingImage,
                    onPickImage = onPickImage,
                    onRemoveImage = onRemoveImage,
                )
            }
        }
    }
}

@Preview
@Composable
private fun RecipesContentPreview() {
    MenuAdminTheme {
        RecipesContent(
            uiState = RecipesUiState(
                isLoading = false,
                recipes = listOf(
                    Recipe(
                        id = "rec-1",
                        name = "Croquetas Ibericas",
                        category = "Entrantes",
                        price = 12.50,
                        isActive = true,
                        ingredientCount = 5,
                        allergenCount = 2,
                        computedAllergens = setOf(AllergenType.GLUTEN, AllergenType.DAIRY),
                    ),
                ),
            ),
            onNewRecipe = {},
            onDismissEditor = {},
            onSearchQueryChange = {},
            onEditRecipe = {},
            onFormNameChange = {},
            onFormDescriptionChange = {},
            onFormCategoryChange = {},
            onFormPriceChange = {},
            onPickImage = {},
            onRemoveImage = {},
            onAddIngredientToForm = {},
            onRemoveIngredientFromForm = {},
            onSaveRecipe = {},
            onDeleteRecipe = {},
        )
    }
}
