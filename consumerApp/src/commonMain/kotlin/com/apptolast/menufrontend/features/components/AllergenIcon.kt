package com.apptolast.menufrontend.features.components

import com.apptolast.menufrontend.domain.model.Allergen
import com.apptolast.menufrontend.resources.Res
import com.apptolast.menufrontend.resources.allergen_celery
import com.apptolast.menufrontend.resources.allergen_crustaceans
import com.apptolast.menufrontend.resources.allergen_dairy
import com.apptolast.menufrontend.resources.allergen_eggs
import com.apptolast.menufrontend.resources.allergen_fish
import com.apptolast.menufrontend.resources.allergen_gluten
import com.apptolast.menufrontend.resources.allergen_lupin
import com.apptolast.menufrontend.resources.allergen_mollusks
import com.apptolast.menufrontend.resources.allergen_mustard
import com.apptolast.menufrontend.resources.allergen_peanuts
import com.apptolast.menufrontend.resources.allergen_sesame
import com.apptolast.menufrontend.resources.allergen_soy
import com.apptolast.menufrontend.resources.allergen_sulfites
import com.apptolast.menufrontend.resources.allergen_tree_nuts
import org.jetbrains.compose.resources.DrawableResource

/**
 * Dedicated allergen iconography (Spec 005 follow-up). Material Icons has no allergen-specific glyphs, so
 * fish/crustaceans/molluscs and eggs/dairy collided. These are purpose-drawn line icons (SVG drawable
 * resources) — one distinct, recognizable shape per EU allergen — rendered the same on Android and iOS.
 * The single-colour icons are tinted by the `Icon` composable, so they follow the chip's safe/danger colour.
 */
fun Allergen.iconResource(): DrawableResource = when (this) {
    Allergen.GLUTEN -> Res.drawable.allergen_gluten
    Allergen.CRUSTACEANS -> Res.drawable.allergen_crustaceans
    Allergen.EGGS -> Res.drawable.allergen_eggs
    Allergen.FISH -> Res.drawable.allergen_fish
    Allergen.PEANUTS -> Res.drawable.allergen_peanuts
    Allergen.SOY -> Res.drawable.allergen_soy
    Allergen.DAIRY -> Res.drawable.allergen_dairy
    Allergen.TREE_NUTS -> Res.drawable.allergen_tree_nuts
    Allergen.CELERY -> Res.drawable.allergen_celery
    Allergen.MUSTARD -> Res.drawable.allergen_mustard
    Allergen.SESAME -> Res.drawable.allergen_sesame
    Allergen.SULFITES -> Res.drawable.allergen_sulfites
    Allergen.LUPIN -> Res.drawable.allergen_lupin
    Allergen.MOLLUSKS -> Res.drawable.allergen_mollusks
}
