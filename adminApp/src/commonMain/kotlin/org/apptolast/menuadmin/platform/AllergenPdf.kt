package org.apptolast.menuadmin.platform

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.apptolast.menuadmin.domain.model.AllergenType
import org.apptolast.menuadmin.domain.model.Menu
import org.apptolast.menuadmin.domain.model.Recipe

/**
 * Triggers browser-side generation of the allergen menu PDF.
 *
 * The actual implementation calls the JS renderer exposed as
 * `window.generateAllergenPdf` (see `adminApp/src/webMain/resources/allergen-pdf.js`),
 * which lazily loads jsPDF (`jspdf.umd.min.js`) and triggers the file download.
 *
 * NOTE: the renderer draws the restaurant/company logos onto a `<canvas>` with
 * `crossOrigin = "anonymous"`, so the Firebase Storage bucket must allow CORS
 * for this origin (see `storage.cors.json` / `scripts/apply-storage-cors.sh`).
 */
expect fun launchAllergenPdf(payloadJson: String)

/**
 * Default allergen-information texts shown on the PDF. These are reconstructed
 * defaults grounded in EU Regulation (EU) No 1169/2011 on the provision of food
 * information to consumers; tweak them per restaurant if a different wording is
 * required.
 */
const val ALLERGEN_PDF_REGULATION_TEXT: String =
    "Información sobre alérgenos conforme al Reglamento (UE) nº 1169/2011"

const val ALLERGEN_PDF_NOTA_TEXT: String =
    "Si tiene alguna alergia o intolerancia alimentaria, consúltelo con nuestro " +
        "personal antes de realizar su pedido. La ausencia de un alérgeno en esta " +
        "tabla no garantiza la inexistencia de trazas."

/** Serialised contract consumed by `window.generateAllergenPdf` (allergen-pdf.js). */
@Serializable
data class AllergenPdfPayload(
    val restaurantName: String,
    val regulationText: String,
    val notaText: String,
    val fileName: String,
    val columns: List<String>,
    val rows: List<AllergenPdfRow>,
    val restaurantLogoUrl: String? = null,
    val companyLogoUrl: String? = null,
)

@Serializable
data class AllergenPdfRow(
    val name: String,
    val ingredients: String,
    /** One flag per column in [AllergenPdfPayload.columns]: true if the dish contains that allergen. */
    val present: List<Boolean>,
)

private val allergenPdfJson = Json { encodeDefaults = false }

/**
 * Builds the PDF payload from the selected [menu] and its fully-loaded [recipes]
 * (recipes carry `computedAllergens`). Columns are the 14 EU allergens, in the
 * canonical [AllergenType] order; each row marks which of them the dish contains.
 */
fun buildAllergenPdfPayload(
    menu: Menu,
    recipes: List<Recipe>,
    regulationText: String = ALLERGEN_PDF_REGULATION_TEXT,
    notaText: String = ALLERGEN_PDF_NOTA_TEXT,
): AllergenPdfPayload {
    val allergens = AllergenType.entries
    val rows = recipes.map { recipe ->
        AllergenPdfRow(
            name = recipe.name,
            ingredients = recipe.ingredients
                .joinToString(", ") { it.ingredientName }
                .ifBlank { "—" },
            present = allergens.map { it in recipe.computedAllergens },
        )
    }
    val displayName = menu.name.ifBlank { "Menú" }
    return AllergenPdfPayload(
        restaurantName = displayName,
        regulationText = regulationText,
        notaText = notaText,
        fileName = "menu-alergenos-${slugify(displayName)}.pdf",
        columns = allergens.map { it.nameEs },
        rows = rows,
        restaurantLogoUrl = menu.restaurantLogoUrl,
        companyLogoUrl = menu.companyLogoUrl,
    )
}

/** Serialises a payload to the JSON string expected by `window.generateAllergenPdf`. */
fun encodeAllergenPdfPayload(payload: AllergenPdfPayload): String =
    allergenPdfJson.encodeToString(AllergenPdfPayload.serializer(), payload)

private fun slugify(value: String): String =
    value.lowercase()
        .map { if (it.isLetterOrDigit()) it else '-' }
        .joinToString("")
        .trim('-')
        .ifBlank { "menu" }
