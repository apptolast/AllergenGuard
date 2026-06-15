package com.apptolast.menufrontend.features.dishdetail.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apptolast.menufrontend.data.repository.RestaurantRepository
import com.apptolast.menufrontend.data.repository.UserRepository
import com.apptolast.menufrontend.features.dishdetail.data.DishDetailState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DishDetailViewModel(
    private val restaurantRepository: RestaurantRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(DishDetailState())
    val state: StateFlow<DishDetailState> = _state.asStateFlow()

    fun loadDish(dishId: String, restaurantId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            restaurantRepository.getRestaurantName(restaurantId)
                .onSuccess { name -> _state.update { it.copy(restaurantName = name) } }

            userRepository.getUserAllergens()
                .onSuccess { allergens -> _state.update { it.copy(userAllergens = allergens) } }

            restaurantRepository.getDishDetail(dishId)
                .onSuccess { dish ->
                    _state.update { it.copy(dish = dish, isLoading = false) }
                }
                .onFailure { e ->
                    _state.update { it.copy(error = e.message, isLoading = false) }
                }
        }
    }
}
