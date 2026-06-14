package org.apptolast.menuadmin.di

import kotlinx.serialization.json.Json
import org.apptolast.menuadmin.data.SelectedRestaurantHolder
import org.apptolast.menuadmin.data.local.ThemePreferences
import org.apptolast.menuadmin.data.remote.auth.AuthService
import org.apptolast.menuadmin.data.remote.auth.TokenManager
import org.apptolast.menuadmin.data.remote.createAuthHttpClient
import org.apptolast.menuadmin.data.remote.createHttpClient
import org.apptolast.menuadmin.data.remote.dish.DishService
import org.apptolast.menuadmin.data.remote.firebase.FirebaseAuthService
import org.apptolast.menuadmin.data.remote.firebase.FirebaseConfig
import org.apptolast.menuadmin.data.remote.firebase.FirestoreClient
import org.apptolast.menuadmin.data.remote.firebase.createFirestoreHttpClient
import org.apptolast.menuadmin.data.remote.ingredient.IngredientService
import org.apptolast.menuadmin.data.remote.menu.MenuService
import org.apptolast.menuadmin.data.remote.menudigitalcard.MenuDigitalCardService
import org.apptolast.menuadmin.data.remote.recipe.RecipeService
import org.apptolast.menuadmin.data.remote.restaurant.RestaurantService
import org.apptolast.menuadmin.data.repository.ApiDashboardRepository
import org.apptolast.menuadmin.data.repository.FirebaseAuthRepository
import org.apptolast.menuadmin.data.repository.FirestoreDashboardRepository
import org.apptolast.menuadmin.data.repository.FirestoreIngredientRepository
import org.apptolast.menuadmin.data.repository.FirestoreMenuRepository
import org.apptolast.menuadmin.data.repository.FirestoreRecipeRepository
import org.apptolast.menuadmin.data.repository.FirestoreRestaurantRepository
import org.apptolast.menuadmin.data.repository.RemoteAuthRepository
import org.apptolast.menuadmin.data.repository.RemoteDishRepository
import org.apptolast.menuadmin.data.repository.RemoteIngredientRepository
import org.apptolast.menuadmin.data.repository.RemoteMenuDigitalCardRepository
import org.apptolast.menuadmin.data.repository.RemoteMenuRepository
import org.apptolast.menuadmin.data.repository.RemoteRecipeRepository
import org.apptolast.menuadmin.data.repository.RemoteRestaurantRepository
import org.apptolast.menuadmin.domain.repository.AuthRepository
import org.apptolast.menuadmin.domain.repository.DashboardRepository
import org.apptolast.menuadmin.domain.repository.DishRepository
import org.apptolast.menuadmin.domain.repository.IngredientRepository
import org.apptolast.menuadmin.domain.repository.MenuDigitalCardRepository
import org.apptolast.menuadmin.domain.repository.MenuRepository
import org.apptolast.menuadmin.domain.repository.RecipeRepository
import org.apptolast.menuadmin.domain.repository.RestaurantRepository
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

val dataModule = module {
    // Shared Json instance
    single {
        Json {
            ignoreUnknownKeys = true
            prettyPrint = true
        }
    }

    // Network infrastructure
    single { TokenManager() }
    single(named("auth")) { createAuthHttpClient(get()) }
    single { createHttpClient(get(), get()) }

    // API Services
    single { AuthService(get(named("auth"))) }
    singleOf(::RestaurantService)
    singleOf(::MenuService)
    singleOf(::DishService)
    singleOf(::IngredientService)
    singleOf(::RecipeService)
    singleOf(::MenuDigitalCardService)

    // Firebase (Auth REST + Firestore REST) — works on all targets incl. wasmJs
    single { FirebaseAuthService(get(named("auth"))) }
    single(named("firestore")) { createFirestoreHttpClient(get(), get(), get()) }
    single { FirestoreClient(get(named("firestore"))) }

    // Repositories — Auth + Ingredients feature-flagged: Firestore vs custom backend
    if (FirebaseConfig.useFirestore) {
        singleOf(::FirebaseAuthRepository) bind AuthRepository::class
        singleOf(::FirestoreIngredientRepository) bind IngredientRepository::class
        singleOf(::FirestoreRestaurantRepository) bind RestaurantRepository::class
        singleOf(::FirestoreRecipeRepository) bind RecipeRepository::class
        singleOf(::FirestoreMenuRepository) bind MenuRepository::class
        singleOf(::FirestoreDashboardRepository) bind DashboardRepository::class
    } else {
        singleOf(::RemoteAuthRepository) bind AuthRepository::class
        singleOf(::RemoteIngredientRepository) bind IngredientRepository::class
        singleOf(::RemoteRestaurantRepository) bind RestaurantRepository::class
        singleOf(::RemoteRecipeRepository) bind RecipeRepository::class
        singleOf(::RemoteMenuRepository) bind MenuRepository::class
        singleOf(::ApiDashboardRepository) bind DashboardRepository::class
    }

    // Still backend-backed (dishes/digital-cards dropped in the simplified model — inert, no UI uses them)
    singleOf(::RemoteDishRepository) bind DishRepository::class
    singleOf(::RemoteMenuDigitalCardRepository) bind MenuDigitalCardRepository::class

    // Local preferences
    single { ThemePreferences() }

    // Shared state holders
    single { SelectedRestaurantHolder() }
}
