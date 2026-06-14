package org.apptolast.menuadmin.presentation.screens.backup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.apptolast.menuadmin.data.SelectedRestaurantHolder
import org.apptolast.menuadmin.data.util.BackupData
import org.apptolast.menuadmin.data.util.JsonExporter
import org.apptolast.menuadmin.domain.model.Ingredient
import org.apptolast.menuadmin.domain.model.Menu
import org.apptolast.menuadmin.domain.model.Recipe
import org.apptolast.menuadmin.domain.platform.FileHandler
import org.apptolast.menuadmin.domain.repository.IngredientRepository
import org.apptolast.menuadmin.domain.repository.MenuRepository
import org.apptolast.menuadmin.domain.repository.RecipeRepository
import kotlin.time.Clock

/** How an import file is applied over the current database. The user picks this in the UI. */
enum class ImportMode {
    /** Upsert: update existing documents by id and add new ones. Never deletes. */
    MERGE,

    /**
     * Replace: upsert everything in the file, then delete the current documents whose id is NOT in
     * the file, so the database ends up exactly as the file. Scoped to the global ingredient catalog
     * and the selected restaurant's recipes/menus. Destructive — guarded by the pre-import backup.
     */
    REPLACE,
}

/** Counts shown in the confirmation dialog so the user knows what an import will do before applying it. */
data class ImportPreview(
    val restaurantName: String,
    val ingredientsNew: Int,
    val ingredientsUpdated: Int,
    val recipesNew: Int,
    val recipesUpdated: Int,
    val menusNew: Int,
    val menusUpdated: Int,
    val ingredientsToDelete: Int,
    val recipesToDelete: Int,
    val menusToDelete: Int,
    val backupFileName: String,
)

data class BackupUiState(
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
    val message: String? = null,
    val preview: ImportPreview? = null,
)

