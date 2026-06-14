package org.apptolast.menuadmin.data.util

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import org.apptolast.menuadmin.data.dto.ImportDataDto
import org.apptolast.menuadmin.data.dto.ImportIngredientDto
import org.apptolast.menuadmin.data.dto.ImportRecipeDto
import org.apptolast.menuadmin.data.mapper.ImportMapper
import org.apptolast.menuadmin.data.mapper.ImportResult
import org.apptolast.menuadmin.domain.model.Ingredient
import org.apptolast.menuadmin.domain.model.Menu
import org.apptolast.menuadmin.domain.model.Recipe
import kotlin.time.Clock
import kotlin.time.Instant

@Serializable
data class BackupData(
    val ingredients: List<Ingredient>,
    val recipes: List<Recipe>,
    val menus: List<Menu>,
    val exportedAt: Instant,
)

object JsonExporter {
    fun exportMenu(
        menu: Menu,
        json: Json,
    ): String = json.encodeToString(Menu.serializer(), menu)

    fun importMenu(
        jsonString: String,
        json: Json,
    ): Menu = json.decodeFromString(Menu.serializer(), jsonString)

    fun exportAllData(
        ingredients: List<Ingredient>,
        recipes: List<Recipe>,
        menus: List<Menu>,
        json: Json,
    ): String {
        val backup = BackupData(
            ingredients = ingredients,
            recipes = recipes,
            menus = menus,
            exportedAt = Clock.System.now(),
        )
        return json.encodeToString(BackupData.serializer(), backup)
    }

    fun importAllData(
        jsonString: String,
        json: Json,
    ): BackupData = json.decodeFromString(BackupData.serializer(), jsonString)

    fun importExternalData(
        jsonString: String,
        json: Json,
    ): ImportResult {
        val dto = json.decodeFromString(ImportDataDto.serializer(), jsonString)
        return ImportMapper.mapAll(dto)
    }

    fun exportExternalData(
        ingredients: List<Ingredient>,
        recipes: List<Recipe>,
        json: Json,
    ): String {
        val dto = ImportDataDto(
            ingredients = ingredients.map { ingredient ->
                ImportIngredientDto(
                    id = ingredient.id.toLongOrNull() ?: 0L,
                    name = ingredient.name,
                    contains = ingredient.allergenTypes.map { it.jsonKey },
                )
            },
            recipes = recipes.map { recipe ->
                ImportRecipeDto(
                    id = recipe.id.toLongOrNull() ?: 0L,
                    name = recipe.name,
                    ingredientIds = recipe.ingredients.map { ri ->
                        JsonPrimitive(ri.ingredientId.toLongOrNull() ?: 0L)
                    },
                    active = recipe.isActive,
                )
            },
            timestamp = Clock.System.now().toString(),
        )
        return json.encodeToString(ImportDataDto.serializer(), dto)
    }
}
