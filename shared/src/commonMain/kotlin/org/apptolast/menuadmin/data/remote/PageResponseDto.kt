package org.apptolast.menuadmin.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class PageResponseDto<T>(
    val content: List<T>,
    val page: Int = 0,
    val size: Int = 20,
    val totalElements: Long = 0,
    val totalPages: Int = 0,
    val last: Boolean = true,
)
