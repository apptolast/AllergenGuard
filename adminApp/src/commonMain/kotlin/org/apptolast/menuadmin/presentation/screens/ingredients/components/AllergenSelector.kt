package org.apptolast.menuadmin.presentation.screens.ingredients.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.sp
import org.apptolast.menuadmin.domain.model.AllergenType
import org.apptolast.menuadmin.domain.model.ContainmentLevel
import org.apptolast.menuadmin.presentation.components.LucideIcon
import org.apptolast.menuadmin.presentation.theme.Amber500
import org.apptolast.menuadmin.presentation.theme.Blue100
import org.apptolast.menuadmin.presentation.theme.MenuAdminTheme
import org.apptolast.menuadmin.presentation.theme.color

/**
 * Allergen selector with containment level support.
 * Click cycle: inactive -> CONTAINS -> MAY_CONTAIN -> inactive
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AllergenSelector(
    allergens: Map<AllergenType, ContainmentLevel>,
    onToggle: (AllergenType) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AllergenType.entries.forEach { allergen ->
            AllergenCard(
                allergenType = allergen,
                containmentLevel = allergens[allergen],
                onClick = { onToggle(allergen) },
            )
        }
    }
}

@Composable
private fun AllergenCard(
    allergenType: AllergenType,
    containmentLevel: ContainmentLevel?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isActive = containmentLevel != null
    val isMayContain = containmentLevel == ContainmentLevel.MAY_CONTAIN
    val accentColor = if (isMayContain) Amber500 else allergenType.color
    val backgroundColor = if (isActive) {
        accentColor.copy(alpha = 0.12f)
    } else {
        Blue100.copy(alpha = 0.3f)
    }
    val borderColor = if (isActive) accentColor else MaterialTheme.colorScheme.outlineVariant
    val textColor = if (isActive) accentColor else MaterialTheme.colorScheme.onSurfaceVariant
    val shape = RoundedCornerShape(12.dp)

    Column(
        modifier = modifier
            .widthIn(min = 100.dp)
            .clip(shape)
            .border(
                width = if (isActive) 2.dp else 1.dp,
                color = borderColor,
                shape = shape,
            )
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        LucideIcon(
            codepoint = allergenType.icon,
            size = 28.sp,
            color = if (isActive) accentColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        )
        Text(
            text = allergenType.nameEs,
            color = if (isActive) MaterialTheme.colorScheme.onSurface else textColor,
            fontSize = 12.sp,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
        if (containmentLevel != null) {
            Text(
                text = containmentLevel.labelEs,
                color = accentColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
    }
}

@Preview
@Composable
private fun PreviewAllergenSelector() {
    MenuAdminTheme {
        AllergenSelector(
            allergens = mapOf(
                AllergenType.GLUTEN to ContainmentLevel.CONTAINS,
                AllergenType.DAIRY to ContainmentLevel.MAY_CONTAIN,
            ),
            onToggle = {},
        )
    }
}

@Preview
@Composable
private fun PreviewAllergenCardContains() {
    MenuAdminTheme {
        AllergenCard(
            allergenType = AllergenType.GLUTEN,
            containmentLevel = ContainmentLevel.CONTAINS,
            onClick = {},
        )
    }
}

@Preview
@Composable
private fun PreviewAllergenCardMayContain() {
    MenuAdminTheme {
        AllergenCard(
            allergenType = AllergenType.EGGS,
            containmentLevel = ContainmentLevel.MAY_CONTAIN,
            onClick = {},
        )
    }
}

@Preview
@Composable
private fun PreviewAllergenCardInactive() {
    MenuAdminTheme {
        AllergenCard(
            allergenType = AllergenType.DAIRY,
            containmentLevel = null,
            onClick = {},
        )
    }
}
