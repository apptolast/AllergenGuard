package com.apptolast.menufrontend.data.firebase

import com.apptolast.menufrontend.domain.model.Allergen
import com.apptolast.menufrontend.domain.model.Dish
import com.apptolast.menufrontend.domain.model.Restaurant
import org.apptolast.menuadmin.data.remote.firebase.FirestoreDocument

// Maps between the shared Firestore model (admin) and the consumer's own view models.
// The 14 EU allergens differ only in a few names (MILK=DAIRY, SOYA=SOY, SULPHITES=SULFITES, MOLLUSCS=MOLLUSKS).

internal fun allergenFromCode(code: String): Allergen? = when (code.uppercase()) {
    "GLUTEN" -> Allergen.GLUTEN
    "CRUSTACEANS" -> Allergen.CRUSTACEANS
    "EGGS" -> Allergen.EGGS
    "FISH" -> Allergen.FISH
    "PEANUTS" -> Allergen.PEANUTS
    "SOYA", "SOY" -> Allergen.SOY
    "MILK", "DAIRY" -> Allergen.DAIRY
    "TREE_NUTS" -> Allergen.TREE_NUTS
    "CELERY" -> Allergen.CELERY
    "MUSTARD" -> Allergen.MUSTARD
    "SESAME" -> Allergen.SESAME
    "SULPHITES", "SULFITES" -> Allergen.SULFITES
    "LUPIN", "LUPINS" -> Allergen.LUPIN
    "MOLLUSCS", "MOLLUSKS" -> Allergen.MOLLUSKS
    else -> null
}

internal fun Allergen.toApiCode(): String = when (this) {
    Allergen.GLUTEN -> "GLUTEN"
    Allergen.CRUSTACEANS -> "CRUSTACEANS"
    Allergen.EGGS -> "EGGS"
    Allergen.FISH -> "FISH"
    Allergen.PEANUTS -> "PEANUTS"
    Allergen.SOY -> "SOYA"
    Allergen.DAIRY -> "MILK"
    Allergen.TREE_NUTS -> "TREE_NUTS"
    Allergen.CELERY -> "CELERY"
    Allergen.MUSTARD -> "MUSTARD"
    Allergen.SESAME -> "SESAME"
    Allergen.SULFITES -> "SULPHITES"
    Allergen.LUPIN -> "LUPIN"
    Allergen.MOLLUSKS -> "MOLLUSCS"
}

@Suppress("UNCHECKED_CAST")
internal fun FirestoreDocument.toRestaurant(): Restaurant {
    val rating = fields["rating"] as? Map<String, Any?>
    return Restaurant(
        id = id,
        name = fields["name"] as? String ?: "",
        cuisineType = fields["cuisineType"] as? String ?: "",
        rating = ((rating?.get("average") as? Double) ?: 0.0).toFloat(),
        reviewCount = ((rating?.get("count") as? Long) ?: 0L).toInt(),
        dishCount = 0,
        imageUrl = fields["logoUrl"] as? String,
        distance = null,
    )
}

@Suppress("UNCHECKED_CAST")
internal fun FirestoreDocument.toDish(restaurantId: String): Dish {
    val ingredients = (fields["ingredients"] as? List<Any?>).orEmpty().mapNotNull {
        (it as? Map<String, Any?>)?.get("name") as? String
    }
    val codes = (fields["computedAllergens"] as? List<Any?>).orEmpty().map {
        (it as? Map<String, Any?>)?.get("code") as? String
    }
    val parsed = parseComputedAllergens(codes)
    return Dish(
        id = id,
        restaurantId = restaurantId,
        name = fields["name"] as? String ?: "",
        description = fields["description"] as? String ?: "",
        category = fields["section"] as? String ?: "",
        price = (fields["price"] as? Double) ?: (fields["price"] as? Long)?.toDouble() ?: 0.0,
        imageUrl = fields["imageUrl"] as? String,
        ingredients = ingredients,
        allergens = parsed.allergens,
        isSubRecipe = fields["isSubRecipe"] as? Boolean ?: false,
        hasUnknownAllergen = parsed.hasUnknown,
    )
}
