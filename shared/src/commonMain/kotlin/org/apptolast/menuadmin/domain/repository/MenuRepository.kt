package org.apptolast.menuadmin.domain.repository

import kotlinx.coroutines.flow.Flow
import org.apptolast.menuadmin.domain.model.Menu

interface MenuRepository {
    fun getAllMenus(): Flow<List<Menu>>

    fun getMenusByRestaurant(restaurantId: String): Flow<List<Menu>>

    suspend fun getMenuById(id: String): Menu?

    suspend fun addMenu(menu: Menu): Menu

    suspend fun updateMenu(menu: Menu): Menu

    suspend fun deleteMenu(id: String)

    suspend fun exportMenuToJson(id: String): String

    suspend fun importMenuFromJson(json: String): Menu
}
