package org.apptolast.menuadmin.presentation.screens.backup

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import menuadmin.adminapp.generated.resources.Res
import menuadmin.adminapp.generated.resources.action_cancel
import menuadmin.adminapp.generated.resources.backup_confirm_destination
import menuadmin.adminapp.generated.resources.backup_confirm_import_title
import menuadmin.adminapp.generated.resources.backup_confirm_ingredients
import menuadmin.adminapp.generated.resources.backup_confirm_menus
import menuadmin.adminapp.generated.resources.backup_confirm_pre_import_backup
import menuadmin.adminapp.generated.resources.backup_confirm_recipes
import menuadmin.adminapp.generated.resources.backup_confirm_replace_title
import menuadmin.adminapp.generated.resources.backup_confirm_to_delete
import menuadmin.adminapp.generated.resources.backup_export_button
import menuadmin.adminapp.generated.resources.backup_export_description
import menuadmin.adminapp.generated.resources.backup_export_title
import menuadmin.adminapp.generated.resources.backup_import_button
import menuadmin.adminapp.generated.resources.backup_import_description
import menuadmin.adminapp.generated.resources.backup_import_mode_label
import menuadmin.adminapp.generated.resources.backup_import_title
import menuadmin.adminapp.generated.resources.backup_mode_merge
import menuadmin.adminapp.generated.resources.backup_mode_merge_desc
import menuadmin.adminapp.generated.resources.backup_mode_replace
import menuadmin.adminapp.generated.resources.backup_mode_replace_desc
import menuadmin.adminapp.generated.resources.backup_screen_subtitle
import menuadmin.adminapp.generated.resources.backup_screen_title
import menuadmin.adminapp.generated.resources.backup_target_expand
import menuadmin.adminapp.generated.resources.backup_target_hint
import menuadmin.adminapp.generated.resources.backup_target_label
import menuadmin.adminapp.generated.resources.backup_target_placeholder
import org.apptolast.menuadmin.domain.model.Restaurant
import org.apptolast.menuadmin.presentation.theme.Blue500
import org.apptolast.menuadmin.presentation.theme.Green500
import org.apptolast.menuadmin.presentation.theme.MenuAdminTheme
import org.jetbrains.compose.resources.stringResource
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
        onSelectTargetRestaurant = viewModel::selectTargetRestaurant,
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
    onSelectTargetRestaurant: (String) -> Unit,
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
                text = stringResource(Res.string.backup_screen_title),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(Res.string.backup_screen_subtitle),
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
                        text = stringResource(Res.string.backup_export_title),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(Res.string.backup_export_description),
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
                    Text(text = stringResource(Res.string.backup_export_button))
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
                        text = stringResource(Res.string.backup_import_title),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(Res.string.backup_import_description),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                if (uiState.restaurants.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    TargetRestaurantSelector(
                        restaurants = uiState.restaurants,
                        selectedId = uiState.targetRestaurantId,
                        onSelect = onSelectTargetRestaurant,
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(Res.string.backup_import_mode_label),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                ImportModeOption(
                    selected = importMode == ImportMode.MERGE,
                    title = stringResource(Res.string.backup_mode_merge),
                    description = stringResource(Res.string.backup_mode_merge_desc),
                    onSelect = { importMode = ImportMode.MERGE },
                )
                ImportModeOption(
                    selected = importMode == ImportMode.REPLACE,
                    title = stringResource(Res.string.backup_mode_replace),
                    description = stringResource(Res.string.backup_mode_replace_desc),
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
                        text = stringResource(Res.string.backup_import_button),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@Composable
private fun TargetRestaurantSelector(
    restaurants: List<Restaurant>,
    selectedId: String?,
    onSelect: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = restaurants.find { it.id == selectedId }?.name
        ?: stringResource(Res.string.backup_target_placeholder)

    Column {
        Text(
            text = stringResource(Res.string.backup_target_label),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                    .clickable { expanded = true }
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = selectedName,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = stringResource(Res.string.backup_target_expand),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                restaurants.forEach { restaurant ->
                    DropdownMenuItem(
                        text = { Text(restaurant.name) },
                        onClick = {
                            onSelect(restaurant.id)
                            expanded = false
                        },
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(Res.string.backup_target_hint),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
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
        title = {
            Text(
                text = if (mode == ImportMode.REPLACE) {
                    stringResource(Res.string.backup_confirm_replace_title)
                } else {
                    stringResource(Res.string.backup_confirm_import_title)
                },
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(Res.string.backup_confirm_destination, preview.restaurantName),
                    fontSize = 14.sp,
                )
                Text(
                    text = stringResource(
                        Res.string.backup_confirm_ingredients,
                        preview.ingredientsNew,
                        preview.ingredientsUpdated,
                    ),
                    fontSize = 14.sp,
                )
                Text(
                    text = stringResource(
                        Res.string.backup_confirm_recipes,
                        preview.recipesNew,
                        preview.recipesUpdated,
                    ),
                    fontSize = 14.sp,
                )
                Text(
                    text = stringResource(
                        Res.string.backup_confirm_menus,
                        preview.menusNew,
                        preview.menusUpdated,
                    ),
                    fontSize = 14.sp,
                )
                if (mode == ImportMode.REPLACE) {
                    val toDelete = preview.ingredientsToDelete + preview.recipesToDelete + preview.menusToDelete
                    Text(
                        text = stringResource(
                            Res.string.backup_confirm_to_delete,
                            toDelete,
                            preview.ingredientsToDelete,
                            preview.recipesToDelete,
                            preview.menusToDelete,
                        ),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(Res.string.backup_confirm_pre_import_backup, preview.backupFileName),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = if (mode == ImportMode.REPLACE) {
                        stringResource(Res.string.backup_mode_replace)
                    } else {
                        stringResource(Res.string.backup_mode_merge)
                    },
                    color = if (mode == ImportMode.REPLACE) MaterialTheme.colorScheme.error else Blue500,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(text = stringResource(Res.string.action_cancel)) }
        },
    )
}

@Preview
@Composable
private fun BackupRestoreContentPreview() {
    MenuAdminTheme {
        BackupRestoreContent(
            uiState = BackupUiState(
                restaurants = listOf(
                    Restaurant(id = "1", name = "El Rincon del Mar"),
                    Restaurant(id = "2", name = "Hotel Valsequillo"),
                ),
                targetRestaurantId = "1",
            ),
            onExport = {},
            onAnalyzeImport = {},
            onConfirmImport = {},
            onCancelImport = {},
            onClearMessage = {},
            onSelectTargetRestaurant = {},
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
            onSelectTargetRestaurant = {},
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
