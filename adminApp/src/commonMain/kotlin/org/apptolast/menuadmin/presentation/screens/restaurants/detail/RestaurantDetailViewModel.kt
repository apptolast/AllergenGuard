package org.apptolast.menuadmin.presentation.screens.restaurants.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.apptolast.menuadmin.data.SelectedRestaurantHolder
import org.apptolast.menuadmin.domain.model.Restaurant
import org.apptolast.menuadmin.domain.repository.MenuRepository
import org.apptolast.menuadmin.domain.repository.RecipeRepository
import org.apptolast.menuadmin.domain.repository.RestaurantRepository

class RestaurantDetailViewModel(
    private val restaurantRepository: RestaurantRepository,
    private val menuRepository: MenuRepository,
    private val recipeRepository: RecipeRepository,
    private val selectedRestaurantHolder: SelectedRestaurantHolder,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val restaurantId: String = savedStateHandle["restaurantId"] ?: ""

    private val _uiState = MutableStateFlow(RestaurantDetailUiState())
    val uiState: StateFlow<RestaurantDetailUiState> = _uiState.asStateFlow()

    init {
        loadRestaurant()
    }

    private fun loadRestaurant() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val restaurant = restaurantRepository.getRestaurantById(restaurantId)
                if (restaurant != null) {
                    selectedRestaurantHolder.select(restaurant)

                    val recipes = recipeRepository.getRecipesByRestaurant(restaurantId).first()
                    val menus = menuRepository.getMenusByRestaurant(restaurantId).first()
                    val publishedMenus = menus.filter { it.isActive }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            restaurant = restaurant,
                            recipesCount = recipes.size,
                            menusCount = menus.size,
                            publishedMenusCount = publishedMenus.size,
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Restaurante no encontrado",
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Error al cargar el restaurante: ${e.message ?: "Error desconocido"}",
                    )
                }
            }
        }
    }

    fun startEditing() {
        val restaurant = _uiState.value.restaurant ?: return
        _uiState.update {
            it.copy(
                isEditing = true,
                editName = restaurant.name,
                editDescription = restaurant.description,
                editAddress = restaurant.address,
                editPhone = restaurant.phone,
            )
        }
    }

    fun cancelEditing() {
        _uiState.update { it.copy(isEditing = false) }
    }

    fun onNameChange(name: String) {
        _uiState.update { it.copy(editName = name) }
    }

    fun onDescriptionChange(description: String) {
        _uiState.update { it.copy(editDescription = description) }
    }

    fun onAddressChange(address: String) {
        _uiState.update { it.copy(editAddress = address) }
    }

    fun onPhoneChange(phone: String) {
        _uiState.update { it.copy(editPhone = phone) }
    }

    fun saveRestaurant() {
        val state = _uiState.value
        val current = state.restaurant ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                val updated = restaurantRepository.updateRestaurant(
                    Restaurant(
                        id = current.id,
                        name = state.editName,
                        slug = current.slug,
                        description = state.editDescription,
                        address = state.editAddress,
                        phone = state.editPhone,
                        logoUrl = current.logoUrl,
                        active = current.active,
                    ),
                )
                selectedRestaurantHolder.select(updated)
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        isEditing = false,
                        restaurant = updated,
                        successMessage = "Restaurante actualizado",
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = "Error al guardar: ${e.message ?: "Error desconocido"}",
                    )
                }
            }
        }
    }

    fun dismissMessage() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}
