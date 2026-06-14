package org.apptolast.menuadmin.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.apptolast.menuadmin.domain.model.AllergenType
import org.apptolast.menuadmin.presentation.theme.MenuAdminTheme
import org.apptolast.menuadmin.presentation.theme.color

@Composable
fun AllergenSummaryCard(
    allergenType: AllergenType,
    isPresent: Boolean,
    modifier: Modifier = Modifier,
) {
    val bgColor = if (isPresent) {
        allergenType.color.copy(alpha = 0.12f)
    } else {
        MaterialTheme.colorScheme.surface
    }
    val borderColor = if (isPresent) allergenType.color else MaterialTheme.colorScheme.outlineVariant
    val textColor = if (isPresent) allergenType.color else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    val shape = RoundedCornerShape(12.dp)

    Column(
        modifier = modifier
            .width(100.dp)
            .clip(shape)
            .border(
                width = if (isPresent) 2.dp else 1.dp,
                color = borderColor,
                shape = shape,
            )
            .background(bgColor)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        LucideIcon(
            codepoint = allergenType.icon,
            size = 24.sp,
            color = textColor,
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
            text = if (isPresent) "CONTIENE" else "Libre",
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
            isPresent = true,
        )
    }
}

@Preview
@Composable
private fun PreviewAllergenSummaryCardAbsent() {
    MenuAdminTheme {
        AllergenSummaryCard(
            allergenType = AllergenType.DAIRY,
            isPresent = false,
        )
    }
}
