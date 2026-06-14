package org.apptolast.menuadmin.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.apptolast.menuadmin.domain.model.Restaurant

class SelectedRestaurantHolder {
    private val _selected = MutableStateFlow<Restaurant?>(null)
    val selected: StateFlow<Restaurant?> = _selected.asStateFlow()

    fun select(restaurant: Restaurant) {
        _selected.value = restaurant
    }

    fun clear() {
        _selected.value = null
    }
}
