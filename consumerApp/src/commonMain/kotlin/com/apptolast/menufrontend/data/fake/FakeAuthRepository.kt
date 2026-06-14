package com.apptolast.menufrontend.data.fake

import com.apptolast.menufrontend.data.repository.AuthRepository
import com.apptolast.menufrontend.domain.model.Allergen
import com.apptolast.menufrontend.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeAuthRepository : AuthRepository {

    private val fakeUser = User(
        id = "user-1",
        name = "Hugo Garcia",
        email = "hugo@example.com",
        allergens = setOf(Allergen.FISH, Allergen.DAIRY),
        favoriteRestaurantIds = setOf("rest-1"),
    )

    private val _currentUser = MutableStateFlow<User?>(null)
    override val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    override suspend fun login(email: String, password: String): Result<User> {
        _currentUser.value = fakeUser.copy(email = email)
        return Result.success(_currentUser.value!!)
    }

    override suspend fun loginWithGoogle(): Result<User> {
        _currentUser.value = fakeUser
        return Result.success(fakeUser)
    }

    override suspend fun register(email: String, password: String, name: String): Result<User> {
        val user = fakeUser.copy(email = email, name = name)
        _currentUser.value = user
        return Result.success(user)
    }

    override suspend fun logout() {
        _currentUser.value = null
    }
}
