package org.apptolast.menuadmin.data.remote.menudigitalcard

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import org.apptolast.menuadmin.data.remote.ApiConstants

class MenuDigitalCardService(
    private val client: HttpClient,
) {
    suspend fun getByMenuId(menuId: String): List<MenuDigitalCardResponseDto> =
        client.get("${ApiConstants.ADMIN_MENU_DIGITAL_CARDS}/$menuId").body()

    suspend fun create(request: CreateMenuDigitalCardRequestDto): MenuDigitalCardResponseDto =
        client.post(ApiConstants.ADMIN_MENU_DIGITAL_CARDS) {
            setBody(request)
        }.body()

    suspend fun update(
        id: String,
        request: UpdateMenuDigitalCardRequestDto,
    ): MenuDigitalCardResponseDto =
        client.put("${ApiConstants.ADMIN_MENU_DIGITAL_CARDS}/$id") {
            setBody(request)
        }.body()

    suspend fun delete(id: String) {
        client.delete("${ApiConstants.ADMIN_MENU_DIGITAL_CARDS}/$id")
    }
}
