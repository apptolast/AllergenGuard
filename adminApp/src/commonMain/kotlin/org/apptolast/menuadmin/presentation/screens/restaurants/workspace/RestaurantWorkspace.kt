package org.apptolast.menuadmin.presentation.screens.restaurants.workspace

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.apptolast.menuadmin.presentation.screens.cartadigital.CartaDigitalScreen
import org.apptolast.menuadmin.presentation.screens.cartadigital.CartaDigitalViewModel
import org.apptolast.menuadmin.presentation.screens.menus.MenusScreen
import org.apptolast.menuadmin.presentation.screens.menus.MenusViewModel
import org.apptolast.menuadmin.presentation.screens.recipes.RecipesScreen
import org.apptolast.menuadmin.presentation.screens.recipes.RecipesViewModel
import org.apptolast.menuadmin.presentation.screens.restaurants.detail.RestaurantDetailViewModel
import org.apptolast.menuadmin.presentation.screens.restaurants.detail.RestaurantOverviewContent
import org.apptolast.menuadmin.presentation.theme.Blue500
import org.apptolast.menuadmin.presentation.theme.MenuAdminTheme
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

private enum class WorkspaceTab(
    val title: String,
) {
    OVERVIEW("Resumen"),
    RECIPES("Recetas"),
    MENUS("Menus"),
    CARTA("Carta Digital"),
}

@Composable
fun RestaurantWorkspace(
    restaurantId: String,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
) {
    val detailViewModel: RestaurantDetailViewModel = koinViewModel()
    val detailState by detailViewModel.uiState.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = WorkspaceTab.entries

    Column(modifier = modifier.fillMaxSize()) {
        // Header
        WorkspaceHeader(
            restaurantName = detailState.restaurant?.name ?: "",
            restaurantSlug = detailState.restaurant?.slug ?: "",
            isLoading = detailState.isLoading,
            isEditing = detailState.isEditing,
            onBack = onBack,
            onEdit = {
                selectedTab = 0
                detailViewModel.startEditing()
            },
        )

        // Tab bar
        @OptIn(ExperimentalMaterial3Api::class)
        PrimaryScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = Blue500,
            edgePadding = 16.dp,
            divider = { HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant) },
        ) {
            tabs.forEachIndexed { index, tab ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = tab.title,
                            fontWeight = if (selectedTab == index) {
                                FontWeight.SemiBold
                            } else {
                                FontWeight.Normal
                            },
                            color = if (selectedTab == index) Blue500 else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                )
            }
        }

        // Tab content
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (tabs[selectedTab]) {
                WorkspaceTab.OVERVIEW -> {
                    RestaurantOverviewContent(
                        uiState = detailState,
                        onStartEditing = detailViewModel::startEditing,
                        onCancelEditing = detailViewModel::cancelEditing,
                        onSave = detailViewModel::saveRestaurant,
                        onNameChange = detailViewModel::onNameChange,
                        onDescriptionChange = detailViewModel::onDescriptionChange,
                        onAddressChange = detailViewModel::onAddressChange,
                        onPhoneChange = detailViewModel::onPhoneChange,
                        onDismissMessage = detailViewModel::dismissMessage,
                    )
                }

                WorkspaceTab.RECIPES -> {
                    val vm: RecipesViewModel = koinViewModel(
                        key = "recipes-$restaurantId",
                        parameters = { parametersOf(restaurantId) },
                    )
                    RecipesScreen(viewModel = vm)
                }

                WorkspaceTab.MENUS -> {
                    val vm: MenusViewModel = koinViewModel(
                        key = "menus-$restaurantId",
                        parameters = { parametersOf(restaurantId) },
                    )
                    MenusScreen(viewModel = vm)
                }

                WorkspaceTab.CARTA -> {
                    val vm: CartaDigitalViewModel = koinViewModel(
                        key = "carta-$restaurantId",
                        parameters = { parametersOf(restaurantId) },
                    )
                    CartaDigitalScreen(viewModel = vm)
                }
            }
        }
    }
}

@Composable
private fun WorkspaceHeader(
    restaurantName: String,
    restaurantSlug: String,
    isLoading: Boolean,
    isEditing: Boolean,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onEdit: () -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Volver a restaurantes",
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Blue500,
                    strokeWidth = 2.dp,
                )
            } else {
                Text(
                    text = restaurantName,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = restaurantSlug,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (!isLoading && !isEditing) {
            OutlinedButton(
                onClick = onEdit,
                shape = RoundedCornerShape(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = "Editar",
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Editar")
            }
        }
    }
}

@Preview
@Composable
private fun WorkspaceHeaderPreview() {
    MenuAdminTheme {
        WorkspaceHeader(
            restaurantName = "restaurant",
            restaurantSlug = "res",
            isLoading = false,
            isEditing = false,
        )
    }
}
