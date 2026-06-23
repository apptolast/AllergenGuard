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
import kotlinx.serialization.json.jsonObject
import org.apptolast.menuadmin.data.CurrentAccountHolder
import org.apptolast.menuadmin.data.SelectedRestaurantHolder
import org.apptolast.menuadmin.data.util.BackupData
import org.apptolast.menuadmin.data.util.JsonExporter
import org.apptolast.menuadmin.domain.model.Ingredient
import org.apptolast.menuadmin.domain.model.Menu
import org.apptolast.menuadmin.domain.model.Recipe
import org.apptolast.menuadmin.domain.model.Restaurant
import org.apptolast.menuadmin.domain.platform.FileHandler
import org.apptolast.menuadmin.domain.repository.IngredientRepository
import org.apptolast.menuadmin.domain.repository.MenuRepository
import org.apptolast.menuadmin.domain.repository.RecipeRepository
import org.apptolast.menuadmin.domain.repository.RestaurantRepository
import kotlin.time.Clock

/** How an import file is applied over the current database. The user picks this in the UI. */
enum class ImportMode {
    /** Upsert: update existing documents by id and add new ones. Never deletes. */
    MERGE,

    /**
     * Replace: upsert everything in the file, then delete the current documents whose id is NOT in
     * the file, so the database ends up exactly as the file. App-native backups are matched against
     * the whole platform; legacy dumps only against their target restaurant (and never delete menus,
     * which they do not describe). Destructive — guarded by the pre-import backup.
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
    /** All restaurants, to populate the "target restaurant" picker for legacy imports. */
    val restaurants: List<Restaurant> = emptyList(),
    /** Where restaurant-scoped data from a legacy (restaurant-less) file is imported. */
    val targetRestaurantId: String? = null,
)

