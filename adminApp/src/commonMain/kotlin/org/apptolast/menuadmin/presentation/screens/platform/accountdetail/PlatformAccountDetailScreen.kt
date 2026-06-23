package org.apptolast.menuadmin.presentation.screens.platform.accountdetail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import org.apptolast.menuadmin.domain.model.AccountRole
import org.apptolast.menuadmin.domain.model.AccountUser
import org.apptolast.menuadmin.domain.model.AccountUserStatus
import org.apptolast.menuadmin.domain.model.Restaurant
import org.apptolast.menuadmin.presentation.components.ErrorSnackbarEffect
import org.apptolast.menuadmin.presentation.theme.Blue500
import org.apptolast.menuadmin.presentation.theme.Green500
import org.apptolast.menuadmin.presentation.theme.MenuAdminTheme
import org.apptolast.menuadmin.presentation.theme.Red500
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun PlatformAccountDetailScreen(
    accountId: String,
    onBack: () -> Unit,
    viewModel: PlatformAccountDetailViewModel = koinViewModel { parametersOf(accountId) },
) {
    val uiState by viewModel.uiState.collectAsState()
    PlatformAccountDetailContent(
        uiState = uiState,
        onBack = onBack,
        onInviteUser = viewModel::onInviteUser,
        onEditUser = viewModel::onEditUser,
        onRequestRemove = viewModel::onRequestRemove,
        onFormEmailChange = viewModel::onFormEmailChange,
        onFormRoleChange = viewModel::onFormRoleChange,
        onToggleRestaurant = viewModel::onToggleRestaurant,
        onSaveUser = viewModel::onSaveUser,
        onDismissForm = viewModel::onDismissForm,
        onConfirmRemove = viewModel::onConfirmRemove,
        onDismissRemove = viewModel::onDismissRemove,
    )
}

