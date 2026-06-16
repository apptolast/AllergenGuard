package com.apptolast.menufrontend.features.profile.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.apptolast.menufrontend.core.theme.AllergenGuardTheme
import com.apptolast.menufrontend.domain.model.Allergen
import com.apptolast.menufrontend.features.components.AllergenFilterChip
import com.apptolast.menufrontend.resources.Res
import com.apptolast.menufrontend.resources.profile_edit_allergies_title
import com.apptolast.menufrontend.resources.profile_save
import org.jetbrains.compose.resources.stringResource

/**
 * Bottom sheet that lets the user toggle every EU allergen and persist the selection on save.
 * Selection edits happen on a working copy in the ViewModel; only [onSave] commits them.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllergenEditSheet(
    selection: Set<Allergen>,
    allergenLabels: Map<Allergen, String>,
    isSaving: Boolean,
    onToggle: (Allergen) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        AllergenEditSheetContent(
            selection = selection,
            allergenLabels = allergenLabels,
            isSaving = isSaving,
            onToggle = onToggle,
            onSave = onSave,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AllergenEditSheetContent(
    selection: Set<Allergen>,
    allergenLabels: Map<Allergen, String>,
    isSaving: Boolean,
    onToggle: (Allergen) -> Unit,
    onSave: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(Res.string.profile_edit_allergies_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Allergen.entries.forEach { allergen ->
                AllergenFilterChip(
                    allergen = allergen,
                    label = allergenLabels[allergen] ?: allergen.name,
                    isSelected = allergen in selection,
                    onToggle = { onToggle(allergen) },
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        Button(
            onClick = onSave,
            enabled = !isSaving,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            } else {
                Text(stringResource(Res.string.profile_save))
            }
        }
    }
}

@Preview
@Composable
private fun PreviewAllergenEditSheetContent() {
    AllergenGuardTheme {
        AllergenEditSheetContent(
            selection = setOf(Allergen.GLUTEN, Allergen.DAIRY, Allergen.PEANUTS),
            allergenLabels = Allergen.entries.associateWith { it.name },
            isSaving = false,
            onToggle = {},
            onSave = {},
        )
    }
}
