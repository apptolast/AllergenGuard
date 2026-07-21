package org.apptolast.menuadmin.presentation.components

import menuadmin.adminapp.generated.resources.Res
import menuadmin.adminapp.generated.resources.allergen_celery
import menuadmin.adminapp.generated.resources.allergen_crustaceans
import menuadmin.adminapp.generated.resources.allergen_dairy
import menuadmin.adminapp.generated.resources.allergen_eggs
import menuadmin.adminapp.generated.resources.allergen_fish
import menuadmin.adminapp.generated.resources.allergen_gluten
import menuadmin.adminapp.generated.resources.allergen_lupin
import menuadmin.adminapp.generated.resources.allergen_mollusks
import menuadmin.adminapp.generated.resources.allergen_mustard
import menuadmin.adminapp.generated.resources.allergen_peanuts
import menuadmin.adminapp.generated.resources.allergen_sesame
import menuadmin.adminapp.generated.resources.allergen_soy
import menuadmin.adminapp.generated.resources.allergen_sulfites
import menuadmin.adminapp.generated.resources.allergen_tree_nuts
import org.apptolast.menuadmin.domain.model.AllergenType
import org.jetbrains.compose.resources.DrawableResource

/**
 * Official UK Food Standards Agency allergen icons (Open Government Licence v3.0). Shared with the
 * consumer app and the allergen PDF so the whole product uses one recognizable, standard icon set.
 */
fun AllergenType.iconResource(): DrawableResource =
    when (this) {
        AllergenType.GLUTEN -> Res.drawable.allergen_gluten
        AllergenType.CRUSTACEANS -> Res.drawable.allergen_crustaceans
        AllergenType.EGGS -> Res.drawable.allergen_eggs
        AllergenType.FISH -> Res.drawable.allergen_fish
        AllergenType.PEANUTS -> Res.drawable.allergen_peanuts
        AllergenType.SOY -> Res.drawable.allergen_soy
        AllergenType.DAIRY -> Res.drawable.allergen_dairy
        AllergenType.TREE_NUTS -> Res.drawable.allergen_tree_nuts
        AllergenType.CELERY -> Res.drawable.allergen_celery
        AllergenType.MUSTARD -> Res.drawable.allergen_mustard
        AllergenType.SESAME -> Res.drawable.allergen_sesame
        AllergenType.SULFITES -> Res.drawable.allergen_sulfites
        AllergenType.LUPINS -> Res.drawable.allergen_lupin
        AllergenType.MOLLUSKS -> Res.drawable.allergen_mollusks
    }
