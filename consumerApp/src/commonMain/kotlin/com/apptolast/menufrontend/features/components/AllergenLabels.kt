package com.apptolast.menufrontend.features.components

import androidx.compose.runtime.Composable
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
import org.jetbrains.compose.resources.stringResource

/**
 * Localized display labels for every EU allergen. Shared by the menu filter row, the profile
 * allergy chips and the allergen edit sheet so the wording stays consistent across the app.
 */
@Composable
fun allergenLabels(): Map<Allergen, String> = mapOf(
    Allergen.GLUTEN to stringResource(Res.string.allergen_gluten),
    Allergen.FISH to stringResource(Res.string.allergen_fish),
    Allergen.PEANUTS to stringResource(Res.string.allergen_peanuts),
    Allergen.DAIRY to stringResource(Res.string.allergen_dairy),
    Allergen.EGGS to stringResource(Res.string.allergen_eggs),
    Allergen.SOY to stringResource(Res.string.allergen_soy),
    Allergen.SULFITES to stringResource(Res.string.allergen_sulfites),
    Allergen.MOLLUSKS to stringResource(Res.string.allergen_mollusks),
    Allergen.CRUSTACEANS to stringResource(Res.string.allergen_crustaceans),
    Allergen.TREE_NUTS to stringResource(Res.string.allergen_tree_nuts),
    Allergen.CELERY to stringResource(Res.string.allergen_celery),
    Allergen.MUSTARD to stringResource(Res.string.allergen_mustard),
    Allergen.SESAME to stringResource(Res.string.allergen_sesame),
    Allergen.LUPIN to stringResource(Res.string.allergen_lupin),
)
