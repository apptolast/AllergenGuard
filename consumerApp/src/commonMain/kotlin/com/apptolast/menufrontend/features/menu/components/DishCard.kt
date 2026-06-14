package com.apptolast.menufrontend.features.menu.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.apptolast.menufrontend.core.theme.AllergenActiveBg
import com.apptolast.menufrontend.core.theme.AllergenActiveText
import com.apptolast.menufrontend.core.theme.SafeGreenBorder
import com.apptolast.menufrontend.core.theme.SafeGreenLight
import com.apptolast.menufrontend.domain.model.Allergen
import com.apptolast.menufrontend.domain.model.Dish
import com.apptolast.menufrontend.features.components.icon
import com.apptolast.menufrontend.resources.Res
import com.apptolast.menufrontend.resources.menu_contains
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DishCard(
    dish: Dish,
    userAllergens: Set<Allergen>,
    allergenLabels: Map<Allergen, String>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isSafe = dish.allergens.none { it in userAllergens }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (isSafe) {
                    Modifier.border(1.dp, SafeGreenBorder, RoundedCornerShape(12.dp))
                } else {
                    Modifier
                },
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSafe) SafeGreenLight else MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = dish.name.uppercase(),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Text(
                text = dish.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (dish.allergens.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = stringResource(Res.string.menu_contains),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        dish.allergens.forEach { allergen ->
                            val isDangerous = allergen in userAllergens
                            AllergenBadge(
                                allergen = allergen,
                                label = allergenLabels[allergen] ?: allergen.name,
                                isDangerous = isDangerous,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AllergenBadge(
    allergen: Allergen,
    label: String,
    isDangerous: Boolean,
) {
    Card(
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDangerous) AllergenActiveBg else MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector = allergen.icon(),
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = if (isDangerous) AllergenActiveText else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (isDangerous) AllergenActiveText else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
