package com.apptolast.menufrontend.features.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.apptolast.menufrontend.core.theme.AllergenGuardTheme
import com.apptolast.menufrontend.core.theme.extendedColors
import com.apptolast.menufrontend.domain.model.Allergen

/**
 * Toggleable allergen chip reused by the menu filter row and the profile allergy edit sheet.
 */
@Composable
fun AllergenFilterChip(
    allergen: Allergen,
    label: String,
    isSelected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.extendedColors
    FilterChip(
        selected = isSelected,
        onClick = onToggle,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            )
        },
        leadingIcon = {
            Icon(
                imageVector = allergen.icon(),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
        },
        modifier = modifier,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = colors.chipUnselectedContainer,
            labelColor = colors.onChipUnselected,
            iconColor = colors.onChipUnselected,
            selectedContainerColor = colors.dangerContainer,
            selectedLabelColor = colors.onDangerContainer,
            selectedLeadingIconColor = colors.onDangerContainer,
        ),
        border = FilterChipDefaults.filterChipBorder(
            borderColor = MaterialTheme.colorScheme.outlineVariant,
            selectedBorderColor = colors.onDangerContainer,
            enabled = true,
            selected = isSelected,
        ),
    )
}

@Preview
@Composable
private fun PreviewAllergenFilterChip() {
    AllergenGuardTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AllergenFilterChip(
                allergen = Allergen.GLUTEN,
                label = "Gluten",
                isSelected = true,
                onToggle = {},
            )
            AllergenFilterChip(
                allergen = Allergen.FISH,
                label = "Pescado",
                isSelected = false,
                onToggle = {},
            )
        }
    }
}
