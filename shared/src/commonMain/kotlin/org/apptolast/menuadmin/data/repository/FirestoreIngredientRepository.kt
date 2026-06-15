package org.apptolast.menuadmin.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import org.apptolast.menuadmin.data.remote.firebase.FirestoreClient
import org.apptolast.menuadmin.data.remote.firebase.FirestoreDocument
import org.apptolast.menuadmin.domain.model.AllergenType
import org.apptolast.menuadmin.domain.model.ContainmentLevel
import org.apptolast.menuadmin.domain.model.Ingredient
import org.apptolast.menuadmin.domain.model.IngredientAllergen
import org.apptolast.menuadmin.domain.repository.IngredientRepository
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * [IngredientRepository] backed by the global Firestore `ingredients` collection. Same caching
 * (StateFlow + lazy load) behaviour as the backend version, so the UI/ViewModels are unchanged.
 */
@OptIn(ExperimentalUuidApi::class)
class FirestoreIngredientRepository(
    private val firestore: FirestoreClient,
) : IngredientRepository {
    private val _ingredients = MutableStateFlow<List<Ingredient>>(emptyList())
    private var hasLoaded = false

    override fun getAllIngredients(): Flow<List<Ingredient>> =
        flow {
            if (!hasLoaded) runCatching { refresh() }
            emitAll(_ingredients)
        }

    private suspend fun refresh() {
        _ingredients.value = firestore.listDocuments(COLLECTION).map { it.toIngredient() }
        hasLoaded = true
    }

    override suspend fun getIngredientById(id: String): Ingredient? =
        firestore.getDocument("$COLLECTION/$id")?.toIngredient()

    override suspend fun addIngredient(ingredient: Ingredient): Ingredient {
        val id = ingredient.id.ifEmpty { Uuid.random().toString() }
        firestore.patchDocument("$COLLECTION/$id", ingredient.toFields())
        refresh()
        return _ingredients.value.find { it.id == id } ?: ingredient.copy(id = id)
    }

    override suspend fun updateIngredient(ingredient: Ingredient): Ingredient {
        firestore.patchDocument("$COLLECTION/${ingredient.id}", ingredient.toFields())
        refresh()
        return _ingredients.value.find { it.id == ingredient.id } ?: ingredient
    }

    override suspend fun deleteIngredient(id: String) {
        firestore.deleteDocument("$COLLECTION/$id")
        refresh()
    }

    override suspend fun searchIngredients(query: String): List<Ingredient> {
        if (!hasLoaded) runCatching { refresh() }
        return _ingredients.value.filter { it.name.contains(query, ignoreCase = true) }
    }

    override suspend fun replaceAll(ingredients: List<Ingredient>) {
        runCatching { refresh() }
    }

    private fun FirestoreDocument.toIngredient(): Ingredient {
        @Suppress("UNCHECKED_CAST")
        val rawAllergens = (fields["allergens"] as? List<Any?>).orEmpty()
        val allergens = rawAllergens.mapNotNull { entry ->
            val m = entry as? Map<*, *> ?: return@mapNotNull null
            val code = m["code"] as? String ?: return@mapNotNull null
            val type = AllergenType.fromApiCode(code)
            IngredientAllergen(
                allergenId = type?.id ?: 0,
                allergenCode = code,
                allergenName = type?.nameEs ?: "",
                containmentLevel = ContainmentLevel.fromApi(m["level"] as? String ?: "CONTAINS"),
            )
        }
        return Ingredient(
            id = id,
            name = fields["name"] as? String ?: "",
            description = fields["description"] as? String ?: "",
            brand = fields["brand"] as? String ?: "",
            labelInfo = fields["labelInfo"] as? String ?: "",
            allergens = allergens,
            createdAt = createTime ?: Instant.DISTANT_PAST,
            updatedAt = updateTime ?: Instant.DISTANT_PAST,
        )
    }

    private fun Ingredient.toFields(): Map<String, Any?> =
        mapOf(
            "name" to name,
            "brand" to brand,
            "labelInfo" to labelInfo,
            "description" to description,
            "allergens" to allergens.map {
                mapOf("code" to it.allergenCode, "level" to it.containmentLevel.apiValue)
            },
        )

    private companion object {
        const val COLLECTION = "ingredients"
    }
}
