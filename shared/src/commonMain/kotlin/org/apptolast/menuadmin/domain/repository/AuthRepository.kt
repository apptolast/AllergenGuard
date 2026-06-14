package org.apptolast.menuadmin.domain.repository

interface AuthRepository {
    val isLoggedIn: Boolean

    suspend fun login(
        email: String,
        password: String,
    )

    suspend fun registerAdmin(
        email: String,
        password: String,
        name: String? = null,
    )

    fun logout()
}
