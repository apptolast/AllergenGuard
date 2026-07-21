package com.apptolast.menufrontend.features.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Image
import org.jetbrains.compose.resources.painterResource
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.apptolast.menufrontend.core.theme.AllergenGuardTheme
import com.apptolast.menufrontend.core.theme.extendedColors
import com.apptolast.menufrontend.domain.model.Allergen

/**
 * Single reusable allergen pill chip: a compact icon + label. Shared by the menu filter row (where it
 * toggles a filter) and the profile "My Allergies" display (where tapping opens the edit sheet).
 * Selected = danger palette; unselected = neutral chip palette. The taller multi-select cell used in
 * the edit sheet is a separate component (AllergenGridItem) on purpose.
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
    val container = if (isSelected) colors.dangerContainer else colors.chipUnselectedContainer
    val content = if (isSelected) colors.onDangerContainer else colors.onChipUnselected
    val borderColor = if (isSelected) colors.onDangerContainer else MaterialTheme.colorScheme.outlineVariant

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .background(container)
            .clickable(onClick = onToggle)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Image(
            painter = painterResource(allergen.iconResource()),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = content,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
        )
    }
}

@Preview
@Composable
private fun PreviewAllergenFilterChip() {
    AllergenGuardTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
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
