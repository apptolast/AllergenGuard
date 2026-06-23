package org.apptolast.menuadmin.domain.repository

interface AuthRepository {
    val isLoggedIn: Boolean

    /** Email of the logged-in user, recovered from the stored token. Null if unknown. */
    val currentUserEmail: String?

    /** Stable user id (uid) of the logged-in user. Null if unknown. */
    val currentUserId: String?

    /** Display name of the logged-in user, recovered from the stored token. Null if not set. */
    val currentUserName: String?

    /** Whether the logged-in user's email has been verified. */
    val isEmailVerified: Boolean

    suspend fun login(
        email: String,
        password: String,
    )

    suspend fun registerAdmin(
        email: String,
        password: String,
        name: String? = null,
    )

    /** Updates the signed-in user's display name and refreshes the stored session so it takes effect. */
    suspend fun updateDisplayName(name: String)

    fun logout()
}
