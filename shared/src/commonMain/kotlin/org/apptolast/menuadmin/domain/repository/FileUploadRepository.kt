package org.apptolast.menuadmin.domain.repository

interface FileUploadRepository {
    suspend fun uploadImage(
        fileBytes: ByteArray,
        fileName: String,
        contentType: String = "image/jpeg",
    ): String
}
