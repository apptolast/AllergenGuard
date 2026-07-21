package com.apptolast.menufrontend.features.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.apptolast.menufrontend.core.theme.AllergenGuardTheme
import com.apptolast.menufrontend.core.theme.extendedColors
import com.apptolast.menufrontend.domain.model.Allergen
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.FilterQuality
import com.apptolast.menufrontend.features.components.iconResource
import org.jetbrains.compose.resources.imageResource

/**
 * Larger, equal-width selectable allergen cell used by the profile edit sheet's 3-column grid.
 * Icon on top, label below. Selected = danger palette; unselected = neutral chip palette.
 * (The smaller [com.apptolast.menufrontend.features.components.AllergenFilterChip] stays for the
 * menu filter row, which needs a compact horizontal chip.)
 */
@Composable
fun AllergenGridItem(
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

    Column(
        modifier = modifier
            .heightIn(min = 68.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .background(container)
            .clickable(onClick = onToggle)
            .padding(horizontal = 6.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            bitmap = imageResource(allergen.iconResource()),
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            filterQuality = FilterQuality.High,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = content,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 2,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview
@Composable
private fun PreviewAllergenGridItem() {
    AllergenGuardTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AllergenGridItem(
                allergen = Allergen.GLUTEN,
                label = "Gluten",
                isSelected = true,
                onToggle = {},
                modifier = Modifier.weight(1f),
            )
            AllergenGridItem(
                allergen = Allergen.TREE_NUTS,
                label = "Frutos secos",
                isSelected = false,
                onToggle = {},
                modifier = Modifier.weight(1f),
            )
            AllergenGridItem(
                allergen = Allergen.FISH,
                label = "Pescado",
                isSelected = false,
                onToggle = {},
                modifier = Modifier.weight(1f),
            )
        }
    }
}