@Composable
fun PlatformAccountDetailContent(
    uiState: PlatformAccountDetailUiState,
    onBack: () -> Unit,
    onInviteUser: () -> Unit,
    onEditUser: (AccountUser) -> Unit,
    onRequestRemove: (AccountUser) -> Unit,
    onFormEmailChange: (String) -> Unit,
    onFormRoleChange: (AccountRole) -> Unit,
    onToggleRestaurant: (String) -> Unit,
    onSaveUser: () -> Unit,
    onDismissForm: () -> Unit,
    onConfirmRemove: () -> Unit,
    onDismissRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = MaterialTheme.colorScheme.onSurface)
                }
                Column {
                    Text(
                        uiState.accountName,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        "Usuarios y roles de la cuenta",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Button(
                onClick = onInviteUser,
                colors = ButtonDefaults.buttonColors(containerColor = Blue500),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text("Invitar usuario", color = Color.White)
            }
        }

        ErrorSnackbarEffect(uiState.error)
        uiState.successMessage?.let { Text(it, color = MenuAdminTheme.colors.success, fontSize = 13.sp) }

        if (uiState.isLoading) {
            Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Blue500)
            }
        } else if (uiState.users.isEmpty()) {
            Text(
                "Esta cuenta no tiene usuarios. Invita al primero.",
                fontSize = 14.sp,
                color = MenuAdminTheme.colors.textMuted,
            )
        } else {
            uiState.users.forEach { user ->
                UserCard(user, onEdit = { onEditUser(user) }, onRemove = { onRequestRemove(user) })
            }
        }
    }

    if (uiState.isFormVisible) {
        UserFormDialog(uiState, onFormEmailChange, onFormRoleChange, onToggleRestaurant, onSaveUser, onDismissForm)
    }
    uiState.removingUser?.let { user ->
        AlertDialog(
            onDismissRequest = onDismissRemove,
            title = { Text("Quitar usuario", fontWeight = FontWeight.Bold, color = Red500) },
            text = {
                Text(
                    "¿Quitar a ${user.email} de la cuenta? Se eliminará su acceso (membresía e invitación).",
                    fontSize = 14.sp,
                )
            },
            confirmButton = {
                Button(
                    onClick = onConfirmRemove,
                    enabled = !uiState.isRemoving,
                    colors = ButtonDefaults.buttonColors(containerColor = Red500),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    if (uiState.isRemoving) {
                        CircularProgressIndicator(
                            Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text("Quitar", color = Color.White)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissRemove, enabled = !uiState.isRemoving) { Text("Cancelar") }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.surface,
        )
    }
}

@Composable
private fun UserCard(
    user: AccountUser,
    onEdit: () -> Unit,
    onRemove: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    user.email,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val statusText = if (user.status ==
                    AccountUserStatus.PENDING
                ) {
                    "Pendiente (sin primer login)"
                } else {
                    "Activo"
                }
                val detail = if (user.role == AccountRole.RESTAURANT_MANAGER && user.restaurantIds.isNotEmpty()) {
                    " · ${user.restaurantIds.size} restaurante(s)"
                } else {
                    ""
                }
                Text(
                    "${roleLabel(user.role)} · $statusText$detail",
                    fontSize = 12.sp,
                    color = if (user.status == AccountUserStatus.PENDING) MenuAdminTheme.colors.textMuted else Green500,
                )
            }
            IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Outlined.Edit,
                    "Editar",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            }
            IconButton(onClick = onRemove, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Outlined.Delete, "Quitar", tint = Red500, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun UserFormDialog(
    uiState: PlatformAccountDetailUiState,
    onEmailChange: (String) -> Unit,
    onRoleChange: (AccountRole) -> Unit,
    onToggleRestaurant: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    val isInvite = uiState.editingUser == null
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isInvite) "Invitar usuario" else "Editar usuario", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = uiState.formEmail,
                    onValueChange = onEmailChange,
                    label = { Text("Email") },
                    singleLine = true,
                    enabled = isInvite, // email is the doc id; not editable once it exists
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                )
                Text("Rol", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    RoleButton("Administrador", uiState.formRole == AccountRole.ACCOUNT_ADMIN, Modifier.weight(1f)) {
                        onRoleChange(AccountRole.ACCOUNT_ADMIN)
                    }
                    RoleButton("Encargado", uiState.formRole == AccountRole.RESTAURANT_MANAGER, Modifier.weight(1f)) {
                        onRoleChange(AccountRole.RESTAURANT_MANAGER)
                    }
                }
                if (uiState.formRole == AccountRole.RESTAURANT_MANAGER) {
                    Text("Restaurantes asignados", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (uiState.accountRestaurants.isEmpty()) {
                        Text(
                            "La cuenta no tiene restaurantes todavía.",
                            fontSize = 12.sp,
                            color = MenuAdminTheme.colors.textMuted,
                        )
                    } else {
                        Column(modifier = Modifier.heightIn(max = 200.dp).verticalScroll(rememberScrollState())) {
                            uiState.accountRestaurants.forEach { r ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().clickable {
                                        onToggleRestaurant(r.id)
                                    }.padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Checkbox(checked = r.id in uiState.formRestaurantIds, onCheckedChange = {
                                        onToggleRestaurant(r.id)
                                    })
                                    Text(
                                        r.name.ifBlank {
                                            r.id
                                        },
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                enabled = !uiState.isSaving && uiState.formEmail.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Blue500),
                shape = RoundedCornerShape(8.dp),
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("Guardar", color = Color.White)
                }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !uiState.isSaving) { Text("Cancelar") } },
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.surface,
    )
}

@Composable
private fun RoleButton(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    if (selected) {
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(containerColor = Blue500),
            shape = RoundedCornerShape(8.dp),
            modifier = modifier,
        ) {
            Text(label, color = Color.White, fontSize = 13.sp)
        }
    } else {
        OutlinedButton(onClick = onClick, shape = RoundedCornerShape(8.dp), modifier = modifier) {
            Text(label, fontSize = 13.sp)
        }
    }
}

private fun roleLabel(role: AccountRole): String =
    when (role) {
        AccountRole.ACCOUNT_ADMIN -> "Administrador"
        AccountRole.RESTAURANT_MANAGER -> "Encargado de restaurante"
    }

@Preview
@Composable
private fun PreviewPlatformAccountDetailContent() {
    MenuAdminTheme {
        PlatformAccountDetailContent(
            uiState = PlatformAccountDetailUiState(
                isLoading = false,
                accountName = "Alacor",
                users = listOf(
                    AccountUser(
                        "admin@apptolast.com",
                        "uid1",
                        AccountRole.ACCOUNT_ADMIN,
                        emptyList(),
                        AccountUserStatus.ACTIVE,
                    ),
                    AccountUser(
                        "manager@apptolast.com",
                        "uid2",
                        AccountRole.RESTAURANT_MANAGER,
                        listOf("la-brava-piconera"),
                        AccountUserStatus.ACTIVE,
                    ),
                    AccountUser("nuevo@x.com", null, AccountRole.ACCOUNT_ADMIN, emptyList(), AccountUserStatus.PENDING),
                ),
                accountRestaurants = listOf(Restaurant(id = "la-brava-piconera", name = "La Brava Piconera")),
            ),
            onBack = {},
            onInviteUser = {},
            onEditUser = {},
            onRequestRemove = {},
            onFormEmailChange = {},
            onFormRoleChange = {},
            onToggleRestaurant = {},
            onSaveUser = {},
            onDismissForm = {},
            onConfirmRemove = {},
            onDismissRemove = {},
        )
    }
}
