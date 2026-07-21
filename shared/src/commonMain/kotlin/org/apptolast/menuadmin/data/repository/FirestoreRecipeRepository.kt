package org.apptolast.menuadmin.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import org.apptolast.menuadmin.data.remote.firebase.FirestoreClient
import org.apptolast.menuadmin.data.remote.firebase.FirestoreDocument
import org.apptolast.menuadmin.domain.model.AllergenType
import org.apptolast.menuadmin.domain.model.Recipe
import org.apptolast.menuadmin.domain.model.RecipeComponentType
import org.apptolast.menuadmin.domain.model.RecipeIngredient
import org.apptolast.menuadmin.domain.repository.IngredientRepository
import org.apptolast.menuadmin.domain.repository.RecipeRepository
import org.apptolast.menuadmin.domain.util.RecipeAllergenAggregator
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * [RecipeRepository] backed by the `restaurants/{restaurantId}/recipes` subcollection.
 *
 * `computedAllergens` are derived on the client from the selected ingredients' allergens (taking the
 * strongest containment level, ignoring FREE_OF) and persisted on the recipe document — so consumers
 * read them without joins. Recipes are cached per restaurant; id-only operations resolve the
 * restaurant from the cached recipe (the nested path needs it).
 */
@OptIn(ExperimentalUuidApi::class)
class FirestoreRecipeRepository(
    private val firestore: FirestoreClient,
    private val ingredientRepository: IngredientRepository,
) : RecipeRepository {
    private val _recipes = MutableStateFlow<List<Recipe>>(emptyList())
    private var loadedRestaurantId: String? = null

    override fun getAllRecipes(): Flow<List<Recipe>> = _recipes

    override fun getRecipesByRestaurant(restaurantId: String): Flow<List<Recipe>> =
        flow {
            if (loadedRestaurantId != restaurantId) runCatching { refresh(restaurantId) }
            emitAll(_recipes)
        }

    private suspend fun refresh(restaurantId: String) {
        _recipes.value = firestore.listDocuments(path(restaurantId)).map { it.toRecipe(restaurantId) }
        loadedRestaurantId = restaurantId
    }

    override suspend fun getRecipeById(id: String): Recipe? {
        val cached = _recipes.value.find { it.id == id }
        val restaurantId = cached?.restaurantId ?: loadedRestaurantId ?: return cached
        return firestore.getDocument("${path(restaurantId)}/$id")?.toRecipe(restaurantId) ?: cached
    }

    override suspend fun addRecipe(recipe: Recipe): Recipe {
        val restaurantId = recipe.restaurantId.ifEmpty { loadedRestaurantId }
            ?: throw IllegalStateException("No restaurant selected")
        val id = recipe.id.ifEmpty { Uuid.random().toString() }
        firestore.patchDocument("${path(restaurantId)}/$id", recipe.toFields(computeAllergens(recipe)))
        refresh(restaurantId)
        return _recipes.value.find { it.id == id } ?: recipe.copy(id = id, restaurantId = restaurantId)
    }

    override suspend fun updateRecipe(recipe: Recipe): Recipe {
        val restaurantId = recipe.restaurantId.ifEmpty { loadedRestaurantId }
            ?: throw IllegalStateException("No restaurant selected")
        firestore.patchDocument("${path(restaurantId)}/${recipe.id}", recipe.toFields(computeAllergens(recipe)))
        refresh(restaurantId)
        return _recipes.value.find { it.id == recipe.id } ?: recipe
    }

    override suspend fun deleteRecipe(id: String) {
        val restaurantId = _recipes.value.find { it.id == id }?.restaurantId ?: loadedRestaurantId ?: return
        firestore.deleteDocument("${path(restaurantId)}/$id")
        refresh(restaurantId)
    }

    override suspend fun toggleRecipeActive(id: String): Recipe {
        val current = _recipes.value.find { it.id == id }
            ?: throw NoSuchElementException("Recipe with id $id not found")
        return updateRecipe(current.copy(isActive = !current.isActive))
    }

    override suspend fun replaceAll(recipes: List<Recipe>) {
        loadedRestaurantId?.let { runCatching { refresh(it) } }
    }

    /**
     * code -> strongest containment level. Aggregates the selected ingredients' allergens AND, for
     * SUB_RECIPE components, the allergens of the referenced recipe (recursively) via
     * [RecipeAllergenAggregator], resolved against the cached recipes of the restaurant. FREE_OF ignored.
     */
    private suspend fun computeAllergens(recipe: Recipe): List<Map<String, Any?>> {
        val byId = ingredientRepository.getAllIngredients().first().associateBy { it.id }
        val recipesById = _recipes.value.associateBy { it.id }
        return RecipeAllergenAggregator.computeAllergens(recipe, byId, recipesById)
            .map { (code, level) -> mapOf("code" to code, "level" to level.apiValue) }
    }

    private fun FirestoreDocument.toRecipe(restaurantId: String): Recipe {
        val ingredients = (fields["ingredients"] as? List<Any?>).orEmpty().mapNotNull { e ->
            val m = e as? Map<*, *> ?: return@mapNotNull null
            val ingredientId = m["ingredientId"] as? String ?: return@mapNotNull null
            RecipeIngredient(
                ingredientId = ingredientId,
                ingredientName = m["name"] as? String ?: "",
                quantity = (m["qty"] as? Double) ?: (m["qty"] as? Long)?.toDouble() ?: 0.0,
                unit = m["unit"] as? String ?: "",
                type = if ((m["type"] as? String) == "recipe") {
                    RecipeComponentType.SUB_RECIPE
                } else {
                    RecipeComponentType.INGREDIENT
                },
            )
        }
        val allergens = (fields["computedAllergens"] as? List<Any?>).orEmpty().mapNotNull { e ->
            val m = e as? Map<*, *> ?: return@mapNotNull null
            AllergenType.fromApiCode(m["code"] as? String ?: "")
        }.toSet()
        return Recipe(
            id = id,
            restaurantId = restaurantId,
            name = fields["name"] as? String ?: "",
            description = fields["description"] as? String ?: "",
            category = fields["section"] as? String ?: "",
            imageUrl = fields["imageUrl"] as? String,
            price = (fields["price"] as? Double) ?: (fields["price"] as? Long)?.toDouble() ?: 0.0,
            isActive = fields["active"] as? Boolean ?: true,
            isSubRecipe = fields["isSubRecipe"] as? Boolean ?: false,
            ingredients = ingredients,
            computedAllergens = allergens,
            ingredientCount = ingredients.size,
            allergenCount = allergens.size,
            createdAt = createTime ?: Instant.DISTANT_PAST,
            updatedAt = updateTime ?: Instant.DISTANT_PAST,
        )
    }

    private fun Recipe.toFields(computedAllergens: List<Map<String, Any?>>): Map<String, Any?> =
        mapOf(
            "name" to name,
            "description" to description,
            "section" to category,
            "imageUrl" to imageUrl,
            "price" to (if (price > 0) price else null),
            "active" to isActive,
            "ingredients" to ingredients.map { ri ->
                buildMap<String, Any?> {
                    put("ingredientId", ri.ingredientId)
                    put("name", ri.ingredientName)
                    if (ri.quantity > 0) put("qty", ri.quantity)
                    if (ri.unit.isNotEmpty()) put("unit", ri.unit)
                    // Persist the component type only for sub-recipes (backward-compatible: absent = ingredient).
                    if (ri.type == RecipeComponentType.SUB_RECIPE) put("type", "recipe")
                }
            },
            "computedAllergens" to computedAllergens,
            "isSubRecipe" to isSubRecipe,
        )

    private companion object {
        fun path(restaurantId: String) = "restaurants/$restaurantId/recipes"
    }
}
