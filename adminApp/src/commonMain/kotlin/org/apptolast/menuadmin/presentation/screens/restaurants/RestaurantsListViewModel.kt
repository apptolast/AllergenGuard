package org.apptolast.menuadmin.presentation.screens.restaurants

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import menuadmin.adminapp.generated.resources.Res
import menuadmin.adminapp.generated.resources.error_unknown
import menuadmin.adminapp.generated.resources.restaurants_created
import menuadmin.adminapp.generated.resources.restaurants_deleted
import menuadmin.adminapp.generated.resources.restaurants_error_deleting
import menuadmin.adminapp.generated.resources.restaurants_error_loading
import menuadmin.adminapp.generated.resources.restaurants_error_saving
import menuadmin.adminapp.generated.resources.restaurants_updated
import org.apptolast.menuadmin.data.CurrentAccountHolder
import org.apptolast.menuadmin.domain.model.Restaurant
import org.apptolast.menuadmin.domain.repository.RestaurantRepository
import org.jetbrains.compose.resources.getString

class RestaurantsListViewModel(
    private val restaurantRepository: RestaurantRepository,
    private val accountHolder: CurrentAccountHolder,
) : ViewModel() {
    private val _formState = MutableStateFlow(RestaurantsListUiState())

    val uiState: StateFlow<RestaurantsListUiState> = combine(
        restaurantRepository.getAllRestaurants(),
        _formState,
        accountHolder.session,
    ) { restaurants, formState, session ->
        formState.copy(
            isLoading = false,
            isAccountAdmin = session?.isAccountAdmin ?: false,
            restaurants = restaurants,
        )
    }
        .catch { throwable ->
            emit(
                _formState.value.copy(
                    isLoading = false,
                    error = throwable.message ?: getString(Res.string.restaurants_error_loading),
                ),
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = RestaurantsListUiState(),
        )

    fun onNewRestaurant() {
        _formState.update {
            it.copy(
                isFormVisible = true,
                editingRestaurant = null,
                formName = "",
                formSlug = "",
                formDescription = "",
                formAddress = "",
                formPhone = "",
            )
        }
    }

    fun onEditRestaurant(restaurant: Restaurant) {
        _formState.update {
            it.copy(
                isFormVisible = true,
                editingRestaurant = restaurant,
                formName = restaurant.name,
                formSlug = restaurant.slug,
                formDescription = restaurant.description,
                formAddress = restaurant.address,
                formPhone = restaurant.phone,
            )
        }
    }

    fun onFormNameChange(name: String) {
        _formState.update { state ->
            val slug = if (state.editingRestaurant == null) {
                name.lowercase()
                    .replace(" ", "-")
                    .replace(Regex("[^a-z0-9-]"), "")
            } else {
                state.formSlug
            }
            state.copy(formName = name, formSlug = slug)
        }
    }

    fun onFormSlugChange(slug: String) {
        _formState.update { it.copy(formSlug = slug) }
    }

    fun onFormDescriptionChange(desc: String) {
        _formState.update { it.copy(formDescription = desc) }
    }

    fun onFormAddressChange(addr: String) {
        _formState.update { it.copy(formAddress = addr) }
    }

    fun onFormPhoneChange(phone: String) {
        _formState.update { it.copy(formPhone = phone) }
    }

    fun onSaveRestaurant() {
        val state = _formState.value
        if (state.formName.isBlank()) return

        viewModelScope.launch {
            _formState.update { it.copy(isSaving = true, error = null) }
            try {
                val existing = state.editingRestaurant
                if (existing != null) {
                    restaurantRepository.updateRestaurant(
                        existing.copy(
                            name = state.formName,
                            slug = state.formSlug,
                            description = state.formDescription,
                            address = state.formAddress,
                            phone = state.formPhone,
                        ),
                    )
                } else {
                    restaurantRepository.createRestaurant(
                        Restaurant(
                            name = state.formName,
                            slug = state.formSlug,
                            description = state.formDescription,
                            address = state.formAddress,
                            phone = state.formPhone,
                        ),
                    )
                }
                val successMessage = if (existing != null) {
                    getString(Res.string.restaurants_updated)
                } else {
                    getString(Res.string.restaurants_created)
                }
                _formState.update {
                    it.copy(
                        isSaving = false,
                        isFormVisible = false,
                        editingRestaurant = null,
                        successMessage = successMessage,
                    )
                }
            } catch (e: Exception) {
                val errorMessage = getString(
                    Res.string.restaurants_error_saving,
                    e.message ?: getString(Res.string.error_unknown),
                )
                _formState.update {
                    it.copy(
                        isSaving = false,
                        error = errorMessage,
                    )
                }
            }
        }
    }

    fun onDeleteRestaurant(id: String) {
        viewModelScope.launch {
            _formState.update { it.copy(error = null) }
            try {
                restaurantRepository.deleteRestaurant(id)
                val message = getString(Res.string.restaurants_deleted)
                _formState.update { it.copy(successMessage = message) }
            } catch (e: Exception) {
                val errorMessage = getString(
                    Res.string.restaurants_error_deleting,
                    e.message ?: getString(Res.string.error_unknown),
                )
                _formState.update {
                    it.copy(error = errorMessage)
                }
            }
        }
    }

    fun onDismissForm() {
        _formState.update {
            it.copy(
                isFormVisible = false,
                editingRestaurant = null,
                formName = "",
                formSlug = "",
                formDescription = "",
                formAddress = "",
                formPhone = "",
            )
        }
    }

    fun dismissMessage() {
        _formState.update { it.copy(error = null, successMessage = null) }
    }
}
