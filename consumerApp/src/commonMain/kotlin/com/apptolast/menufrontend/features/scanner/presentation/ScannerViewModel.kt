package com.apptolast.menufrontend.features.scanner.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apptolast.menufrontend.features.scanner.data.ScannerAction
import com.apptolast.menufrontend.features.scanner.data.ScannerState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface ScannerEffect {
    data class NavigateToMenu(val restaurantId: String) : ScannerEffect
    data object NavigateBack : ScannerEffect
}

class ScannerViewModel : ViewModel() {

    private val _state = MutableStateFlow(ScannerState())
    val state: StateFlow<ScannerState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<ScannerEffect>()
    val effect: SharedFlow<ScannerEffect> = _effect.asSharedFlow()

    fun onAction(action: ScannerAction) {
        when (action) {
            is ScannerAction.CodeScanned -> handleScannedCode(action.code)
            ScannerAction.NavigateBack -> {
                viewModelScope.launch { _effect.emit(ScannerEffect.NavigateBack) }
            }
            ScannerAction.RetryScanning -> {
                _state.update { it.copy(isScanning = true, scannedCode = null, error = null) }
            }
        }
    }

    private fun handleScannedCode(code: String) {
        _state.update { it.copy(isScanning = false, scannedCode = code) }
        // QR codes contain a URL like: allergenguard://restaurant/{restaurantId}
        // or a simple restaurant ID
        val restaurantId = extractRestaurantId(code)
        if (restaurantId != null) {
            viewModelScope.launch {
                _effect.emit(ScannerEffect.NavigateToMenu(restaurantId))
            }
        } else {
            _state.update { it.copy(error = "QR no válido") }
        }
    }

    private fun extractRestaurantId(code: String): String? {
        // Support both URL format and plain ID
        return when {
            code.contains("restaurant/") -> code.substringAfterLast("restaurant/").takeIf { it.isNotBlank() }
            code.startsWith("rest-") -> code
            else -> null
        }
    }
}
