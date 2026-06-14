package org.apptolast.menuadmin.data.remote.upload

import kotlinx.serialization.Serializable

@Serializable
data class UploadResponseDto(
    val url: String,
)
