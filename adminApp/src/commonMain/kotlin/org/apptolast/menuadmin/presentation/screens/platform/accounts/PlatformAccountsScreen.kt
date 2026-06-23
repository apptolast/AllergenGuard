package org.apptolast.menuadmin.presentation.screens.platform.accounts

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import org.apptolast.menuadmin.domain.model.Account
import org.apptolast.menuadmin.presentation.components.ErrorSnackbarEffect
import org.apptolast.menuadmin.presentation.theme.Blue500
import org.apptolast.menuadmin.presentation.theme.MenuAdminTheme
import org.apptolast.menuadmin.presentation.theme.Red500
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PlatformAccountsScreen(
    onOpenAccount: (String) -> Unit,
    onManageAccount: (Account) -> Unit,
    viewModel: PlatformAccountsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    PlatformAccountsContent(
        uiState = uiState,
        onOpenAccount = onOpenAccount,
        onManageAccount = { account ->
            // Enter impersonation, then let the navigation callback open the scoped workspace.
            viewModel.onEnterAccount(account)
            onManageAccount(account)
        },
        onNewAccount = viewModel::onNewAccount,
        onEditAccount = viewModel::onEditAccount,
        onRequestDelete = viewModel::onRequestDelete,
        onFormNameChange = viewModel::onFormNameChange,
        onFormRegionChange = viewModel::onFormRegionChange,
        onFormLanguageChange = viewModel::onFormLanguageChange,
        onFormFirstAdminEmailChange = viewModel::onFormFirstAdminEmailChange,
        onSaveAccount = viewModel::onSaveAccount,
        onDismissForm = viewModel::onDismissForm,
        onDeleteConfirmTextChange = viewModel::onDeleteConfirmTextChange,
        onConfirmDelete = viewModel::onConfirmDelete,
        onDismissDelete = viewModel::onDismissDelete,
    )
}

@Composable
fun PlatformAccountsContent(
    uiState: PlatformAccountsUiState,
    onOpenAccount: (String) -> Unit,
    onManageAccount: (Account) -> Unit,
    onNewAccount: () -> Unit,
    onEditAccount: (Account) -> Unit,
    onRequestDelete: (Account) -> Unit,
    onFormNameChange: (String) -> Unit,
    onFormRegionChange: (String) -> Unit,
    onFormLanguageChange: (String) -> Unit,
    onFormFirstAdminEmailChange: (String) -> Unit,
    onSaveAccount: () -> Unit,
    onDismissForm: () -> Unit,
    onDeleteConfirmTextChange: (String) -> Unit,
    onConfirmDelete: () -> Unit,
    onDismissDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    "Cuentas",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    "Gestiona las cuentas de la plataforma",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Button(
                onClick = onNewAccount,
                colors = ButtonDefaults.buttonColors(containerColor = Blue500),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text("Nueva Cuenta", color = Color.White)
            }
        }

        ErrorSnackbarEffect(uiState.error)
        uiState.successMessage?.let { Text(it, color = MenuAdminTheme.colors.success, fontSize = 13.sp) }

        if (uiState.isLoading) {
            Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Blue500)
            }
        } else if (uiState.accounts.isEmpty()) {
            Text("No hay cuentas todavía. Crea la primera.", fontSize = 14.sp, color = MenuAdminTheme.colors.textMuted)
        } else {
            uiState.accounts.forEach { account ->
                AccountCard(
                    account = account,
                    onClick = { onOpenAccount(account.id) },
                    onManage = { onManageAccount(account) },
                    onEdit = { onEditAccount(account) },
                    onDelete = { onRequestDelete(account) },
                )
            }
        }
    }

    if (uiState.isFormVisible) {
        AccountFormDialog(
            uiState,
            onFormNameChange,
            onFormRegionChange,
            onFormLanguageChange,
            onFormFirstAdminEmailChange,
            onSaveAccount,
            onDismissForm,
        )
    }
    uiState.deletingAccount?.let { account ->
        DeleteAccountDialog(account, uiState, onDeleteConfirmTextChange, onConfirmDelete, onDismissDelete)
    }
}

