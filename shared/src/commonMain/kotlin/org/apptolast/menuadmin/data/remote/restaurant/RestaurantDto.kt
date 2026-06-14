package org.apptolast.menuadmin.data.remote.restaurant

import kotlinx.serialization.Serializable

@Serializable
data class RestaurantResponseDto(
    val id: String,
    val name: String,
    val slug: String,
    val description: String = "",
    val address: String = "",
    val phone: String = "",
    val logoUrl: String? = null,
    val createdAt: String? = null,
    val active: Boolean = true,
)

@Serializable
data class RestaurantRequestDto(
    val name: String,
    val slug: String,
    val description: String? = null,
    val address: String? = null,
    val phone: String? = null,
    val logoUrl: String? = null,
)