class BackupViewModel(
    private val ingredientRepository: IngredientRepository,
    private val recipeRepository: RecipeRepository,
    private val menuRepository: MenuRepository,
    private val fileHandler: FileHandler,
    private val json: Json,
    private val selectedRestaurantHolder: SelectedRestaurantHolder,
    private val restaurantRepository: RestaurantRepository,
    private val currentAccountHolder: CurrentAccountHolder,
) : ViewModel() {
    private val _uiState = MutableStateFlow(BackupUiState())
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    // Held between analyzeImport() (parse + preview + safety backup) and confirmImport() (apply).
    private var pending: BackupData? = null
    private var currentBeforeImport: Snapshot? = null
    private var pendingIsLegacy: Boolean = false

    private data class Snapshot(
        val ingredients: List<Ingredient>,
        val recipes: List<Recipe>,
        val menus: List<Menu>,
    )

    init {
        viewModelScope.launch {
            val all = runCatching { restaurantRepository.getAllRestaurants().first() }.getOrDefault(emptyList())
            _uiState.update {
                it.copy(
                    restaurants = all,
                    targetRestaurantId = it.targetRestaurantId
                        ?: selectedRestaurantHolder.selected.value?.id
                        ?: all.firstOrNull()?.id,
                )
            }
        }
    }

    fun selectTargetRestaurant(id: String) {
        _uiState.update { it.copy(targetRestaurantId = id) }
    }

    fun exportData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, message = null) }
            try {
                val s = globalSnapshot()
                val jsonString = JsonExporter.exportAllData(
                    s.ingredients,
                    s.recipes,
                    s.menus,
                    json,
                    accountId = currentAccountHolder.accountIdOrNull ?: "",
                )
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
                val parsed = parseImport(content)
                val backup = parsed.data

                // Reject an app-native backup that belongs to a different account: its recipes/menus carry
                // foreign restaurantIds the security rules would refuse. Legacy files have no account and
                // re-target by the chosen restaurant of the current account.
                val currentAccountId = currentAccountHolder.accountIdOrNull
                if (!parsed.isLegacy &&
                    backup.accountId.isNotEmpty() &&
                    currentAccountId != null &&
                    backup.accountId != currentAccountId
                ) {
                    _uiState.update {
                        it.copy(
                            isImporting = false,
                            preview = null,
                            message = "Este backup pertenece a otra cuenta y no se puede importar aquí.",
                        )
                    }
                    return@launch
                }

                // Baseline for the preview, the REPLACE delete-set and the safety backup. App-native
                // files span the whole platform; legacy files only touch their target restaurant.
                val current = if (parsed.isLegacy) {
                    val targetId = _uiState.value.targetRestaurantId!! // parseImport guarantees non-null here
                    Snapshot(
                        ingredients = ingredientRepository.getAllIngredients().first(),
                        recipes = recipeRepository.getRecipesByRestaurant(targetId).first(),
                        menus = menuRepository.getMenusByRestaurant(targetId).first(),
                    )
                } else {
                    globalSnapshot()
                }

                // Safety net: download the current state before touching anything (matters most for REPLACE).
                val backupFileName = "menuadmin_backup_pre-import_" +
                    Clock.System.now().toString().replace(":", "-").substringBefore(".") + ".json"
                fileHandler.saveFile(
                    JsonExporter.exportAllData(
                        current.ingredients,
                        current.recipes,
                        current.menus,
                        json,
                        accountId = currentAccountHolder.accountIdOrNull ?: "",
                    ),
                    backupFileName,
                )

                pending = backup
                currentBeforeImport = current
                pendingIsLegacy = parsed.isLegacy

                val curIng = current.ingredients.map { it.id }.toSet()
                val curRec = current.recipes.map { it.id }.toSet()
                val curMenu = current.menus.map { it.id }.toSet()
                val fileIng = backup.ingredients.map { it.id }.toSet()
                val fileRec = backup.recipes.map { it.id }.toSet()
                val fileMenu = backup.menus.map { it.id }.toSet()

                val restaurantName = if (parsed.isLegacy) {
                    _uiState.value.restaurants.find { it.id == _uiState.value.targetRestaurantId }?.name
                        ?: "Restaurante destino"
                } else {
                    "Todos los restaurantes"
                }

                _uiState.update {
                    it.copy(
                        isImporting = false,
                        preview = ImportPreview(
                            restaurantName = restaurantName,
                            ingredientsNew = backup.ingredients.count { i -> i.id !in curIng },
                            ingredientsUpdated = backup.ingredients.count { i -> i.id in curIng },
                            recipesNew = backup.recipes.count { r -> r.id !in curRec },
                            recipesUpdated = backup.recipes.count { r -> r.id in curRec },
                            menusNew = backup.menus.count { m -> m.id !in curMenu },
                            menusUpdated = backup.menus.count { m -> m.id in curMenu },
                            ingredientsToDelete = current.ingredients.count { i -> i.id !in fileIng },
                            recipesToDelete = current.recipes.count { r -> r.id !in fileRec },
                            // Legacy files describe no menus, so REPLACE never deletes menus.
                            menusToDelete = if (parsed.isLegacy) 0 else current.menus.count { m -> m.id !in fileMenu },
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
        val isLegacy = pendingIsLegacy
        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true, preview = null) }
            try {
                // Upsert in both modes; ingredients first so recipes recompute their allergens correctly.
                // Recipes/menus route by their own restaurantId (set per item, or stamped on legacy data).
                backup.ingredients.forEach { ingredientRepository.updateIngredient(it) }
                backup.recipes.forEach { recipeRepository.updateRecipe(it) }
                backup.menus.forEach { menuRepository.updateMenu(it) }

                var deleted = 0
                if (mode == ImportMode.REPLACE && current != null) {
                    val fileIng = backup.ingredients.map { it.id }.toSet()
                    val fileRec = backup.recipes.map { it.id }.toSet()

                    // Group deletes by restaurant: the repos resolve the document path from their cache,
                    // which only holds the last-loaded restaurant, so re-load each group first.
                    current.recipes.filter { it.id !in fileRec }
                        .groupBy { it.restaurantId }
                        .forEach { (rid, recipes) ->
                            if (rid.isNotEmpty()) recipeRepository.getRecipesByRestaurant(rid).first()
                            recipes.forEach {
                                recipeRepository.deleteRecipe(it.id)
                                deleted++
                            }
                        }

                    // Legacy files never describe menus, so deleting them would wipe the restaurant's menus.
                    if (!isLegacy) {
                        val fileMenu = backup.menus.map { it.id }.toSet()
                        current.menus.filter { it.id !in fileMenu }
                            .groupBy { it.restaurantId }
                            .forEach { (rid, menus) ->
                                if (rid.isNotEmpty()) menuRepository.getMenusByRestaurant(rid).first()
                                menus.forEach {
                                    menuRepository.deleteMenu(it.id)
                                    deleted++
                                }
                            }
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
                pendingIsLegacy = false
            }
        }
    }

    /**
     * Parses either supported format: the app's own backup ([BackupData], which always carries
     * `exportedAt`) or a legacy/external dump (`{ ingredients, recipes, timestamp }`).
     *
     * App-native backups embed `restaurantId` per recipe/menu, so they restore every restaurant on
     * their own — no target needed. Legacy dumps carry no restaurant, so recipes are assigned to the
     * [BackupUiState.targetRestaurantId] chosen in the screen (decoupled from the navigation selection).
     */
    private fun parseImport(content: String): ParsedImport {
        val root = json.parseToJsonElement(content).jsonObject
        if (root.containsKey("exportedAt")) {
            return ParsedImport(JsonExporter.importAllData(content, json), isLegacy = false)
        }
        val targetId = _uiState.value.targetRestaurantId
            ?: throw IllegalStateException("Elige un restaurante destino para importar este archivo")
        val result = JsonExporter.importExternalData(content, json, targetId)
        return ParsedImport(
            BackupData(
                ingredients = result.ingredients,
                recipes = result.recipes,
                menus = emptyList(), // legacy format carries no menus
                exportedAt = Clock.System.now(),
            ),
            isLegacy = true,
        )
    }

    private data class ParsedImport(
        val data: BackupData,
        val isLegacy: Boolean,
    )

    fun cancelImport() {
        pending = null
        currentBeforeImport = null
        pendingIsLegacy = false
        _uiState.update { it.copy(preview = null, isImporting = false) }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    /** Whole platform: global ingredient catalog + every restaurant's recipes and menus. */
    private suspend fun globalSnapshot(): Snapshot {
        val ingredients = ingredientRepository.getAllIngredients().first()
        val restaurants = restaurantRepository.getAllRestaurants().first()
        val recipes = mutableListOf<Recipe>()
        val menus = mutableListOf<Menu>()
        for (r in restaurants) {
            // Recipes first: loading menus refreshes the recipe cache for the same restaurant.
            recipes += recipeRepository.getRecipesByRestaurant(r.id).first()
            menus += menuRepository.getMenusByRestaurant(r.id).first()
        }
        return Snapshot(ingredients, recipes, menus)
    }
}
