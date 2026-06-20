package org.apptolast.menuadmin.platform

// Resolves to the global `generateAllergenPdf` installed by allergen-pdf.js.
private external fun generateAllergenPdf(payloadJson: String)

actual fun launchAllergenPdf(payloadJson: String) {
    generateAllergenPdf(payloadJson)
}
