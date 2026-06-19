package org.apptolast.menuadmin.presentation.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Fastfood
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.apptolast.menuadmin.domain.model.AllergenType
import org.apptolast.menuadmin.domain.model.DashboardStats
import org.apptolast.menuadmin.presentation.components.ErrorSnackbarEffect
import org.apptolast.menuadmin.presentation.components.StatCard
import org.apptolast.menuadmin.presentation.screens.dashboard.components.AllergenFrequencyChart
import org.apptolast.menuadmin.presentation.screens.dashboard.components.RecentActivityList
import org.apptolast.menuadmin.presentation.theme.Amber500
import org.apptolast.menuadmin.presentation.theme.Blue500
import org.apptolast.menuadmin.presentation.theme.Green500
import org.apptolast.menuadmin.presentation.theme.MenuAdminTheme
import org.apptolast.menuadmin.presentation.theme.Red500
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DashboardScreen(viewModel: DashboardViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    DashboardContent(uiState = uiState)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardContent(
    uiState: DashboardUiState,
    modifier: Modifier = Modifier,
) {
    if (uiState.isLoading) {
        CircularProgressIndicator()
        return
    }

    val stats = uiState.stats

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        // Header
        Column {
            Text(
                text = "Dashboard",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Bienvenido de nuevo, aqui tienes un resumen de tu actividad",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Stat Cards
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            StatCard(
                label = "Ingredientes",
                value = (stats?.totalIngredients ?: 0).toString(),
                icon = Icons.Outlined.Restaurant,
                iconTint = Blue500,
                modifier = Modifier.weight(1f),
            )
            StatCard(
                label = "Recetas activas",
                value = (stats?.activeRecipes ?: 0).toString(),
                icon = Icons.Outlined.Fastfood,
                iconTint = Green500,
                modifier = Modifier.weight(1f),
            )
            StatCard(
                label = "Menus",
                value = (stats?.totalMenus ?: 0).toString(),
                icon = Icons.AutoMirrored.Outlined.MenuBook,
                iconTint = Amber500,
                modifier = Modifier.weight(1f),
            )
            StatCard(
                label = "Restaurantes",
                value = (stats?.totalRestaurants ?: 0).toString(),
                icon = Icons.Outlined.Business,
                iconTint = Red500,
                modifier = Modifier.weight(1f),
            )
        }

        // Recent activity + common allergens, side by side
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            // Recent activity (wider)
            DashboardSectionCard(modifier = Modifier.weight(2f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "Actividad Reciente",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "Ver Toda",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Blue500,
                    )
                }
                if (stats != null) {
                    RecentActivityList(activities = stats.recentActivity)
                }
            }

            // Most common allergens
            DashboardSectionCard(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Alergenos mas comunes",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (stats != null && stats.allergenFrequency.isNotEmpty()) {
                    val maxValue = stats.allergenFrequency.values.maxOrNull() ?: 1
                    AllergenFrequencyChart(
                        allergenFrequency = stats.allergenFrequency,
                        maxValue = maxValue,
                    )
                }
            }
        }

        // Errors surface through the app-wide snackbar instead of inline red text.
        ErrorSnackbarEffect(uiState.error)
    }
}

/** White rounded card used to group a dashboard section (matches the panels in the mockup). */
@Composable
private fun DashboardSectionCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        content = content,
    )
}

@Preview
@Composable
private fun DashboardContentPreview() {
    MenuAdminTheme {
        DashboardContent(
            uiState = DashboardUiState(
                isLoading = false,
                stats = DashboardStats(
                    totalIngredients = 127,
                    activeRecipes = 45,
                    totalMenus = 8,
                    totalRestaurants = 3,
                    recentActivity = emptyList(),
                    allergenFrequency = mapOf(
                        AllergenType.GLUTEN to 42,
                        AllergenType.DAIRY to 35,
                        AllergenType.EGGS to 28,
                    ),
                ),
            ),
        )
    }
}
