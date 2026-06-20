package com.apptolast.menufrontend.features.menu.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.apptolast.menufrontend.core.theme.AllergenGuardTheme
import com.apptolast.menufrontend.domain.model.Allergen
import com.apptolast.menufrontend.features.components.AllergenFilterChip
import com.apptolast.menufrontend.resources.Res
import com.apptolast.menufrontend.resources.menu_restore_filters
import com.apptolast.menufrontend.resources.menu_select_allergies
import com.apptolast.menufrontend.resources.menu_show_less
import com.apptolast.menufrontend.resources.menu_show_more
import org.jetbrains.compose.resources.stringResource

/**
 * Allergen filter for the menu. Collapsed it shows only the active filters (so the screen isn't
 * dominated by chips); a "show more / show less" toggle reveals every allergen. Filtering still
 * applies regardless of what's currently visible.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AllergenFilterSection(
    allergenLabels: Map<Allergen, String>,
    activeFilters: Set<Allergen>,
    userAllergens: Set<Allergen>,
    expanded: Boolean,
    modifier: Modifier = Modifier,
    onToggleAllergen: (Allergen) -> Unit = {},
    onToggleExpanded: () -> Unit = {},
    onRestoreFilters: () -> Unit = {},
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            // Reserve the button's touch-target height so the header doesn't jump when the
            // "restore" action appears/disappears as filters drift from the profile.
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(Res.string.menu_select_allergies),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )

            // Only offer to restore when the filters have drifted from the saved profile.
            if (userAllergens.isNotEmpty() && activeFilters != userAllergens) {
                TextButton(
                    onClick = onRestoreFilters,
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = stringResource(Res.string.menu_restore_filters),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }

        val visible = if (expanded) {
            Allergen.entries
        } else {
            Allergen.entries.filter { it in activeFilters }
        }

        if (visible.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                visible.forEach { allergen ->
                    AllergenFilterChip(
                        allergen = allergen,
                        label = allergenLabels[allergen] ?: allergen.name,
                        isSelected = allergen in activeFilters,
                        onToggle = { onToggleAllergen(allergen) },
                    )
                }
            }
        }

        // Only worth a toggle when there are hidden allergens to reveal (or to collapse again).
        if (expanded || activeFilters.size < Allergen.entries.size) {
            TextButton(
                onClick = onToggleExpanded,
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = stringResource(
                            if (expanded) Res.string.menu_show_less else Res.string.menu_show_more,
                        ),
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Icon(
                        imageVector = if (expanded) {
                            Icons.Filled.KeyboardArrowUp
                        } else {
                            Icons.Filled.KeyboardArrowDown
                        },
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun PreviewAllergenFilterSectionCollapsed() {
    AllergenGuardTheme {
        AllergenFilterSection(
            allergenLabels = Allergen.entries.associateWith { it.name },
            activeFilters = setOf(Allergen.GLUTEN, Allergen.FISH, Allergen.SOY),
            userAllergens = setOf(Allergen.GLUTEN, Allergen.FISH, Allergen.SOY),
            expanded = false,
            modifier = Modifier.background(color = MaterialTheme.colorScheme.background)
        )
    }
}

@Preview
@Composable
private fun PreviewAllergenFilterSectionExpanded() {
    AllergenGuardTheme {
        AllergenFilterSection(
            allergenLabels = Allergen.entries.associateWith { it.name },
            activeFilters = setOf(Allergen.GLUTEN, Allergen.FISH, Allergen.SOY, Allergen.TREE_NUTS),
            userAllergens = setOf(Allergen.GLUTEN, Allergen.FISH, Allergen.SOY),
            expanded = true,
            modifier = Modifier.background(color = MaterialTheme.colorScheme.background)
        )
    }
}
