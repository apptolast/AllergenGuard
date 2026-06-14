package org.apptolast.menuadmin.presentation.screens.backup

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.apptolast.menuadmin.presentation.theme.Blue500
import org.apptolast.menuadmin.presentation.theme.Green500
import org.apptolast.menuadmin.presentation.theme.MenuAdminTheme
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun BackupRestoreScreen(viewModel: BackupViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    BackupRestoreContent(
        uiState = uiState,
        onExport = viewModel::exportData,
        onAnalyzeImport = viewModel::analyzeImport,
        onConfirmImport = viewModel::confirmImport,
        onCancelImport = viewModel::cancelImport,
        onClearMessage = viewModel::clearMessage,
    )
}

@Composable
fun BackupRestoreContent(
    uiState: BackupUiState,
    onExport: () -> Unit,
    onAnalyzeImport: () -> Unit,
    onConfirmImport: (ImportMode) -> Unit,
    onCancelImport: () -> Unit,
    onClearMessage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // UI-only flag: which import strategy the user wants. Persisted across the confirm dialog.
    var importMode by remember { mutableStateOf(ImportMode.MERGE) }

    uiState.preview?.let { preview ->
        ImportConfirmDialog(
            preview = preview,
            mode = importMode,
            onConfirm = { onConfirmImport(importMode) },
            onDismiss = onCancelImport,
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Column {
            Text(
                text = "Backup / Restaurar",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Exporta e importa tus datos",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Status message
        uiState.message?.let { message ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Green500.copy(alpha = 0.1f)),
                border = BorderStroke(1.dp, Green500.copy(alpha = 0.3f)),
            ) {
                Text(
                    text = message,
                    modifier = Modifier.padding(16.dp),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        // Export Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.FileDownload,
                        contentDescription = null,
                        tint = Green500,
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Exportar Datos",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Descarga todos tus ingredientes, recetas y menus en formato JSON",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onExport,
                    enabled = !uiState.isExporting,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Blue500,
                        contentColor = Color.White,
                    ),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    if (uiState.isExporting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.FileDownload,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Exportar JSON")
                }
            }
        }

        // Import Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.FileUpload,
                        contentDescription = null,
                        tint = Blue500,
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Importar Datos",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Restaura tus datos desde un archivo JSON. Antes de aplicar se descarga " +
                        "automaticamente una copia de seguridad del estado actual.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Modo de importacion",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                ImportModeOption(
                    selected = importMode == ImportMode.MERGE,
                    title = "Combinar",
                    description = "Actualiza los existentes y anade los nuevos. No borra nada.",
                    onSelect = { importMode = ImportMode.MERGE },
                )
                ImportModeOption(
                    selected = importMode == ImportMode.REPLACE,
                    title = "Reemplazar",
                    description = "Deja la base de datos igual que el fichero (borra lo que no este en el).",
                    onSelect = { importMode = ImportMode.REPLACE },
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = onAnalyzeImport,
                    enabled = !uiState.isImporting,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    if (uiState.isImporting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.onSurface,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.FileUpload,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Importar JSON",
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@Composable
private fun ImportModeOption(
    selected: Boolean,
    title: String,
    description: String,
    onSelect: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onSelect)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top,
    ) {
        RadioButton(selected = selected, onClick = onSelect)
        Spacer(modifier = Modifier.width(4.dp))
        Column(modifier = Modifier.padding(top = 12.dp)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ImportConfirmDialog(
    preview: ImportPreview,
    mode: ImportMode,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = if (mode == ImportMode.REPLACE) "Confirmar reemplazo" else "Confirmar importacion") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = "Restaurante: ${preview.restaurantName}", fontSize = 14.sp)
                Text(
                    text = "Ingredientes: ${preview.ingredientsNew} nuevos, ${preview.ingredientsUpdated} actualizados",
                    fontSize = 14.sp,
                )
                Text(
                    text = "Recetas: ${preview.recipesNew} nuevas, ${preview.recipesUpdated} actualizadas",
                    fontSize = 14.sp,
                )
                Text(
                    text = "Menus: ${preview.menusNew} nuevos, ${preview.menusUpdated} actualizados",
                    fontSize = 14.sp,
                )
                if (mode == ImportMode.REPLACE) {
                    val toDelete = preview.ingredientsToDelete + preview.recipesToDelete + preview.menusToDelete
                    Text(
                        text = "Se eliminaran $toDelete elementos que no estan en el fichero " +
                            "(${preview.ingredientsToDelete} ingredientes, ${preview.recipesToDelete} recetas, " +
                            "${preview.menusToDelete} menus).",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Copia de seguridad descargada: ${preview.backupFileName}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = if (mode == ImportMode.REPLACE) "Reemplazar" else "Combinar",
                    color = if (mode == ImportMode.REPLACE) MaterialTheme.colorScheme.error else Blue500,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(text = "Cancelar") }
        },
    )
}

@Preview
@Composable
private fun BackupRestoreContentPreview() {
    MenuAdminTheme {
        BackupRestoreContent(
            uiState = BackupUiState(),
            onExport = {},
            onAnalyzeImport = {},
            onConfirmImport = {},
            onCancelImport = {},
            onClearMessage = {},
        )
    }
}

@Preview
@Composable
private fun BackupRestoreContentWithMessagePreview() {
    MenuAdminTheme {
        BackupRestoreContent(
            uiState = BackupUiState(
                message = "Combinacion completado: 45 ingredientes, 12 recetas, 3 menus importados",
            ),
            onExport = {},
            onAnalyzeImport = {},
            onConfirmImport = {},
            onCancelImport = {},
            onClearMessage = {},
        )
    }
}

@Preview
@Composable
private fun ImportConfirmDialogPreview() {
    MenuAdminTheme {
        ImportConfirmDialog(
            preview = ImportPreview(
                restaurantName = "El Rincon del Mar",
                ingredientsNew = 5,
                ingredientsUpdated = 12,
                recipesNew = 2,
                recipesUpdated = 3,
                menusNew = 1,
                menusUpdated = 0,
                ingredientsToDelete = 4,
                recipesToDelete = 1,
                menusToDelete = 0,
                backupFileName = "menuadmin_backup_pre-import_2026-06-14T10-30-00.json",
            ),
            mode = ImportMode.REPLACE,
            onConfirm = {},
            onDismiss = {},
        )
    }
}
