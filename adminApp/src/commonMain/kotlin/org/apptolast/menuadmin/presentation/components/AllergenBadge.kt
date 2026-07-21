package org.apptolast.menuadmin.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import menuadmin.adminapp.generated.resources.Res
import menuadmin.adminapp.generated.resources.common_allergen_traces_label
import org.apptolast.menuadmin.domain.model.AllergenType
import org.apptolast.menuadmin.domain.model.ContainmentLevel
import org.apptolast.menuadmin.presentation.theme.MenuAdminTheme
import org.apptolast.menuadmin.presentation.theme.color
import org.jetbrains.compose.resources.imageResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun AllergenBadge(
    allergenType: AllergenType,
    isActive: Boolean,
    onClick: (() -> Unit)? = null,
    compact: Boolean = false,
    containmentLevel: ContainmentLevel = ContainmentLevel.CONTAINS,
    modifier: Modifier = Modifier,
) {
    // "May contain" (traces) is shown as a hollow, dashed-border chip with a "(trazas)" qualifier so it
    // is unmistakable from a definite allergen ("contains"), which stays a solid filled chip.
    val mayContain = isActive && containmentLevel == ContainmentLevel.MAY_CONTAIN
    val backgroundColor = when {
        !isActive -> MenuAdminTheme.colors.allergenInactiveBg
        mayContain -> Color.Transparent
        else -> allergenType.color.copy(alpha = 0.15f)
    }
    val contentColor = if (isActive) allergenType.color else MenuAdminTheme.colors.allergenInactiveText
    val borderColor = if (isActive) allergenType.color else MaterialTheme.colorScheme.outlineVariant

    val cornerRadius = if (compact) 8.dp else 12.dp
    val shape = RoundedCornerShape(cornerRadius)
    val borderModifier = if (mayContain) {
        Modifier.dashedBorder(borderColor, cornerRadius)
    } else {
        Modifier.border(width = 1.dp, color = borderColor, shape = shape)
    }

    Row(
        modifier = modifier
            .clip(shape)
            .then(borderModifier)
            .background(backgroundColor)
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                },
            )
            .padding(
                vertical = if (compact) 4.dp else 8.dp,
                horizontal = if (compact) 8.dp else 14.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(if (compact) 5.dp else 8.dp),
    ) {
        Image(
            bitmap = imageResource(allergenType.iconResource()),
            contentDescription = null,
            modifier = Modifier.size(if (compact) 24.dp else 28.dp),
            filterQuality = FilterQuality.High,
        )
        Text(
            text = if (mayContain) {
                stringResource(Res.string.common_allergen_traces_label, allergenType.nameEs)
            } else {
                allergenType.nameEs
            },
            color = contentColor,
            fontSize = if (compact) 12.sp else 14.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
        )
    }
}

/** Rounded dashed outline, used to mark "may contain" (traces) allergens. */
internal fun Modifier.dashedBorder(
    color: Color,
    cornerRadius: Dp,
): Modifier =
    drawBehind {
        val strokeWidth = 1.dp.toPx()
        drawRoundRect(
            color = color,
            topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
            size = Size(size.width - strokeWidth, size.height - strokeWidth),
            cornerRadius = CornerRadius(cornerRadius.toPx()),
            style = Stroke(width = strokeWidth, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 5f))),
        )
    }

@Preview
@Composable
private fun AllergenBadgeActivePreview() {
    MenuAdminTheme {
        AllergenBadge(
            allergenType = AllergenType.GLUTEN,
            isActive = true,
            onClick = {},
        )
    }
}

@Preview
@Composable
private fun AllergenBadgeInactivePreview() {
    MenuAdminTheme {
        AllergenBadge(
            allergenType = AllergenType.DAIRY,
            isActive = false,
            onClick = {},
        )
    }
}
