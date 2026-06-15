package org.apptolast.menuadmin.presentation.screens.restaurants

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.apptolast.menuadmin.domain.model.Restaurant
import org.apptolast.menuadmin.presentation.theme.Blue500
import org.apptolast.menuadmin.presentation.theme.Green500
import org.apptolast.menuadmin.presentation.theme.MenuAdminTheme
import org.apptolast.menuadmin.presentation.theme.Red500
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RestaurantsListScreen(
    onNavigateToRestaurant: (String) -> Unit,
    viewModel: RestaurantsListViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    RestaurantsListContent(
        uiState = uiState,
        onNavigateToRestaurant = onNavigateToRestaurant,
        onNewRestaurant = viewModel::onNewRestaurant,
        onEditRestaurant = viewModel::onEditRestaurant,
        onFormNameChange = viewModel::onFormNameChange,
        onFormSlugChange = viewModel::onFormSlugChange,
        onFormDescriptionChange = viewModel::onFormDescriptionChange,
        onFormAddressChange = viewModel::onFormAddressChange,
        onFormPhoneChange = viewModel::onFormPhoneChange,
        onSaveRestaurant = viewModel::onSaveRestaurant,
        onDismissForm = viewModel::onDismissForm,
        onDismissMessage = viewModel::dismissMessage,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RestaurantsListContent(
    uiState: RestaurantsListUiState,
    onNavigateToRestaurant: (String) -> Unit,
    onNewRestaurant: () -> Unit,
    onEditRestaurant: (Restaurant) -> Unit,
    onFormNameChange: (String) -> Unit,
    onFormSlugChange: (String) -> Unit,
    onFormDescriptionChange: (String) -> Unit,
    onFormAddressChange: (String) -> Unit,
    onFormPhoneChange: (String) -> Unit,
    onSaveRestaurant: () -> Unit,
    onDismissForm: () -> Unit,
    onDismissMessage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "Restaurantes",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Gestiona tus restaurantes",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Button(
                onClick = onNewRestaurant,
                colors = ButtonDefaults.buttonColors(containerColor = Blue500),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text("Nuevo Restaurante", color = Color.White)
            }
        }

        // Error message
        uiState.error?.let { error ->
            Text(text = error, color = Red500, fontSize = 13.sp)
        }

        // Success message
        uiState.successMessage?.let { msg ->
            Text(text = msg, color = MenuAdminTheme.colors.success, fontSize = 13.sp)
        }

        // Loading
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = Blue500,
                    strokeWidth = 3.dp,
                )
            }
        } else if (uiState.restaurants.isEmpty()) {
            Text(
                text = "No tienes restaurantes. Crea uno con el boton superior.",
                fontSize = 14.sp,
                color = MenuAdminTheme.colors.textMuted,
                modifier = Modifier.padding(vertical = 24.dp),
            )
        } else {
            // Restaurant cards grid
            FlowRow(
                maxItemsInEachRow = 3,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                uiState.restaurants.forEach { restaurant ->
                    RestaurantCard(
                        restaurant = restaurant,
                        onClick = { onNavigateToRestaurant(restaurant.id) },
                        onEdit = { onEditRestaurant(restaurant) },
                        modifier = Modifier.weight(1f),
                    )
                }
                // Fill remaining slots so cards don't stretch to fill the row
                val remainder = (3 - uiState.restaurants.size % 3) % 3
                repeat(remainder) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }

    // Restaurant edit form dialog
    if (uiState.isFormVisible) {
        RestaurantFormDialog(
            uiState = uiState,
            onNameChange = onFormNameChange,
            onSlugChange = onFormSlugChange,
            onDescriptionChange = onFormDescriptionChange,
            onAddressChange = onFormAddressChange,
            onPhoneChange = onFormPhoneChange,
            onSave = onSaveRestaurant,
            onDismiss = onDismissForm,
        )
    }
}

@Composable
private fun RestaurantCard(
    restaurant: Restaurant,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Name + Edit button row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = restaurant.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Editar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            // Slug
            Text(
                text = restaurant.slug,
                fontSize = 13.sp,
                color = MenuAdminTheme.colors.textMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            // Description
            if (restaurant.description.isNotEmpty()) {
                Text(
                    text = restaurant.description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // Address
            if (restaurant.address.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = restaurant.address,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            // Phone
            if (restaurant.phone.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Phone,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = restaurant.phone,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            // Active/Inactive badge
            Text(
                text = if (restaurant.active) "Activo" else "Inactivo",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (restaurant.active) Green500 else Red500,
                modifier = Modifier
                    .padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun RestaurantFormDialog(
    uiState: RestaurantsListUiState,
    onNameChange: (String) -> Unit,
    onSlugChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (uiState.editingRestaurant != null) "Editar Restaurante" else "Nuevo Restaurante",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = uiState.formName,
                    onValueChange = onNameChange,
                    label = { Text("Nombre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                )
                OutlinedTextField(
                    value = uiState.formSlug,
                    onValueChange = onSlugChange,
                    label = { Text("Slug") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                )
                OutlinedTextField(
                    value = uiState.formDescription,
                    onValueChange = onDescriptionChange,
                    label = { Text("Descripcion") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    minLines = 2,
                )
                OutlinedTextField(
                    value = uiState.formAddress,
                    onValueChange = onAddressChange,
                    label = { Text("Direccion") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                )
                OutlinedTextField(
                    value = uiState.formPhone,
                    onValueChange = onPhoneChange,
                    label = { Text("Telefono") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                enabled = !uiState.isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = Blue500),
                shape = RoundedCornerShape(8.dp),
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("Guardar", color = Color.White)
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !uiState.isSaving,
            ) {
                Text("Cancelar")
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.surface,
    )
}

@Preview
@Composable
private fun PreviewRestaurantsListContentEmpty() {
    MenuAdminTheme {
        RestaurantsListContent(
            uiState = RestaurantsListUiState(
                isLoading = false,
                restaurants = emptyList(),
            ),
            onNavigateToRestaurant = {},
            onNewRestaurant = {},
            onEditRestaurant = {},
            onFormNameChange = {},
            onFormSlugChange = {},
            onFormDescriptionChange = {},
            onFormAddressChange = {},
            onFormPhoneChange = {},
            onSaveRestaurant = {},
            onDismissForm = {},
            onDismissMessage = {},
        )
    }
}

@Preview
@Composable
private fun PreviewRestaurantsListContentWithData() {
    MenuAdminTheme {
        RestaurantsListContent(
            uiState = RestaurantsListUiState(
                isLoading = false,
                restaurants = listOf(
                    Restaurant(
                        id = "1",
                        name = "Hotel Palace Barcelona",
                        slug = "hotel-palace-barcelona",
                        description = "Restaurante de cocina mediterranea con vistas al mar",
                        address = "Paseo de Gracia 123, Barcelona",
                        phone = "+34 934 567 890",
                        active = true,
                    ),
                ),
            ),
            onNavigateToRestaurant = {},
            onNewRestaurant = {},
            onEditRestaurant = {},
            onFormNameChange = {},
            onFormSlugChange = {},
            onFormDescriptionChange = {},
            onFormAddressChange = {},
            onFormPhoneChange = {},
            onSaveRestaurant = {},
            onDismissForm = {},
            onDismissMessage = {},
        )
    }
}
