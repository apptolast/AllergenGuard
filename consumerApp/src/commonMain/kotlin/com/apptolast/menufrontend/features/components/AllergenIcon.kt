package com.apptolast.menufrontend.features.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BakeryDining
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.Egg
import androidx.compose.material.icons.outlined.FilterVintage
import androidx.compose.material.icons.outlined.Grain
import androidx.compose.material.icons.outlined.Grass
import androidx.compose.material.icons.outlined.LocalFlorist
import androidx.compose.material.icons.outlined.Park
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.SetMeal
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.Water
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.Waves
import androidx.compose.ui.graphics.vector.ImageVector
import com.apptolast.menufrontend.domain.model.Allergen

// Spec 005: every allergen maps to a DISTINCT icon. Material Icons has no shrimp, so crustaceans uses
// `Waves` — the key requirement is that crustaceans, fish and molluscs read as three different icons
// (previously crustaceans shared `SetMeal` with fish).
fun Allergen.icon(): ImageVector = when (this) {
    Allergen.GLUTEN -> Icons.Outlined.BakeryDining
    Allergen.CRUSTACEANS -> Icons.Outlined.Waves
    Allergen.EGGS -> Icons.Outlined.Egg
    Allergen.FISH -> Icons.Outlined.SetMeal
    Allergen.PEANUTS -> Icons.Outlined.FilterVintage
    Allergen.SOY -> Icons.Outlined.Grass
    Allergen.DAIRY -> Icons.Outlined.WaterDrop
    Allergen.TREE_NUTS -> Icons.Outlined.Park
    Allergen.CELERY -> Icons.Outlined.Eco
    Allergen.MUSTARD -> Icons.Outlined.LocalFlorist
    Allergen.SESAME -> Icons.Outlined.Grain
    Allergen.SULFITES -> Icons.Outlined.Science
    Allergen.LUPIN -> Icons.Outlined.Spa
    Allergen.MOLLUSKS -> Icons.Outlined.Water
}
