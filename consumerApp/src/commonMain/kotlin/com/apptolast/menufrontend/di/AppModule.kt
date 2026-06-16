package com.apptolast.menufrontend.di

import com.apptolast.menufrontend.data.firebase.FirebaseAuthRepository
import com.apptolast.menufrontend.data.firebase.FirestoreRestaurantRepository
import com.apptolast.menufrontend.data.firebase.FirestoreUserRepository
import com.apptolast.menufrontend.data.repository.AuthRepository
import com.apptolast.menufrontend.data.repository.RestaurantRepository
import com.apptolast.menufrontend.data.repository.UserRepository
import com.apptolast.menufrontend.features.dishdetail.presentation.DishDetailViewModel
import com.apptolast.menufrontend.features.favorites.presentation.FavoritesViewModel
import com.apptolast.menufrontend.features.home.presentation.HomeViewModel
import com.apptolast.menufrontend.features.login.presentation.LoginViewModel
import com.apptolast.menufrontend.features.menu.presentation.MenuViewModel
import com.apptolast.menufrontend.features.profile.presentation.ProfileViewModel
import com.apptolast.menufrontend.features.scanner.presentation.ScannerViewModel
import kotlinx.serialization.json.Json
import org.apptolast.menuadmin.data.remote.auth.TokenManager
import org.apptolast.menuadmin.data.remote.createAuthHttpClient
import org.apptolast.menuadmin.data.remote.firebase.FirebaseAuthService
import org.apptolast.menuadmin.data.remote.firebase.FirestoreClient
import org.apptolast.menuadmin.data.remote.firebase.createFirestoreHttpClient
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.bind
import org.koin.dsl.module

// Firebase infrastructure reused from :shared (REST: Identity Toolkit Auth + Firestore).
val firebaseModule = module {
    single { Json { ignoreUnknownKeys = true } }
    single { TokenManager() }
    single(named("auth")) { createAuthHttpClient(get()) }
    single { FirebaseAuthService(get(named("auth"))) }
    single(named("firestore")) { createFirestoreHttpClient(get(), get(), get()) }
    single { FirestoreClient(get(named("firestore"))) }
}

val repositoryModule = module {
    singleOf(::FirebaseAuthRepository) bind AuthRepository::class
    singleOf(::FirestoreRestaurantRepository) bind RestaurantRepository::class
    singleOf(::FirestoreUserRepository) bind UserRepository::class
}

val viewModelModule = module {
    viewModelOf(::LoginViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::FavoritesViewModel)
    viewModelOf(::MenuViewModel)
    viewModelOf(::ProfileViewModel)
    viewModelOf(::DishDetailViewModel)
    viewModelOf(::ScannerViewModel)
}

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(platformModule, firebaseModule, repositoryModule, viewModelModule)
    }
}
