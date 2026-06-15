package org.apptolast.menuadmin.data.remote.menu

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import org.apptolast.menuadmin.data.remote.ApiConstants

class MenuService(
    private val client: HttpClient,
) {
    // Menus (scoped to restaurant)
    suspend fun getMenus(
        restaurantId: String,
        archived: Boolean = false,
    ): List<MenuResponseDto> =
        client.get("${ApiConstants.ADMIN_RESTAURANTS}/$restaurantId/menus") {
            parameter("archived", archived)
        }.body()

    suspend fun createMenu(
        restaurantId: String,
        request: MenuRequestDto,
    ): MenuResponseDto =
        client.post("${ApiConstants.ADMIN_RESTAURANTS}/$restaurantId/menus") {
            setBody(request)
        }.body()

    suspend fun updateMenu(
        id: String,
        request: MenuRequestDto,
    ): MenuResponseDto =
        client.put("${ApiConstants.ADMIN_MENUS}/$id") {
            setBody(request)
        }.body()

    suspend fun archiveMenu(id: String) {
        client.delete("${ApiConstants.ADMIN_MENUS}/$id")
    }

    // Sections
    suspend fun createSection(
        menuId: String,
        request: SectionRequestDto,
    ): SectionResponseDto =
        client.post("${ApiConstants.ADMIN_MENUS}/$menuId/sections") {
            setBody(request)
        }.body()

    suspend fun updateSection(
        menuId: String,
        sectionId: String,
        request: SectionRequestDto,
    ): SectionResponseDto =
        client.put("${ApiConstants.ADMIN_MENUS}/$menuId/sections/$sectionId") {
            setBody(request)
        }.body()

    suspend fun deleteSection(
        menuId: String,
        sectionId: String,
    ) {
        client.delete("${ApiConstants.ADMIN_MENUS}/$menuId/sections/$sectionId")
    }

    // Publish
    suspend fun publishMenu(
        id: String,
        request: MenuPublishRequestDto,
    ): MenuResponseDto =
        client.put("${ApiConstants.ADMIN_MENUS}/$id/publish") {
            setBody(request)
        }.body()

    // Menu-Recipe associations
    suspend fun updateMenuRecipes(
        menuId: String,
        request: UpdateMenuRecipesRequestDto,
    ): MenuResponseDto =
        client.put("${ApiConstants.ADMIN_MENUS}/$menuId/recipes") {
            setBody(request)
        }.body()
}