@Composable
private fun AccountCard(
    account: Account,
    onClick: () -> Unit,
    onManage: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    account.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    "${account.id}  ·  ${account.region} / ${account.language}",
                    fontSize = 12.sp,
                    color = MenuAdminTheme.colors.textMuted,
                )
            }
            OutlinedButton(onClick = onManage, shape = RoundedCornerShape(8.dp)) {
                Text("Gestionar", fontSize = 13.sp)
            }
            IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Outlined.Edit,
                    "Editar",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Outlined.Delete, "Eliminar", tint = Red500, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun AccountFormDialog(
    uiState: PlatformAccountsUiState,
    onNameChange: (String) -> Unit,
    onRegionChange: (String) -> Unit,
    onLanguageChange: (String) -> Unit,
    onFirstAdminEmailChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    val isCreate = uiState.editingAccount == null
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isCreate) "Nueva Cuenta" else "Editar Cuenta", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = uiState.formName,
                    onValueChange = onNameChange,
                    label = { Text("Nombre de la cuenta") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                )
                Text("Región", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                SegmentedToggle(
                    listOf("EU" to "EU (UE)", "UK" to "UK (Reino Unido)"),
                    uiState.formRegion,
                    onRegionChange,
                )
                Text("Idioma", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                SegmentedToggle(listOf("es" to "Español", "en" to "English"), uiState.formLanguage, onLanguageChange)
                if (isCreate) {
                    OutlinedTextField(
                        value = uiState.formFirstAdminEmail,
                        onValueChange = onFirstAdminEmailChange,
                        label = { Text("Email del primer administrador (opcional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                    )
                    Text(
                        "Se le creará una invitación como ACCOUNT_ADMIN; entrará en su primer login.",
                        fontSize = 11.sp,
                        color = MenuAdminTheme.colors.textMuted,
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                enabled = !uiState.isSaving && uiState.formName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Blue500),
                shape = RoundedCornerShape(8.dp),
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
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
private fun DeleteAccountDialog(
    account: Account,
    uiState: PlatformAccountsUiState,
    onConfirmTextChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Eliminar cuenta", fontWeight = FontWeight.Bold, color = Red500) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Esto borrará DEFINITIVAMENTE la cuenta \"${account.name}\" y TODO su contenido: " +
                        "restaurantes, recetas, menús, catálogo de ingredientes, usuarios e invitaciones.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    "Se descargará un backup JSON antes de borrar. Escribe el nombre de la cuenta para confirmar:",
                    fontSize = 13.sp,
                    color = MenuAdminTheme.colors.textMuted,
                )
                OutlinedTextField(
                    value = uiState.deleteConfirmText,
                    onValueChange = onConfirmTextChange,
                    label = { Text(account.name) },
                    singleLine = true,
                    enabled = !uiState.isDeleting,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !uiState.isDeleting && uiState.deleteConfirmText.trim() == account.name.trim(),
                colors = ButtonDefaults.buttonColors(containerColor = Red500),
                shape = RoundedCornerShape(8.dp),
            ) {
                if (uiState.isDeleting) {
                    CircularProgressIndicator(Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Descargar backup y borrar", color = Color.White)
                }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !uiState.isDeleting) { Text("Cancelar") } },
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.surface,
    )
}

/** Two-or-more option single-select toggle (selected = filled blue, others = outlined). */
@Composable
private fun SegmentedToggle(
    options: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        options.forEach { (value, label) ->
            if (value == selected) {
                Button(
                    onClick = { onSelect(value) },
                    colors = ButtonDefaults.buttonColors(containerColor = Blue500),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f),
                ) { Text(label, color = Color.White, fontSize = 13.sp) }
            } else {
                OutlinedButton(
                    onClick = { onSelect(value) },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f),
                ) { Text(label, fontSize = 13.sp) }
            }
        }
    }
}

@Preview
@Composable
private fun PreviewPlatformAccountsContent() {
    MenuAdminTheme {
        PlatformAccountsContent(
            uiState = PlatformAccountsUiState(
                isLoading = false,
                accounts = listOf(
                    Account("acc_alacor", "Alacor", "EU", "es"),
                    Account("acc_mati", "Mati Restaurantes", "EU", "es"),
                ),
            ),
            onOpenAccount = {},
            onManageAccount = {},
            onNewAccount = {},
            onEditAccount = {},
            onRequestDelete = {},
            onFormNameChange = {},
            onFormRegionChange = {},
            onFormLanguageChange = {},
            onFormFirstAdminEmailChange = {},
            onSaveAccount = {},
            onDismissForm = {},
            onDeleteConfirmTextChange = {},
            onConfirmDelete = {},
            onDismissDelete = {},
        )
    }
}
