package org.apptolast.menuadmin.platform

import kotlin.js.ExperimentalWasmJsInterop

actual fun launchAllergenPdf(payloadJson: String) {
    generateAllergenPdf(payloadJson)
}

// Calls the global `generateAllergenPdf` installed by allergen-pdf.js.
@OptIn(ExperimentalWasmJsInterop::class)
private fun generateAllergenPdf(payloadJson: String): Unit = js("window.generateAllergenPdf(payloadJson)")
