package org.apptolast.menuadmin.domain.model

data class Restaurant(
    val id: String = "",
    val name: String = "",
    val slug: String = "",
    val description: String = "",
    val address: String = "",
    val phone: String = "",
    val logoUrl: String? = null,
    val active: Boolean = true,
)
