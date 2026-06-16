package org.apptolast.menuadmin.domain.platform

interface FileHandler {
    suspend fun pickAndReadFile(): String?

    suspend fun saveFile(
        content: String,
        fileName: String,
    )

    /**
     * Lets the user pick an image, then downscales and re-encodes it client-side (default WebP) so
     * the uploaded file stays small. Returns the compressed bytes plus their actual content type
     * (which may differ from [preferredMimeType] if the browser can't encode it), or null if the
     * user cancels.
     */
    suspend fun pickAndCompressImage(
        maxDimension: Int = 1280,
        preferredMimeType: String = "image/webp",
        quality: Double = 0.82,
    ): PickedImage?
}

/** A compressed image ready to upload. */
data class PickedImage(
    val bytes: ByteArray,
    val contentType: String,
) {
    override fun equals(other: Any?): Boolean =
        this === other ||
            (other is PickedImage && contentType == other.contentType && bytes.contentEquals(other.bytes))

    override fun hashCode(): Int = 31 * bytes.contentHashCode() + contentType.hashCode()
}
