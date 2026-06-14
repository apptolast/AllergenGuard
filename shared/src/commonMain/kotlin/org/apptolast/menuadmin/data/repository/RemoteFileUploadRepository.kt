package org.apptolast.menuadmin.data.repository

import org.apptolast.menuadmin.data.remote.upload.FileUploadService
import org.apptolast.menuadmin.domain.repository.FileUploadRepository

class RemoteFileUploadRepository(
    private val fileUploadService: FileUploadService,
) : FileUploadRepository {
    override suspend fun uploadImage(
        fileBytes: ByteArray,
        fileName: String,
        contentType: String,
    ): String = fileUploadService.upload(fileBytes, fileName, contentType).url
}
