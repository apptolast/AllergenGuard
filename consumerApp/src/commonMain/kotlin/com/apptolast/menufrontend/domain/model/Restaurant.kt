package com.apptolast.menufrontend.domain.model

data class Restaurant(
    val id: String,
    val name: String,
    val cuisineType: String,
    val rating: Float,
    val reviewCount: Int,
    val dishCount: Int,
    val imageUrl: String? = null,
    val distance: String? = null,
)
