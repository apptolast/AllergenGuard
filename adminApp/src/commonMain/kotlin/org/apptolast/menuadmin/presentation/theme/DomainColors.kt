package org.apptolast.menuadmin.presentation.theme

import androidx.compose.ui.graphics.Color
import org.apptolast.menuadmin.domain.model.AllergenType
import org.apptolast.menuadmin.domain.model.SafetyLevel

// Compose colors for domain enums. Kept out of the domain so the (data + domain) :shared module
// stays free of Compose; the domain only carries a plain ARGB Long (`colorArgb`).

val AllergenType.color: Color get() = Color(colorArgb)

val SafetyLevel.color: Color get() = Color(colorArgb)
