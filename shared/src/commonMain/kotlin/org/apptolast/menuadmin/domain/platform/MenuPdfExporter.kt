package org.apptolast.menuadmin.domain.platform

import kotlinx.serialization.Serializable

/** Generates the allergen-menu PDF (A4 landscape) and downloads it to the user's device. */
interface MenuPdfExporter {
    suspend fun exportAllergenMenu(document: AllergenMenuPdf)
}

/**
 * Plain, serializable description of the allergen menu PDF. The platform implementation serializes
 * this to JSON and renders it (web: jsPDF), so it already carries everything the document needs laid
 * out: [columns] are the allergen header labels and every [AllergenPdfRow.present] flag is aligned
 * positionally to them.
 */
@Serializable
data class AllergenMenuPdf(
    val restaurantName: String,
    val menuName: String,
    val restaurantLogoUrl: String?,
    val companyLogoUrl: String?,
    /** e.g. "Conforme al Reglamento (UE) Nº 1169/2011". */
    val regulationText: String,
    /** Body of the footer note (without the "Nota Informativa:" label, which is drawn separately). */
    val notaText: String,
    val fileName: String,
    val columns: List<String>,
    val rows: List<AllergenPdfRow>,
)

@Serializable
data class AllergenPdfRow(
    val name: String,
    val ingredients: String,
    /** One flag per [AllergenMenuPdf.columns] entry, in the same order. */
    val present: List<Boolean>,
)
