package com.apptolast.menufrontend.features.menu.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.apptolast.menufrontend.core.theme.AllergenActiveBg
import com.apptolast.menufrontend.core.theme.AllergenActiveText
import com.apptolast.menufrontend.core.theme.AllergenInactiveBg
import com.apptolast.menufrontend.core.theme.AllergenInactiveText
import com.apptolast.menufrontend.domain.model.Allergen
import com.apptolast.menufrontend.features.components.icon

@Composable
fun AllergenFilterChip(
    allergen: Allergen,
    label: String,
    isSelected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
            containerColor = AllergenInactiveBg,
            labelColor = AllergenInactiveText,
            iconColor = AllergenInactiveText,
            selectedContainerColor = AllergenActiveBg,
            selectedLabelColor = AllergenActiveText,
            selectedLeadingIconColor = AllergenActiveText,
        ),
        border = FilterChipDefaults.filterChipBorder(
            borderColor = MaterialTheme.colorScheme.outlineVariant,
            selectedBorderColor = AllergenActiveText,
            enabled = true,
            selected = isSelected,
        ),
    )
}
