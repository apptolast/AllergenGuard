package org.apptolast.menuadmin.presentation.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.apptolast.menuadmin.domain.repository.DashboardRepository

class DashboardViewModel(
    dashboardRepository: DashboardRepository,
) : ViewModel() {
    val uiState: StateFlow<DashboardUiState> = dashboardRepository
        .getDashboardStats()
        .map { stats ->
            DashboardUiState(
                isLoading = false,
                stats = stats,
                error = null,
            )
        }
        .catch { throwable ->
            emit(
                DashboardUiState(
                    isLoading = false,
                    stats = null,
                    error = throwable.message ?: "Error al cargar el dashboard",
                ),
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DashboardUiState(),
        )
}