class BackupViewModel(
    private val ingredientRepository: IngredientRepository,
    private val recipeRepository: RecipeRepository,
    private val menuRepository: MenuRepository,
    private val fileHandler: FileHandler,
    private val json: Json,
    private val selectedRestaurantHolder: SelectedRestaurantHolder,
) : ViewModel() {
    private val _uiState = MutableStateFlow(BackupUiState())
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    // Held between analyzeImport() (parse + preview + safety backup) and confirmImport() (apply).
    private var pending: BackupData? = null
    private var currentBeforeImport: Snapshot? = null

    private data class Snapshot(
        val ingredients: List<Ingredient>,
        val recipes: List<Recipe>,
        val menus: List<Menu>,
    )

    fun exportData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, message = null) }
            try {
                val s = currentSnapshot()
                val jsonString = JsonExporter.exportAllData(s.ingredients, s.recipes, s.menus, json)
                fileHandler.saveFile(jsonString, "menuadmin_backup.json")
                _uiState.update {
                    it.copy(
                        isExporting = false,
                        message = "Exportacion completada: ${s.ingredients.size} ingredientes, " +
                            "${s.recipes.size} recetas, ${s.menus.size} menus",
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isExporting = false, message = "Error al exportar: ${e.message}") }
            }
        }
    }

    /** Reads the file, downloads a safety backup of the current DB, and prepares the confirmation preview. */
    fun analyzeImport() {
        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true, message = null) }
            try {
                val content = fileHandler.pickAndReadFile()
                if (content == null) {
                    _uiState.update { it.copy(isImporting = false) }
                    return@launch
                }
                val backup = JsonExporter.importAllData(content, json)
                val current = currentSnapshot()

                // Safety net: download the current state before touching anything (matters most for REPLACE).
                val backupFileName = "menuadmin_backup_pre-import_" +
                    Clock.System.now().toString().replace(":", "-").substringBefore(".") + ".json"
                fileHandler.saveFile(
                    JsonExporter.exportAllData(current.ingredients, current.recipes, current.menus, json),
                    backupFileName,
                )

                pending = backup
                currentBeforeImport = current

                val curIng = current.ingredients.map { it.id }.toSet()
                val curRec = current.recipes.map { it.id }.toSet()
                val curMenu = current.menus.map { it.id }.toSet()
                val fileIng = backup.ingredients.map { it.id }.toSet()
                val fileRec = backup.recipes.map { it.id }.toSet()
                val fileMenu = backup.menus.map { it.id }.toSet()

                _uiState.update {
                    it.copy(
                        isImporting = false,
                        preview = ImportPreview(
                            restaurantName = selectedRestaurantHolder.selected.value?.name ?: "(ninguno seleccionado)",
                            ingredientsNew = backup.ingredients.count { i -> i.id !in curIng },
                            ingredientsUpdated = backup.ingredients.count { i -> i.id in curIng },
                            recipesNew = backup.recipes.count { r -> r.id !in curRec },
                            recipesUpdated = backup.recipes.count { r -> r.id in curRec },
                            menusNew = backup.menus.count { m -> m.id !in curMenu },
                            menusUpdated = backup.menus.count { m -> m.id in curMenu },
                            ingredientsToDelete = current.ingredients.count { i -> i.id !in fileIng },
                            recipesToDelete = current.recipes.count { r -> r.id !in fileRec },
                            menusToDelete = current.menus.count { m -> m.id !in fileMenu },
                            backupFileName = backupFileName,
                        ),
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isImporting = false, preview = null, message = "Error al leer el archivo: ${e.message}")
                }
            }
        }
    }

    /** Applies the previously analysed import in [mode]. */
    fun confirmImport(mode: ImportMode) {
        val backup = pending ?: return
        val current = currentBeforeImport
        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true, preview = null) }
            try {
                // Upsert in both modes; ingredients first so recipes recompute their allergens correctly.
                backup.ingredients.forEach { ingredientRepository.updateIngredient(it) }
                backup.recipes.forEach { recipeRepository.updateRecipe(it) }
                backup.menus.forEach { menuRepository.updateMenu(it) }

                var deleted = 0
                if (mode == ImportMode.REPLACE && current != null) {
                    val fileIng = backup.ingredients.map { it.id }.toSet()
                    val fileRec = backup.recipes.map { it.id }.toSet()
                    val fileMenu = backup.menus.map { it.id }.toSet()
                    current.menus.filter { it.id !in fileMenu }.forEach {
                        menuRepository.deleteMenu(it.id)
                        deleted++
                    }
                    current.recipes.filter { it.id !in fileRec }.forEach {
                        recipeRepository.deleteRecipe(it.id)
                        deleted++
                    }
                    current.ingredients.filter { it.id !in fileIng }
                        .forEach {
                            ingredientRepository.deleteIngredient(it.id)
                            deleted++
                        }
                }

                val label = if (mode == ImportMode.REPLACE) "Reemplazo" else "Combinacion"
                _uiState.update {
                    it.copy(
                        isImporting = false,
                        message = "$label completado: ${backup.ingredients.size} ingredientes, " +
                            "${backup.recipes.size} recetas, ${backup.menus.size} menus importados" +
                            if (deleted > 0) ", $deleted eliminados" else "",
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isImporting = false, message = "Error al importar: ${e.message}") }
            } finally {
                pending = null
                currentBeforeImport = null
            }
        }
    }

    fun cancelImport() {
        pending = null
        currentBeforeImport = null
        _uiState.update { it.copy(preview = null, isImporting = false) }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    /** Current DB scoped like a manual export: global ingredients + the selected restaurant's recipes/menus. */
    private suspend fun currentSnapshot(): Snapshot {
        val ingredients = ingredientRepository.getAllIngredients().first()
        val restaurantId = selectedRestaurantHolder.selected.value?.id
        val recipes =
            if (restaurantId != null) {
                recipeRepository.getRecipesByRestaurant(restaurantId).first()
            } else {
                recipeRepository.getAllRecipes().first()
            }
        val menus =
            if (restaurantId != null) {
                menuRepository.getMenusByRestaurant(restaurantId).first()
            } else {
                menuRepository.getAllMenus().first()
            }
        return Snapshot(ingredients, recipes, menus)
    }
}
