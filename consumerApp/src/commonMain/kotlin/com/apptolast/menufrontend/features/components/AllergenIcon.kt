package com.apptolast.menufrontend.features.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BakeryDining
import androidx.compose.material.icons.outlined.Egg
import androidx.compose.material.icons.outlined.FilterVintage
import androidx.compose.material.icons.outlined.Grass
import androidx.compose.material.icons.outlined.LocalFlorist
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.SetMeal
import androidx.compose.material.icons.outlined.Water
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.ui.graphics.vector.ImageVector
import com.apptolast.menufrontend.domain.model.Allergen

fun Allergen.icon(): ImageVector = when (this) {
    Allergen.GLUTEN -> Icons.Outlined.BakeryDining
    Allergen.FISH -> Icons.Outlined.SetMeal
    Allergen.PEANUTS -> Icons.Outlined.FilterVintage
    Allergen.DAIRY -> Icons.Outlined.WaterDrop
    Allergen.EGGS -> Icons.Outlined.Egg
    Allergen.SOY -> Icons.Outlined.Grass
    Allergen.SULFITES -> Icons.Outlined.Science
    Allergen.MOLLUSKS -> Icons.Outlined.Water
    Allergen.CRUSTACEANS -> Icons.Outlined.SetMeal
    Allergen.TREE_NUTS -> Icons.Outlined.FilterVintage
    Allergen.CELERY -> Icons.Outlined.Grass
    Allergen.MUSTARD -> Icons.Outlined.LocalFlorist
    Allergen.SESAME -> Icons.Outlined.FilterVintage
    Allergen.LUPIN -> Icons.Outlined.LocalFlorist
}
