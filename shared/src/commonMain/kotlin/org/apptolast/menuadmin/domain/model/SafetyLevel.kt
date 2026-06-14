package org.apptolast.menuadmin.domain.model

enum class SafetyLevel(
    val apiValue: String,
    val labelEs: String,
    // ARGB color as a plain Long so the domain stays free of Compose (see SafetyLevel.color in UI).
    val colorArgb: Long,
) {
    SAFE("SAFE", "Seguro", 0xFF22C55E),
    RISK("RISK", "Riesgo", 0xFFF59E0B),
    DANGER("DANGER", "Peligro", 0xFFEF4444),
    ;

    companion object {
        fun fromApi(value: String): SafetyLevel? = entries.firstOrNull { it.apiValue.equals(value, ignoreCase = true) }
    }
}
