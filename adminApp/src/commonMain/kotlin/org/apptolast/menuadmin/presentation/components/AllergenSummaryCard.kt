package org.apptolast.menuadmin.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import menuadmin.adminapp.generated.resources.Res
import menuadmin.adminapp.generated.resources.common_allergen_contains
import menuadmin.adminapp.generated.resources.common_allergen_free
import menuadmin.adminapp.generated.resources.common_allergen_traces
import org.apptolast.menuadmin.domain.model.AllergenType
import org.apptolast.menuadmin.domain.model.ContainmentLevel
import org.apptolast.menuadmin.presentation.theme.MenuAdminTheme
import org.apptolast.menuadmin.presentation.theme.color
import org.jetbrains.compose.resources.imageResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun AllergenSummaryCard(
    allergenType: AllergenType,
    containmentLevel: ContainmentLevel?,
    modifier: Modifier = Modifier,
) {
    // null/FREE_OF -> "Libre"; CONTAINS -> solid filled "CONTIENE"; MAY_CONTAIN -> dashed "TRAZAS"
    // (matching AllergenBadge), so traces are visually distinct from definite allergens.
    val isPresent = containmentLevel != null && containmentLevel != ContainmentLevel.FREE_OF
    val mayContain = containmentLevel == ContainmentLevel.MAY_CONTAIN
    val bgColor = when {
        !isPresent -> MaterialTheme.colorScheme.surface
        mayContain -> Color.Transparent
        else -> allergenType.color.copy(alpha = 0.12f)
    }
    val borderColor = if (isPresent) allergenType.color else MaterialTheme.colorScheme.outlineVariant
    val textColor = if (isPresent) allergenType.color else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    val shape = RoundedCornerShape(12.dp)
    val borderModifier = if (mayContain) {
        Modifier.dashedBorder(borderColor, 12.dp)
    } else {
        Modifier.border(width = if (isPresent) 2.dp else 1.dp, color = borderColor, shape = shape)
    }

    Column(
        modifier = modifier
            .width(100.dp)
            .clip(shape)
            .then(borderModifier)
            .background(bgColor)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Image(
            bitmap = imageResource(allergenType.iconResource()),
            contentDescription = null,
            modifier = Modifier.size(44.dp),
            filterQuality = FilterQuality.High,
        )
        Text(
            text = allergenType.nameEs.uppercase(),
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
        Text(
            text = when {
                mayContain -> stringResource(Res.string.common_allergen_traces)
                isPresent -> stringResource(Res.string.common_allergen_contains)
                else -> stringResource(Res.string.common_allergen_free)
            },
            color = textColor,
            fontSize = 9.sp,
            fontWeight = if (isPresent) FontWeight.Bold else FontWeight.Normal,
        )
    }
}

@Preview
@Composable
private fun PreviewAllergenSummaryCardPresent() {
    MenuAdminTheme {
        AllergenSummaryCard(
            allergenType = AllergenType.GLUTEN,
            containmentLevel = ContainmentLevel.CONTAINS,
        )
    }
}

@Preview
@Composable
private fun PreviewAllergenSummaryCardTrace() {
    MenuAdminTheme {
        AllergenSummaryCard(
            allergenType = AllergenType.LUPINS,
            containmentLevel = ContainmentLevel.MAY_CONTAIN,
        )
    }
}

@Preview
@Composable
private fun PreviewAllergenSummaryCardAbsent() {
    MenuAdminTheme {
        AllergenSummaryCard(
            allergenType = AllergenType.DAIRY,
            containmentLevel = null,
        )
    }
}
