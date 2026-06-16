package org.apptolast.menuadmin.data.repository

import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import org.apptolast.menuadmin.data.SelectedRestaurantHolder
import org.apptolast.menuadmin.data.remote.dish.DishService
import org.apptolast.menuadmin.data.remote.mapper.toDomain
import org.apptolast.menuadmin.data.remote.menu.MenuRequestDto
import org.apptolast.menuadmin.data.remote.menu.MenuService
import org.apptolast.menuadmin.data.remote.menu.SectionRequestDto
import org.apptolast.menuadmin.domain.model.Menu
import org.apptolast.menuadmin.domain.model.Section
import org.apptolast.menuadmin.domain.repository.MenuRepository

class RemoteMenuRepository(
    private val menuService: MenuService,
    private val dishService: DishService,
    private val selectedRestaurantHolder: SelectedRestaurantHolder,
    private val json: Json,
) : MenuRepository {
    private val _menus = MutableStateFlow<List<Menu>>(emptyList())
    private var loadedRestaurantId: String? = null

    override fun getAllMenus(): Flow<List<Menu>> =
        flow {
            val restaurantId = selectedRestaurantHolder.selected.value?.id
            if (restaurantId != null && loadedRestaurantId != restaurantId) {
                try {
                    refreshMenus(restaurantId)
                } catch (e: ClientRequestException) {
                    if (e.response.status != HttpStatusCode.BadRequest) throw e
                } catch (_: Exception) {
                }
            }
            emitAll(_menus)
        }

    override fun getMenusByRestaurant(restaurantId: String): Flow<List<Menu>> =
        flow {
            if (loadedRestaurantId != restaurantId) {
                try {
                    refreshMenus(restaurantId)
                } catch (e: ClientRequestException) {
                    if (e.response.status != HttpStatusCode.BadRequest) throw e
                } catch (_: Exception) {
                }
            }
            emitAll(_menus)
        }

    private suspend fun refreshMenus(restaurantId: String) {
        val menuDtos = menuService.getMenus(restaurantId)
        val dishDtos = dishService.getDishes(restaurantId)

        // Group dishes by sectionId
        val domainDishes = dishDtos.map { it.toDomain() }
        val dishesBySection = domainDishes.groupBy { it.sectionId }

        _menus.value = menuDtos.map { menuDto ->
            val sections = menuDto.sections.map { sectionDto ->
                val sectionDishes = dishesBySection[sectionDto.id] ?: emptyList()
                Section(
                    id = sectionDto.id,
                    name = sectionDto.name,
                    displayOrder = sectionDto.displayOrder,
                    dishes = sectionDishes,
                )
            }
            val allDishes = sections.flatMap { it.dishes }
            Menu(
                id = menuDto.id,
                restaurantId = restaurantId,
                name = menuDto.name,
                description = menuDto.description,
                displayOrder = menuDto.displayOrder,
                sections = sections,
                published = menuDto.published,
                archived = menuDto.archived,
                restaurantLogoUrl = menuDto.restaurantLogoUrl,
                companyLogoUrl = menuDto.companyLogoUrl,
                recipes = menuDto.recipes.map { it.toDomain() },
                dishes = allDishes,
            )
        }
        loadedRestaurantId = restaurantId
    }

    override suspend fun getMenuById(id: String): Menu? {
        if (loadedRestaurantId == null) {
            val restaurantId = selectedRestaurantHolder.selected.value?.id ?: return null
            refreshMenus(restaurantId)
        }
        return _menus.value.find { it.id == id }
    }

    override suspend fun addMenu(menu: Menu): Menu {
        val restaurantId = menu.restaurantId.ifEmpty {
            selectedRestaurantHolder.selected.value?.id ?: loadedRestaurantId
        } ?: throw IllegalStateException("No restaurant selected")

        val response = menuService.createMenu(
            restaurantId,
            MenuRequestDto(
                name = menu.name,
                description = menu.description.ifEmpty { null },
                displayOrder = menu.displayOrder,
                restaurantLogoUrl = menu.restaurantLogoUrl,
                companyLogoUrl = menu.companyLogoUrl,
                recipeIds = menu.recipes.map { it.id }.ifEmpty { null },
            ),
        )
        val created = response.toDomain()
        refreshMenus(restaurantId)
        return created
    }

    override suspend fun updateMenu(menu: Menu): Menu {
        val response = menuService.updateMenu(
            menu.id,
            MenuRequestDto(
                name = menu.name,
                description = menu.description.ifEmpty { null },
                displayOrder = menu.displayOrder,
                restaurantLogoUrl = menu.restaurantLogoUrl,
                companyLogoUrl = menu.companyLogoUrl,
                // Always send recipeIds on update (PUT semantics: full replacement)
                // null = keep existing, [] = clear all, [...] = replace
                recipeIds = menu.recipes.map { it.id },
            ),
        )
        val updated = response.toDomain()
        loadedRestaurantId?.let { refreshMenus(it) }
        return updated
    }

    override suspend fun deleteMenu(id: String) {
        menuService.archiveMenu(id)
        loadedRestaurantId?.let { refreshMenus(it) }
    }

    // Active-menu selection is only supported on the Firestore data path (USE_FIRESTORE). The legacy
    // REST backend is inert in the current build, so this is a no-op here.
    override suspend fun setMenuPublished(
        restaurantId: String,
        menuId: String,
        published: Boolean,
    ) = Unit

    // Sections
    suspend fun addSection(
        menuId: String,
        name: String,
        displayOrder: Int,
    ): Section {
        val response = menuService.createSection(
            menuId,
            SectionRequestDto(name = name, displayOrder = displayOrder),
        )
        loadedRestaurantId?.let { refreshMenus(it) }
        return response.toDomain()
    }

    suspend fun updateSection(
        menuId: String,
        sectionId: String,
        name: String,
        displayOrder: Int,
    ): Section {
        val response = menuService.updateSection(
            menuId,
            sectionId,
            SectionRequestDto(name = name, displayOrder = displayOrder),
        )
        loadedRestaurantId?.let { refreshMenus(it) }
        return response.toDomain()
    }

    suspend fun deleteSection(
        menuId: String,
        sectionId: String,
    ) {
        menuService.deleteSection(menuId, sectionId)
        loadedRestaurantId?.let { refreshMenus(it) }
    }

    override suspend fun exportMenuToJson(id: String): String {
        val menu = getMenuById(id) ?: throw NoSuchElementException("Menu $id not found")
        return json.encodeToString(Menu.serializer(), menu)
    }

    override suspend fun importMenuFromJson(json: String): Menu {
        return this.json.decodeFromString(Menu.serializer(), json)
    }
}
