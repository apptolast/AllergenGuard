package com.apptolast.menufrontend.di

import com.apptolast.menufrontend.data.fake.FakeAuthRepository
import com.apptolast.menufrontend.data.fake.FakeRestaurantRepository
import com.apptolast.menufrontend.data.fake.FakeUserRepository
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
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.bind
import org.koin.dsl.module

val repositoryModule = module {
    singleOf(::FakeAuthRepository) bind AuthRepository::class
    singleOf(::FakeRestaurantRepository) bind RestaurantRepository::class
    singleOf(::FakeUserRepository) bind UserRepository::class
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
        modules(repositoryModule, viewModelModule)
    }
}
