package org.apptolast.menuadmin.domain.repository

import kotlinx.coroutines.flow.Flow
import org.apptolast.menuadmin.domain.model.MenuDigitalCard

interface MenuDigitalCardRepository {
    fun getCardsByMenu(menuId: String): Flow<List<MenuDigitalCard>>

    suspend fun addDishToCard(
        menuId: String,
        dishId: String,
    ): MenuDigitalCard

    suspend fun updateCard(
        id: String,
        dishId: String,
    ): MenuDigitalCard

    suspend fun removeCard(id: String)
}
