package org.apptolast.menuadmin.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import org.apptolast.menuadmin.data.remote.mapper.toDomain
import org.apptolast.menuadmin.data.remote.menudigitalcard.CreateMenuDigitalCardRequestDto
import org.apptolast.menuadmin.data.remote.menudigitalcard.MenuDigitalCardService
import org.apptolast.menuadmin.data.remote.menudigitalcard.UpdateMenuDigitalCardRequestDto
import org.apptolast.menuadmin.domain.model.MenuDigitalCard
import org.apptolast.menuadmin.domain.repository.MenuDigitalCardRepository

class RemoteMenuDigitalCardRepository(
    private val menuDigitalCardService: MenuDigitalCardService,
) : MenuDigitalCardRepository {
    private val _cards = MutableStateFlow<List<MenuDigitalCard>>(emptyList())
    private var loadedMenuId: String? = null

    override fun getCardsByMenu(menuId: String): Flow<List<MenuDigitalCard>> =
        flow {
            if (loadedMenuId != menuId) {
                refreshCards(menuId)
            }
            emitAll(_cards)
        }

    private suspend fun refreshCards(menuId: String) {
        _cards.value = menuDigitalCardService.getByMenuId(menuId).map { it.toDomain() }
        loadedMenuId = menuId
    }

    override suspend fun addDishToCard(
        menuId: String,
        dishId: String,
    ): MenuDigitalCard {
        val response = menuDigitalCardService.create(
            CreateMenuDigitalCardRequestDto(menuId = menuId, dishId = dishId),
        )
        val created = response.toDomain()
        loadedMenuId?.let { refreshCards(it) }
        return created
    }

    override suspend fun updateCard(
        id: String,
        dishId: String,
    ): MenuDigitalCard {
        val response = menuDigitalCardService.update(
            id = id,
            request = UpdateMenuDigitalCardRequestDto(dishId = dishId),
        )
        val updated = response.toDomain()
        loadedMenuId?.let { refreshCards(it) }
        return updated
    }

    override suspend fun removeCard(id: String) {
        menuDigitalCardService.delete(id)
        loadedMenuId?.let { refreshCards(it) }
    }
}
