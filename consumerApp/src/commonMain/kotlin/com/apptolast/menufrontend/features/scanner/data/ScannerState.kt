package com.apptolast.menufrontend.features.scanner.data

data class ScannerState(
    val isScanning: Boolean = true,
    val scannedCode: String? = null,
    val error: String? = null,
)

sealed interface ScannerAction {
    data class CodeScanned(val code: String) : ScannerAction
    data object NavigateBack : ScannerAction
    data object RetryScanning : ScannerAction
}
