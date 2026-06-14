package org.apptolast.menuadmin.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import org.apptolast.menuadmin.data.remote.firebase.FirestoreClient
import org.apptolast.menuadmin.data.remote.firebase.FirestoreDocument
import org.apptolast.menuadmin.domain.model.Menu
import org.apptolast.menuadmin.domain.model.MenuRecipeSummary
import org.apptolast.menuadmin.domain.model.Recipe
import org.apptolast.menuadmin.domain.repository.MenuRepository
import org.apptolast.menuadmin.domain.repository.RecipeRepository
import org.apptolast.menuadmin.presentation.SelectedRestaurantHolder
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * [MenuRepository] backed by the `restaurants/{restaurantId}/menus` subcollection. In the simplified
 * model a menu is just a set of recipes (`recipeIds`); the "carta de alérgenos" is derived from those
 * recipes' computed allergens by the UI. Sections/dishes/digital-cards are intentionally dropped.
 */
@OptIn(ExperimentalUuidApi::class)
class FirestoreMenuRepository(
    private val firestore: FirestoreClient,
    private val recipeRepository: RecipeRepository,
    private val selectedRestaurantHolder: SelectedRestaurantHolder,
    private val json: Json,
) : MenuRepository {
    private val _menus = MutableStateFlow<List<Menu>>(emptyList())
    private var loadedRestaurantId: String? = null

    override fun getAllMenus(): Flow<List<Menu>> =
        flow {
            val restaurantId = selectedRestaurantHolder.selected.value?.id
            if (restaurantId != null && loadedRestaurantId != restaurantId) runCatching { refresh(restaurantId) }
            emitAll(_menus)
        }

    override fun getMenusByRestaurant(restaurantId: String): Flow<List<Menu>> =
        flow {
            if (loadedRestaurantId != restaurantId) runCatching { refresh(restaurantId) }
            emitAll(_menus)
        }

    private suspend fun refresh(restaurantId: String) {
        val recipesById = recipeRepository.getRecipesByRestaurant(restaurantId).first().associateBy { it.id }
        _menus.value = firestore.listDocuments(path(restaurantId)).map { it.toMenu(restaurantId, recipesById) }
        loadedRestaurantId = restaurantId
    }

    override suspend fun getMenuById(id: String): Menu? {
        if (loadedRestaurantId == null) {
            val restaurantId = selectedRestaurantHolder.selected.value?.id ?: return null
            runCatching { refresh(restaurantId) }
        }
        return _menus.value.find { it.id == id }
    }

    override suspend fun addMenu(menu: Menu): Menu {
        val restaurantId = menu.restaurantId.ifEmpty {
            selectedRestaurantHolder.selected.value?.id ?: loadedRestaurantId
        } ?: throw IllegalStateException("No restaurant selected")
        val id = menu.id.ifEmpty { Uuid.random().toString() }
        firestore.patchDocument("${path(restaurantId)}/$id", menu.toFields())
        refresh(restaurantId)
        return _menus.value.find { it.id == id } ?: menu.copy(id = id, restaurantId = restaurantId)
    }

    override suspend fun updateMenu(menu: Menu): Menu {
        val restaurantId = menu.restaurantId.ifEmpty { loadedRestaurantId }
            ?: throw IllegalStateException("No restaurant selected")
        firestore.patchDocument("${path(restaurantId)}/${menu.id}", menu.toFields())
        refresh(restaurantId)
        return _menus.value.find { it.id == menu.id } ?: menu
    }

    override suspend fun deleteMenu(id: String) {
        val restaurantId = _menus.value.find { it.id == id }?.restaurantId ?: loadedRestaurantId ?: return
        firestore.deleteDocument("${path(restaurantId)}/$id")
        refresh(restaurantId)
    }

    override suspend fun exportMenuToJson(id: String): String {
        val menu = getMenuById(id) ?: throw NoSuchElementException("Menu $id not found")
        return json.encodeToString(Menu.serializer(), menu)
    }

    override suspend fun importMenuFromJson(json: String): Menu = this.json.decodeFromString(Menu.serializer(), json)

    private fun FirestoreDocument.toMenu(
        restaurantId: String,
        recipesById: Map<String, Recipe>,
    ): Menu {
        val recipeIds = (fields["recipeIds"] as? List<Any?>).orEmpty().filterIsInstance<String>()
        val recipes = recipeIds.map { rid -> MenuRecipeSummary(id = rid, name = recipesById[rid]?.name ?: "") }
        return Menu(
            id = id,
            restaurantId = restaurantId,
            name = fields["name"] as? String ?: "",
            description = fields["description"] as? String ?: "",
            displayOrder = (fields["displayOrder"] as? Long)?.toInt() ?: 0,
            published = fields["published"] as? Boolean ?: false,
            archived = fields["archived"] as? Boolean ?: false,
            restaurantLogoUrl = fields["restaurantLogoUrl"] as? String,
            companyLogoUrl = fields["companyLogoUrl"] as? String,
            recipes = recipes,
        )
    }

    private fun Menu.toFields(): Map<String, Any?> =
        mapOf(
            "name" to name,
            "description" to description,
            "displayOrder" to displayOrder.toLong(),
            "published" to published,
            "archived" to archived,
            "restaurantLogoUrl" to restaurantLogoUrl,
            "companyLogoUrl" to companyLogoUrl,
            "recipeIds" to recipes.map { it.id },
        )

    private companion object {
        fun path(restaurantId: String) = "restaurants/$restaurantId/menus"
    }
}
