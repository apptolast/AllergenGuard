package org.apptolast.menuadmin.presentation.screens.dashboard

import org.apptolast.menuadmin.domain.model.DashboardStats

data class DashboardUiState(
    val isLoading: Boolean = true,
    val stats: DashboardStats? = null,
    val error: String? = null,
)
