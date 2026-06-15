package org.apptolast.menuadmin.domain.platform

interface FileHandler {
    suspend fun pickAndReadFile(): String?

    suspend fun saveFile(
        content: String,
        fileName: String,
    )
}
