package org.apptolast.menuadmin.data.repository

import org.apptolast.menuadmin.data.remote.firebase.FirebaseStorageClient
import org.apptolast.menuadmin.domain.platform.FileHandler
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Picks + compresses a dish image and uploads it to Firebase Storage under
 * `dish-images/{restaurantId}/{uuid}.{ext}`, returning its public download URL (or null if the user
 * cancels). The extension matches the actual content type so the object previews in the console.
 */
@OptIn(ExperimentalUuidApi::class)
class DishImageUploader(
    private val fileHandler: FileHandler,
    private val storage: FirebaseStorageClient,
) {
    suspend fun pickCompressAndUpload(restaurantId: String): String? {
        val image = fileHandler.pickAndCompressImage() ?: return null
        val ext = when (image.contentType) {
            "image/webp" -> "webp"
            "image/jpeg" -> "jpg"
            "image/png" -> "png"
            else -> "img"
        }
        val path = "dish-images/$restaurantId/${Uuid.random()}.$ext"
        return storage.uploadBytes(path, image.bytes, image.contentType)
    }
}
