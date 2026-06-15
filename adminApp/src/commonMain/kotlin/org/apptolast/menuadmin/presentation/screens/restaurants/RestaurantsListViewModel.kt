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
import org.apptolast.menuadmin.domain.model.Restaurant
import org.apptolast.menuadmin.domain.repository.RestaurantRepository

class RestaurantsListViewModel(
    private val restaurantRepository: RestaurantRepository,
) : ViewModel() {
    private val _formState = MutableStateFlow(RestaurantsListUiState())

    val uiState: StateFlow<RestaurantsListUiState> = combine(
        restaurantRepository.getAllRestaurants(),
        _formState,
    ) { restaurants, formState ->
        formState.copy(
            isLoading = false,
            restaurants = restaurants,
        )
    }
        .catch { throwable ->
            emit(
                _formState.value.copy(
                    isLoading = false,
                    error = throwable.message ?: "Error al cargar restaurantes",
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
                _formState.update {
                    it.copy(
                        isSaving = false,
                        isFormVisible = false,
                        editingRestaurant = null,
                        successMessage = if (existing != null) {
                            "Restaurante actualizado"
                        } else {
                            "Restaurante creado"
                        },
                    )
                }
            } catch (e: Exception) {
                _formState.update {
                    it.copy(
                        isSaving = false,
                        error = "Error al guardar: ${e.message ?: "Error desconocido"}",
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
                _formState.update { it.copy(successMessage = "Restaurante eliminado") }
            } catch (e: Exception) {
                _formState.update {
                    it.copy(error = "Error al eliminar: ${e.message ?: "Error desconocido"}")
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
