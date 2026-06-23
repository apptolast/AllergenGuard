package org.apptolast.menuadmin.presentation.screens.restaurants.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Fastfood
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Publish
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import menuadmin.adminapp.generated.resources.Res
import menuadmin.adminapp.generated.resources.action_cancel
import menuadmin.adminapp.generated.resources.action_save
import menuadmin.adminapp.generated.resources.field_description
import menuadmin.adminapp.generated.resources.field_name
import menuadmin.adminapp.generated.resources.nav_menus
import menuadmin.adminapp.generated.resources.nav_recipes
import menuadmin.adminapp.generated.resources.restaurants_edit
import menuadmin.adminapp.generated.resources.restaurants_field_address
import menuadmin.adminapp.generated.resources.restaurants_field_phone
import menuadmin.adminapp.generated.resources.restaurants_info_title
import menuadmin.adminapp.generated.resources.restaurants_not_found
import menuadmin.adminapp.generated.resources.restaurants_published
import org.apptolast.menuadmin.domain.model.Restaurant
import org.apptolast.menuadmin.presentation.components.ErrorSnackbarEffect
import org.apptolast.menuadmin.presentation.components.StatCard
import org.apptolast.menuadmin.presentation.theme.Amber500
import org.apptolast.menuadmin.presentation.theme.Blue500
import org.apptolast.menuadmin.presentation.theme.Green500
import org.apptolast.menuadmin.presentation.theme.MenuAdminTheme
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RestaurantOverviewContent(
    uiState: RestaurantDetailUiState,
    onStartEditing: () -> Unit,
    onCancelEditing: () -> Unit,
    onSave: () -> Unit,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onDismissMessage: () -> Unit,
    onOpenRecipes: () -> Unit = {},
    onOpenMenus: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val restaurant = uiState.restaurant

    if (restaurant == null && !uiState.isLoading) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = uiState.error ?: stringResource(Res.string.restaurants_not_found),
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        // Errors surface through the app-wide snackbar instead of inline red text.
        ErrorSnackbarEffect(uiState.error)

        // Messages
        uiState.successMessage?.let { msg ->
            Text(text = msg, color = MenuAdminTheme.colors.success, fontSize = 13.sp)
        }

        // Stats row
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            StatCard(
                label = stringResource(Res.string.nav_recipes),
                value = uiState.recipesCount.toString(),
                icon = Icons.Outlined.Fastfood,
                iconTint = Green500,
                onClick = onOpenRecipes,
                modifier = Modifier.weight(1f),
            )
            StatCard(
                label = stringResource(Res.string.nav_menus),
                value = uiState.menusCount.toString(),
                icon = Icons.AutoMirrored.Outlined.MenuBook,
                iconTint = Amber500,
                onClick = onOpenMenus,
                modifier = Modifier.weight(1f),
            )
            StatCard(
                label = stringResource(Res.string.restaurants_published),
                value = uiState.publishedMenusCount.toString(),
                icon = Icons.Outlined.Publish,
                iconTint = Blue500,
                modifier = Modifier.weight(1f),
            )
        }

        // Restaurant info / edit card
        if (uiState.isEditing) {
            RestaurantEditCard(
                uiState = uiState,
                onNameChange = onNameChange,
                onDescriptionChange = onDescriptionChange,
                onAddressChange = onAddressChange,
                onPhoneChange = onPhoneChange,
                onSave = onSave,
                onCancel = onCancelEditing,
            )
        } else if (restaurant != null) {
            RestaurantInfoCard(restaurant = restaurant)
        }
    }
}

@Composable
private fun RestaurantInfoCard(
    restaurant: Restaurant,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(Res.string.restaurants_info_title),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(4.dp))
            if (restaurant.description.isNotEmpty()) {
                Text(
                    text = restaurant.description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            if (restaurant.address.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = restaurant.address,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (restaurant.phone.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Phone,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = restaurant.phone,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun RestaurantEditCard(
    uiState: RestaurantDetailUiState,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(Res.string.restaurants_edit),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            OutlinedTextField(
                value = uiState.editName,
                onValueChange = onNameChange,
                label = { Text(stringResource(Res.string.field_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
            )
            OutlinedTextField(
                value = uiState.editDescription,
                onValueChange = onDescriptionChange,
                label = { Text(stringResource(Res.string.field_description)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                minLines = 2,
            )
            OutlinedTextField(
                value = uiState.editAddress,
                onValueChange = onAddressChange,
                label = { Text(stringResource(Res.string.restaurants_field_address)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
            )
            OutlinedTextField(
                value = uiState.editPhone,
                onValueChange = onPhoneChange,
                label = { Text(stringResource(Res.string.restaurants_field_phone)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                        Icon(
                            imageVector = Icons.Outlined.Save,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(Res.string.action_save), color = Color.White)
                    }
                }
                OutlinedButton(
                    onClick = onCancel,
                    enabled = !uiState.isSaving,
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(stringResource(Res.string.action_cancel))
                }
            }
        }
    }
}

@Preview
@Composable
private fun PreviewRestaurantOverviewContent() {
    MenuAdminTheme {
        RestaurantOverviewContent(
            uiState = RestaurantDetailUiState(
                isLoading = false,
                restaurant = Restaurant(
                    id = "1",
                    name = "Hotel Palace Barcelona",
                    slug = "hotel-palace-barcelona",
                    description = "Restaurante de cocina mediterranea con vistas al mar",
                    address = "Paseo de Gracia 123, Barcelona",
                    phone = "+34 934 567 890",
                    active = true,
                ),
                recipesCount = 12,
                menusCount = 3,
                publishedMenusCount = 2,
            ),
            onStartEditing = {},
            onCancelEditing = {},
            onSave = {},
            onNameChange = {},
            onDescriptionChange = {},
            onAddressChange = {},
            onPhoneChange = {},
            onDismissMessage = {},
        )
    }
}

@Preview
@Composable
private fun PreviewRestaurantOverviewContentEditing() {
    MenuAdminTheme {
        RestaurantOverviewContent(
            uiState = RestaurantDetailUiState(
                isLoading = false,
                isEditing = true,
                restaurant = Restaurant(
                    id = "1",
                    name = "Hotel Palace Barcelona",
                    slug = "hotel-palace-barcelona",
                    description = "Restaurante de cocina mediterranea",
                    address = "Paseo de Gracia 123, Barcelona",
                    phone = "+34 934 567 890",
                    active = true,
                ),
                editName = "Hotel Palace Barcelona",
                editDescription = "Restaurante de cocina mediterranea",
                editAddress = "Paseo de Gracia 123, Barcelona",
                editPhone = "+34 934 567 890",
                recipesCount = 12,
                menusCount = 3,
                publishedMenusCount = 2,
            ),
            onStartEditing = {},
            onCancelEditing = {},
            onSave = {},
            onNameChange = {},
            onDescriptionChange = {},
            onAddressChange = {},
            onPhoneChange = {},
            onDismissMessage = {},
        )
    }
}
