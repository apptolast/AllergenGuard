package org.apptolast.menuadmin.domain.repository

import kotlinx.coroutines.flow.Flow
import org.apptolast.menuadmin.domain.model.DashboardStats

interface DashboardRepository {
    fun getDashboardStats(): Flow<DashboardStats>
}
