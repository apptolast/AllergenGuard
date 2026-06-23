package org.apptolast.menuadmin.presentation.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.apptolast.menuadmin.presentation.components.ErrorSnackbarEffect
import org.apptolast.menuadmin.presentation.theme.Blue500
import org.apptolast.menuadmin.presentation.theme.MenuAdminTheme
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProfileScreen(
    onAccountDeleted: () -> Unit,
    viewModel: ProfileViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    ProfileContent(
        uiState = uiState,
        onStartEditName = viewModel::onStartEditName,
        onNameDraftChange = viewModel::onNameDraftChange,
        onSaveName = viewModel::onSaveName,
        onCancelEditName = viewModel::onCancelEditName,
    )
}

@Composable
fun ProfileContent(
    uiState: ProfileUiState,
    onStartEditName: () -> Unit,
    onNameDraftChange: (String) -> Unit,
    onSaveName: () -> Unit,
    onCancelEditName: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
                text = "Mi Perfil",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Informacion de tu cuenta",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Info card — sized to its content so the name's edit button sits next to the name, not at the
        // far edge of the screen.
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "Cuenta",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                EditableNameRow(
                    name = uiState.name,
                    isEditing = uiState.isEditingName,
                    nameDraft = uiState.nameDraft,
                    isSaving = uiState.isSavingName,
                    onStartEdit = onStartEditName,
                    onDraftChange = onNameDraftChange,
                    onSave = onSaveName,
                    onCancel = onCancelEditName,
                )
                InfoRow(label = "Correo", value = uiState.email ?: "No disponible")
                InfoRow(label = "ID de usuario", value = uiState.userId ?: "—")
                InfoRow(label = "Rol", value = uiState.roleLabel ?: "—")
                InfoRow(
                    label = "Correo verificado",
                    value = if (uiState.emailVerified) "Si" else "No",
                )
            }
        }

        // Errors surface through the app-wide snackbar instead of inline red text.
        ErrorSnackbarEffect(uiState.error)
    }
}

@Composable
private fun EditableNameRow(
    name: String?,
    isEditing: Boolean,
    nameDraft: String,
    isSaving: Boolean,
    onStartEdit: () -> Unit,
    onDraftChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Nombre",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (isEditing) {
            OutlinedTextField(
                value = nameDraft,
                onValueChange = onDraftChange,
                singleLine = true,
                enabled = !isSaving,
                modifier = Modifier.width(320.dp),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Blue500,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                ),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onSave,
                    enabled = !isSaving && nameDraft.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = Blue500),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text("Guardar", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
                TextButton(onClick = onCancel, enabled = !isSaving) {
                    Text("Cancelar")
                }
            }
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = name ?: "No disponible",
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                IconButton(onClick = onStartEdit, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Editar nombre",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Preview
@Composable
private fun PreviewProfileContent() {
    MenuAdminTheme {
        ProfileContent(
            uiState = ProfileUiState(
                name = "Ana Administradora",
                email = "admin@apptolast.com",
                userId = "kJ3mZ9x...",
                roleLabel = "Administrador",
                emailVerified = true,
            ),
            onStartEditName = {},
            onNameDraftChange = {},
            onSaveName = {},
            onCancelEditName = {},
        )
    }
}

@Preview
@Composable
private fun PreviewProfileContentEditing() {
    MenuAdminTheme {
        ProfileContent(
            uiState = ProfileUiState(
                name = "Ana Administradora",
                email = "admin@apptolast.com",
                userId = "kJ3mZ9x...",
                roleLabel = "Administrador",
                emailVerified = true,
                isEditingName = true,
                nameDraft = "Ana Administradora",
            ),
            onStartEditName = {},
            onNameDraftChange = {},
            onSaveName = {},
            onCancelEditName = {},
        )
    }
}
