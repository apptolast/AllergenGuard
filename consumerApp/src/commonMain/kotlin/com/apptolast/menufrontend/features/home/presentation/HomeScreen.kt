package com.apptolast.menufrontend.features.home.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apptolast.menufrontend.features.components.BottomNavTab
import com.apptolast.menufrontend.features.components.BottomNavigationBar
import com.apptolast.menufrontend.features.home.components.RestaurantCard
import com.apptolast.menufrontend.features.home.components.SearchBar
import com.apptolast.menufrontend.features.home.data.HomeAction
import com.apptolast.menufrontend.features.home.data.HomeState
import com.apptolast.menufrontend.resources.Res
import com.apptolast.menufrontend.resources.app_name
import com.apptolast.menufrontend.resources.home_nearby_title
import com.apptolast.menufrontend.resources.home_see_all
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreenRoot(
    onRestaurantClick: (String) -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToProfile: () -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is HomeEffect.NavigateToMenu -> onRestaurantClick(effect.restaurantId)
            }
        }
    }

    HomeScreen(
        state = state,
        onAction = viewModel::onAction,
        onTabSelected = { tab ->
            when (tab) {
                BottomNavTab.EXPLORE -> { /* Already here */ }
                BottomNavTab.FAVORITES -> onNavigateToFavorites()
                BottomNavTab.PROFILE -> onNavigateToProfile()
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: HomeState,
    onAction: (HomeAction) -> Unit,
    onTabSelected: (BottomNavTab) -> Unit,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.app_name),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        bottomBar = {
            BottomNavigationBar(
                selectedTab = BottomNavTab.EXPLORE,
                onTabSelected = onTabSelected,
            )
        },
    ) { padding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Search bar
                item {
                    SearchBar(
                        query = state.searchQuery,
                        onQueryChange = { onAction(HomeAction.SearchQueryChanged(it)) },
                    )
                }

                // Section header
                item {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(Res.string.home_nearby_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Text(
                            text = stringResource(Res.string.home_see_all),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }

                // Restaurant list
                items(
                    items = state.restaurants,
                    key = { it.id },
                ) { restaurant ->
                    RestaurantCard(
                        restaurant = restaurant,
                        onClick = { onAction(HomeAction.RestaurantClicked(restaurant.id)) },
                    )
                }
            }
        }
    }
}
