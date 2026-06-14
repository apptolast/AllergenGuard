package org.apptolast.menuadmin.data.remote.upload

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import org.apptolast.menuadmin.data.remote.ApiConstants

class FileUploadService(
    private val client: HttpClient,
) {
    suspend fun upload(
        fileBytes: ByteArray,
        fileName: String,
        contentType: String = "application/octet-stream",
    ): UploadResponseDto =
        client.submitFormWithBinaryData(
            url = ApiConstants.ADMIN_UPLOAD,
            formData = formData {
                append(
                    key = "file",
                    value = fileBytes,
                    headers = Headers.build {
                        append(HttpHeaders.ContentType, contentType)
                        append(
                            HttpHeaders.ContentDisposition,
                            "form-data; name=\"file\"; filename=\"$fileName\"",
                        )
                    },
                )
            },
        ).body()
}
