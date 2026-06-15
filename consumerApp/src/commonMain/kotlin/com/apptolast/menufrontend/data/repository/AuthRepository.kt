package com.apptolast.menufrontend.data.repository

import com.apptolast.menufrontend.domain.model.User
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val currentUser: StateFlow<User?>
    suspend fun login(email: String, password: String): Result<User>
    suspend fun loginWithGoogle(): Result<User>
    suspend fun register(email: String, password: String, name: String): Result<User>
    suspend fun logout()
}
