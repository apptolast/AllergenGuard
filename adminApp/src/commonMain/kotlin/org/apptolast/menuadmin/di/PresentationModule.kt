package org.apptolast.menuadmin.di

import org.apptolast.menuadmin.presentation.components.SnackbarController
import org.apptolast.menuadmin.presentation.screens.auth.AuthViewModel
import org.apptolast.menuadmin.presentation.screens.backup.BackupViewModel
import org.apptolast.menuadmin.presentation.screens.cartadigital.CartaDigitalViewModel
import org.apptolast.menuadmin.presentation.screens.dashboard.DashboardViewModel
import org.apptolast.menuadmin.presentation.screens.ingredients.IngredientsViewModel
import org.apptolast.menuadmin.presentation.screens.menus.MenusViewModel
import org.apptolast.menuadmin.presentation.screens.platform.accountdetail.PlatformAccountDetailViewModel
import org.apptolast.menuadmin.presentation.screens.platform.accounts.PlatformAccountsViewModel
import org.apptolast.menuadmin.presentation.screens.profile.ProfileViewModel
import org.apptolast.menuadmin.presentation.screens.recipes.RecipesViewModel
import org.apptolast.menuadmin.presentation.screens.restaurants.RestaurantsListViewModel
import org.apptolast.menuadmin.presentation.screens.restaurants.detail.RestaurantDetailViewModel
import org.apptolast.menuadmin.presentation.screens.settings.SettingsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val presentationModule = module {
    single { SnackbarController() }
    viewModelOf(::AuthViewModel)
    viewModelOf(::DashboardViewModel)
    viewModelOf(::IngredientsViewModel)
    viewModel { (restaurantId: String) ->
        RecipesViewModel(get(), get(), get(), restaurantId)
    }
    viewModel { (restaurantId: String) ->
        MenusViewModel(get(), get(), get(), get(), get(), restaurantId)
    }
    viewModel { (restaurantId: String) ->
        CartaDigitalViewModel(get(), get(), restaurantId)
    }
    viewModel { BackupViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { SettingsViewModel(get(), get()) }
    viewModelOf(::ProfileViewModel)
    viewModelOf(::RestaurantsListViewModel)
    viewModelOf(::RestaurantDetailViewModel)
    viewModelOf(::PlatformAccountsViewModel)
    viewModel { (accountId: String) ->
        PlatformAccountDetailViewModel(get(), get(), accountId)
    }
}
