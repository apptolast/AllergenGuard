package org.apptolast.menuadmin.data.remote.menudigitalcard

import kotlinx.serialization.Serializable

// --- Responses ---

@Serializable
data class MenuDigitalCardResponseDto(
    val id: String,
    val menuId: String,
    val dishId: String,
    val dishName: String,
    val createdAt: String,
    val updatedAt: String,
)

// --- Requests ---

@Serializable
data class CreateMenuDigitalCardRequestDto(
    val menuId: String,
    val dishId: String,
)

@Serializable
data class UpdateMenuDigitalCardRequestDto(
    val dishId: String,
)
