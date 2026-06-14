package org.apptolast.menuadmin.presentation.screens.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.apptolast.menuadmin.domain.model.AllergenType
import org.apptolast.menuadmin.presentation.theme.MenuAdminTheme
import org.apptolast.menuadmin.presentation.theme.color

@Composable
fun AllergenFrequencyChart(
    allergenFrequency: Map<AllergenType, Int>,
    maxValue: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        allergenFrequency.entries
            .sortedByDescending { it.value }
            .forEach { (allergen, count) ->
                AllergenBarRow(
                    allergen = allergen,
                    count = count,
                    maxValue = maxValue,
                )
            }
    }
}

@Composable
private fun AllergenBarRow(
    allergen: AllergenType,
    count: Int,
    maxValue: Int,
    modifier: Modifier = Modifier,
) {
    val percentage = if (maxValue > 0) (count.toFloat() / maxValue.toFloat()) else 0f
    val percentageText = "${(percentage * 100).toInt()}%"

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = allergen.nameEs,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(120.dp),
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .height(20.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.background),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = percentage)
                    .height(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(allergen.color),
            )
        }

        Text(
            text = percentageText,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(40.dp),
        )
    }
}

@Preview
@Composable
private fun AllergenFrequencyChartPreview() {
    MenuAdminTheme {
        AllergenFrequencyChart(
            allergenFrequency = mapOf(
                AllergenType.GLUTEN to 42,
                AllergenType.DAIRY to 35,
                AllergenType.EGGS to 28,
                AllergenType.FISH to 15,
            ),
            maxValue = 42,
        )
    }
}
